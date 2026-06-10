(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.directive('diagram', diagram);
	
	diagram.$inject = ['$timeout'];
	function diagram($timeout) {
		var directive = {
			restrict: 'E',
			scope: true,
			replace: true,
			bindToController: {
				source: '='
			},
			controller: 'BrowserDiagramController',
			controllerAs: 'diagramVm',
			link: link
		};
		return directive;
		
		function link(scope, element, attributes, controller) {
			var source = controller.source;

			var svgIsaModel = [];
			var svgAttrModel = [];
			if (source.relationships.length > 0) {
				angular.forEach(source.relationships, function (field, i) {
					if (field.active == true) {
						if (field.type.conceptId == "116680003")
							svgIsaModel.push(field);
						else
							svgAttrModel.push(field);
					}
				});
			}

			var width = 1000,
				height = 500;

			var x = 10,
			    y = 10,
			    maxX = 10,
			    sctClass = "";

			if (source.concept.definitionStatus.id == "900000000000074008") // Primitive
				sctClass = "sct-primitive-concept";
			else
				sctClass = "sct-defined-concept";

			$timeout(function () {
				var svg = d3.select(element[0])
				.append("svg")
				.attr('width', width)
				.attr('height', height);

				controller.loadDefs(svg);

				var rect1 = controller.drawSctBox(svg,
															x,
															y,
															source.concept.term,
															source.concept.conceptId,
															sctClass);

				x = x + 90;
				y = y + rect1.node().getBBox().height + 40;
				var circle1;
				if (source.concept.definitionStatus.id == "900000000000074008")
					circle1 = controller.drawSubsumedByNode(svg, x, y);
				else
					circle1 = controller.drawEquivalentNode(svg, x, y);

				controller.connectElements(svg, rect1, circle1, 'bottom-50', 'left');

				x = x + 55;
				var circle2 = controller.drawConjunctionNode(svg, x, y);
			    controller.connectElements(svg, circle1, circle2, 'right', 'left', 'LineMarker');
			    x = x + 40;
			    y = y - 18;
			    maxX = ((maxX < x) ? x : maxX);
			    // load stated parents
			    sctClass = "sct-defined-concept";

			    angular.forEach(svgIsaModel, function(relationship, i) {
			    	if (relationship.destination.definitionStatus.id == "900000000000074008")
			    		sctClass = "sct-primitive-concept";
			    	else
			    		sctClass = "sct-defined-concept";

			    	var rectParent = controller.drawSctBox(svg, x, y, relationship.destination.term, relationship.destination.conceptId, sctClass);

			    	controller.connectElements(svg, circle2, rectParent, 'center', 'left', 'ClearTriangle');
			        y = y + rectParent.node().getBBox().height + 25;
			        maxX = ((maxX < x + rectParent.node().getBBox().width + 50) ? x + rectParent.node().getBBox().width + 50 : maxX);
			    });

			    // load ungrouped attributes
				var maxRoleNumber = 0;
				angular.forEach(svgAttrModel, function(relationship, i) {
					if (relationship.destination.definitionStatus.id == "900000000000074008")
			            sctClass = "sct-primitive-concept";
		       		else
			            sctClass = "sct-defined-concept";

			        if (relationship.relationshipGroup == 0) {
			        	var rectAttr = controller.drawSctBox(svg, x, y, relationship.type.term,relationship.type.conceptId, "sct-attribute");
			            controller.connectElements(svg, circle2, rectAttr, 'center', 'left');
			            var rectTarget = controller.drawSctBox(svg, x + rectAttr.node().getBBox().width + 50, y, relationship.destination.term,relationship.destination.conceptId, sctClass);
			            controller.connectElements(svg, rectAttr, rectTarget, 'right', 'left');
			            y = y + rectTarget.node().getBBox().height + 25;
			            maxX = ((maxX < x + rectAttr.node().getBBox().width + 50 + rectTarget.node().getBBox().width + 50) ? x + rectAttr.node().getBBox().width + 50 + rectTarget.node().getBBox().width + 50 : maxX);
			        } else {
			        	if (relationship.relationshipGroup > maxRoleNumber) {
			                maxRoleNumber = relationship.relationshipGroup;
			            }
			        }
				});

				y = y + 15;
				for (var i = 1; i <= maxRoleNumber; i++) {
					var groupNode = controller.drawAttributeGroupNode(svg, x, y);
			        controller.connectElements(svg, circle2, groupNode, 'center', 'left');
			        var conjunctionNode = controller.drawConjunctionNode(svg, x + 55, y);
			        controller.connectElements(svg, groupNode, conjunctionNode, 'right', 'left');

			        angular.forEach(svgAttrModel, function(relationship, m) {
			        	if (relationship.relationshipGroup == i) {
			        		if (relationship.destination.definitionStatus.id == "900000000000074008")
			        			sctClass = "sct-primitive-concept";
			        		else
			        			sctClass = "sct-defined-concept";

			        		var rectRole = controller.drawSctBox(svg, x + 85, y - 18, relationship.type.term, relationship.type.conceptId,"sct-attribute");
			                controller.connectElements(svg, conjunctionNode, rectRole, 'center', 'left');
			                var rectRole2 = controller.drawSctBox(svg, x + 85 + rectRole.node().getBBox().width + 30, y - 18, relationship.destination.term,relationship.destination.conceptId, sctClass);
			                controller.connectElements(svg, rectRole, rectRole2, 'right', 'left');
			                y = y + rectRole2.node().getBBox().height + 25;
			                maxX = ((maxX < x + 85 + rectRole.node().getBBox().width + 30 + rectRole2.node().getBBox().width + 50) ? x + 85 + rectRole.node().getBBox().width + 30 + rectRole2.node().getBBox().width + 50 : maxX);
			        	}
			        });
				}
				svg.attr('height', svg.node().getBBox().height + 100);
			}, 50);
		};
	};
	
	
})();