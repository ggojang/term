(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.directive('expression', expression);
	
	expression.$inject = ['$compile', 'utilservice', 'browserservice'];
	function expression($compile, utilservice, browserservice) {
		var directive = {
			restrict: 'E',
			scope: {
				relationships: '='
			},
			link: link
		};
		return directive;

		function link(scope, element, attributes) {

			var e_openCurlyBraces = '<span class="exp-brackets">{</span>',
			e_closeCurlyBraces = '<span class="exp-brackets">}</span>',
			e_colon = '<span class="exp-operators">:</span>',
			e_plus = '<span class="exp-operators">+</span>',
			e_equals = '<span class="exp-operators">=</span>',
			e_pipe = '<span class="exp-pipes">|</span>';

			function referenceToExpression(obj) {
				return '<span class="exp-id">' + obj.conceptId + '</span>' + e_pipe + '<span class="exp-term">' + obj.term + '</span>' + e_pipe;
			};
 
			function conceptToPostCoordinatedExpression(concept, relationships) {
				var html = '<div>';

				if (relationships && relationships.length > 0) {
					//element.css('border', '1px dotted gray');

					//html += '<span>' + (concept.definitionStatus.id === '900000000000073002' ? '===' : '<<<') + '</span>';
					var firstParent = true;
					var attributes = {};

					angular.forEach(relationships, function (relationship, i) {
						if (relationship.active && relationship.type.conceptId == '116680003') {
							if (!firstParent) {
								html += e_plus;
							}

							html += referenceToExpression(relationship.destination);
							firstParent = false;
							
						} else if (relationship.active && relationship.type.conceptId != '116680003') {
							if (!attributes[relationship.relationshipGroup]) {
								attributes[relationship.relationshipGroup] = [];
							}

							attributes[relationship.relationshipGroup].push(relationship);
						}
					});

					var groups = Object.keys(attributes);
					if (groups.length > 0) {
						html += ' ' + e_colon;
					}

					html += '<div class="exp-padding">';

					angular.forEach(groups, function (group, i) {

						var firstInGroup = true;
						var divStart = false;

						if (i != 0) {
							html += '<div>';
						}

						angular.forEach(attributes[group], function(rel, i) {
							if (!firstInGroup) {
								html += ',';
								html += '<div>';
								divStart = true;
							}

							if (firstInGroup && group > 0) {
								if (divStart) html += '</div>';
								html += e_openCurlyBraces + " ";
							} else if (group > 0) {
								html += '&nbsp;&nbsp;';
							}

							firstInGroup = false;
							html += referenceToExpression(rel.type) + " " + e_equals + " " + referenceToExpression(rel.destination);


						});
						if (group != '0') {
							html += ' ' + e_closeCurlyBraces;
						}

						//if (group != groups[groups.length - 1]) html += ',';

						if (i != 0) {
							html += '</div>';
						}
					});
					html += '</div>';
				} else {
					element.css('border', 'none');
				}

				html += '</div>';
				
				utilservice.safeApply(scope, function () {
					element.find('.expression-code').remove();
					
					var el = $compile(html)(scope);
					el.addClass('expression-code');
					element.append(el);
				});
			};
			
			scope.$watchCollection('relationships', function (r) {
				var relationships = angular.copy(r);
				if (typeof relationships === 'undefined' || relationships == null) {
					relationships = [];
				}
				
				conceptToPostCoordinatedExpression(browserservice.context.concept, relationships);
			});
		};
	};
	
})();