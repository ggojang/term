/**
 * Concept Editor on concept visualization
 */
(function() {
	'use strict';
	
	angular.module('app.concept').controller('ConceptTab2Controller', ConceptTab2Controller);
	
	ConceptTab2Controller.$inject = ['$scope', '$element'];
	
	function ConceptTab2Controller($scope, $element) {
		
		$scope.visualization = undefined;
		$scope.inferredData = null;
		//$scope.cvHeight = 500;
		$scope.$on('loadInferredData', function(event, classes){
			//initTab2Ctrl();
			$scope.visualization = undefined;
			$scope.inferredData = classes;
			//$scope.cvHeight = angular.element($element).parents('md-tab-content').height();//md-tab-content
			console.log($scope.cvHeight);
			var vData = {
					"name" : classes.equalClasses[classes.equalClasses.length-1].conceptId+" : "+ classes.equalClasses[classes.equalClasses.length-1].term
					, "size" : 48
					, "type" : "main"
					, "children" : []
			};
			
			if(classes.equalClasses[classes.equalClasses.length-1].attrs.length > 0) {
				var attrsData = {"name" : "Attribute", "size" : 12, "type" : "attr", "children" : []};
				angular.forEach(classes.equalClasses[classes.equalClasses.length-1].attrs, function(attr, key){
					var data = {
							"name" : attr.name.conceptId+" : "+attr.name.term+" = "+attr.value.conceptId+" : "+attr.value.term
							, "size" : 6
							, "type" : "attr"
						};
					attrsData.children.push(data);
				});
				vData.children.push(attrsData);
			}
			
			if(classes.equalClasses.length > 1) {
				var equalData = {"name" : "Equivalent Classes", "size" : 24, "type" : "equal", "children" : []};
				angular.forEach(classes.equalClasses, function(clazz, key){
					if((key+1) < classes.equalClasses.length) {
						var classData = {"name" : clazz.conceptId+" : "+clazz.term, "size" : 12, "type" : "equal"};
						if(clazz.attrs.length > 0) {
							var attrsData = {"name" : "Attribute", "size" : 12, "type" : "attr", "children" : []};
							angular.forEach(clazz.attrs, function(attr, key){
								var data = {
										"name" : attr.name.conceptId+" : "+attr.name.term+" = "+attr.value.conceptId+" : "+attr.value.term
										, "size" : 6
										, "type" : "attr"
									};
								attrsData.children.push(data);
							});
							classData["children"] = attrsData;
						}
						equalData.children.push(classData);
					}
				});
				vData.children.push(equalData);
			}
			
			if(angular.isDefined(classes.superClasses) && classes.superClasses != null) {
				var superData = {"name" : "Super Classes", "size" : 24, "type" : "super", "children" : []};
				angular.forEach(classes.superClasses, function(clazz, key){
					var classData = {"name" : clazz.conceptId+" : "+clazz.term, "size" : 12, "type" : "super"};
					superData.children.push(classData);
				});
				vData.children.push(superData);
			}
			
			if(angular.isDefined(classes.subClasses) && classes.subClasses != null) {
				var subData = {"name" : "Sub Classes", "size" : 24, "type" : "sub", "children" : []};
				angular.forEach(classes.subClasses, function(clazz, key){
					var classData = {"name" : clazz.conceptId+" : "+clazz.term, "size" : 12, "type" : "sub"};
					subData.children.push(classData);
				});
				vData.children.push(subData);
			}
			
			if(angular.isDefined(classes.instances) && classes.instances != null) {
				var insData = {"name" : "Instances", "size" : 24, "type" : "ins", "children" : []};
				angular.forEach(classes.instances, function(clazz, key){
					var classData = {"name" : clazz.conceptId+" : "+clazz.term, "size" : 12, "type" : "ins"};
					insData.children.push(classData);
				});
				vData.children.push(insData);
			}
			//console.log(vData);
			$scope.visualization = vData;
			
		});
		
		function initTab2Ctrl() {
			$scope.visualization = undefined;
			$scope.inferredData = null;
			//$scope.cvHeight = 500;
		}
		
	}
})();