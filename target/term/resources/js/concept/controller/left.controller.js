/**
 * Concept Editor on concept hierarchy tree
 */
(function() {
	'use strict';
	
	angular.module('app.concept').controller('ConceptLeftController', ConceptLeftController);
	
	ConceptLeftController.$inject = ['$scope', '$element', 'apiservice', '$mdDialog','$state'];
	function ConceptLeftController($scope, $element, apiservice, $mdDialog, $state) {
		
		$scope.CHT = [];
		$scope.effectiveTime = '';
		$scope.$on('initCHT', function($event, conceptId, effectiveTime, resultData){
			var rootConcept = {
					active: true,
					conceptId: conceptId,
					term: 'SNOMED CT Concept',
					semanticTag: 'SNOMED RT+CTV3',
					definitionStatus: 'Primitive',
					childrenCount: 19,
					descendantCount: 321900
				};
			rootConcept.children = resultData;
			rootConcept.open = true;
			$scope.CHT.push(rootConcept);
			//console.log($scope.CHT);
			$scope.effectiveTime = effectiveTime;
		});
		
		$scope.goSearch = function($event) {
			var seachWord = $scope.chtSrchWord.trim();
			if(seachWord === '' || seachWord.length < 3) {
				$scope.keywordClass = 'ce-input-error';
				$scope.chtSrchWord = $scope.errorMsg;
				$element.find('span.input-group-btn > button').focus();
				return false;
			}
			$mdDialog.show({
				locals: {parentKeyword:seachWord},
				controller: 'ConceptDialogController',
				bindToController: true,
				templateUrl: 'resources/template/routes/concept/dialogConcept.html',
				parent: angular.element(document.body),
				targetEvent: $event,
				clickOutsideToClose:true,
				fullscreen: true
			})
			.then(
					function(result) {
						if(result != null && result.conceptId != undefined) {
							$state.go('concept.modify', {editMode:'child',targetId:result.conceptId});
						}
					},
					function() {
						//$scope.focusConcept[index].checkClass = '';
					}
			);
		};
		
		$scope.getChildren = function(node) {
			if (node.descendantCount == 0) {
				return;
			}
			node.open = !node.open;
			if (angular.isDefined(node.children)) {
				return;
			}
			node.loading = true;
			apiservice.getChildren(node.conceptId, $scope.effectiveTime) //conceptId, effectivetime
			.then(function(data) {
				node.children = data;
			}, function(error){	
			})
			.finally(function () {
				node.loading = false;
			});
		};
		
	}
})();