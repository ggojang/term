/**
 * Concept Editor common dialog
 */
(function() {
	'use strict';
	
	angular.module('app.concept').controller('ConceptDialogController', ConceptDialogController);
	
	ConceptDialogController.$inject = ['$scope', '$mdDialog', 'apiservice', '$timeout', 'browser.description',
	                                   'utilservice', 'browser.langrefset', 'conceptConstant', 'userservice'];
	function ConceptDialogController($scope, $mdDialog, apiservice, $timeout, descriptionConstant, utilservice, langrefset, conceptConstant, userservice) {
		$scope.descriptionConstant = descriptionConstant;
		$scope.selectedConcept = null;
		
		$scope.dialogLoaded = false;
		$scope.btnDisabledPrev = true;
		$scope.btnDisabledNext = true;
		
		$scope.autocompleteShow = false;
		$scope.searchList = false;
		$scope.searchInit = function() {
			$scope.autocompleteShow = false;
			$scope.searchList = false;
			$scope.btnDisabledPrev = true;
			$scope.btnDisabledNext = true;
			$scope.total = 0;
			$scope.totalPage = 0;
			$scope.page = 1;
			$scope.keyword = '';
			$scope.keywordClass = '';
		};
		
		$scope.selectedRow = function(concept) {
			$scope.searchList = false;
			$scope.selectedConcept = concept;
			getConceptDetail(concept.conceptId);
		};
		
		$scope.descriptionList = [];
		$scope.diagram = false;
		function getConceptDetail(conceptId) {
			$scope.dialogLoaded = true;
			apiservice.getComponent(conceptId, userservice.effectiveTime, true)
			.then(function (component) {
				$scope.descriptionList = sortByDescription(component[1]);
				$scope.source = {
						concept : component[0],
						descriptions : component[1],
						relationships : component[2],
						referencesets : component[3]
				};
				$scope.diagram = true;
				//console.log($scope.descriptionList);
			}, function(error) {
				
			})
			.finally(function () {
				$scope.dialogLoaded = false;
			});
		}
		
		function sortByDescription(descriptions) {//utilservice langrefset
			var group = [];
			angular.forEach(descriptions, function(description, index) {
				var tempDescription = angular.copy(description);
				var languageReferenceset = description.languageReferencesetList[0];
				var priority = utilservice.getDescriptionPriority(
						tempDescription.typeId,
						languageReferenceset.acceptabilityId
					);
				tempDescription.priority = priority;
				var refsetId = languageReferenceset.refsetId;
				var isContain = _arrayObjectIndexOf(group, refsetId, 'id');
				if(isContain == -1) {
					var d = [];
					d.push(tempDescription);
					var lang = _getLanguageReferencesetProperty(refsetId, 'countryCode');
					//if(conceptConstant.svcLanguage.indexOf(lang) > -1){
						group.push({
							id: refsetId,
							lang: lang,
							descriptions: d,
						});
					//}	
						
				} else {
					angular.forEach(group, function(tempValue, tempKey) {
						if (tempValue.id === refsetId) {
							tempValue.descriptions.push(tempDescription);
						}
					});
				}
			});
			
			var sortGroup = [];
			angular.forEach(conceptConstant.svcLanguage, function(lang, key){
				angular.forEach(group, function(g, k){
					if(lang === g.lang) sortGroup.push(g);
				});
			});
			//console.log(JSON.stringify(sortGroup[1]));
			return sortGroup;
		}
		
		function _getLanguageReferencesetProperty(langrefsetId, property) {
			//return _getArrayObjectValue(LANGREFSET, 'id', langrefsetId, property);
			return _getArrayObjectValue(langrefset, 'id', langrefsetId, property);
		}
		
		function _getArrayObjectValue(array, property, term, returnProperty) {
			var value = null;
			for(var i = 0, len = array.length; i < len; i++) {
				if (array[i][property] === term) {
					value = array[i][returnProperty];
					break;
				}
			}
			return value;
		}
		
		function _arrayObjectIndexOf(array, term, property) {
			var index = -1;
			if (property != "") {
				for(var i = 0, len = array.length; i < len; i++) {
					if (array[i][property] === term) {
						index = i;
						break;
					}
				}
			} else {
				for(var i = 0, len = array.length; i < len; i++) {
					if (array[i] === term) {
						index = i;
						break;
					}
				}
			}

			return index;
		}
		
		/** Search S */
		$scope.total = 0;
		$scope.totalPage = 0;
		$scope.page = 1;
		$scope.keyword = '';
		$scope.keywordClass = '';//ce-autocomplete-error-input
		$scope.searchDatas = [];
		$scope.goSearch = function($event, page) {
			$timeout(function(){
				var seachWord = $scope.keyword.trim();
				if(seachWord === '' || seachWord.length < 3) {
					$scope.keywordClass = 'ce-autocomplete-error-input';
					$scope.keyword = $scope.errorMsg;
					return false;
				}	
				getData(seachWord, page);
			});
		};
		
		if(angular.isDefined(this.parentKeyword)) {
			$scope.keyword = this.parentKeyword;
			getData($scope.keyword, 1);
		}
		
		function getData(str, page) {
			$scope.dialogLoaded = true;
			var options = {
					q: str,
					match: 'partial',
					state: 'active'
				};
			apiservice.searchByWord(options, page, 20)
			.then(
					function(data) {
						//console.log(data);
						$scope.btnDisabledPrev = data.page.first ? true : false;
						$scope.btnDisabledNext = data.page.last ? true : false;
						if(data.page.totalElements > 0) {
							$scope.page = page;
							$scope.total = data.page.totalElements;
							$scope.totalPage = data.page.totalPages;
							$scope.autocompleteShow = true;
							$scope.searchList = true;
							var arr = [];
							angular.forEach(data.page.content, function(concept, key) {
								var clone = angular.copy(concept);
								var i = clone.definitionStatusId;
								var cType = {type:'',name:''};
								if (i.length == 19) {
									i = i.substring(0, i.length-1);
								}
								if(i === '900000000000074008') {
									cType.type = 'P'; cType.name = 'Primitive';
								} else if(i === '900000000000073002') {
									cType.type = 'F'; cType.name = 'Fully defined';
								}
								clone.cType = cType;
								this.push(clone);
							}, arr);
							$scope.searchDatas = arr;
						} else {
							$scope.total = 0;
							$scope.totalPage = 0;//warningMsg
							$scope.page = 1;
							$scope.keywordClass = 'ce-autocomplete-warn-input';
							$scope.keyword = $scope.warningMsg;
						}
					},
					function(error) {
						
					}
			)
			.finally(function () {
				$scope.dialogLoaded = false;
			});
		}
		/** Search E */
		
		$scope.okConcept = function() {
			$mdDialog.hide($scope.selectedConcept);
		};
		$scope.cancelConcept = function() {
			$mdDialog.cancel();
		};
	}
	
	angular.module('app.concept').controller('FcpDialogController', FcpDialogController); //focus concept parents
	FcpDialogController.$inject = ['$scope', '$mdDialog', 'apiservice', '$timeout', 'browser.description',
	                                   'utilservice', 'browser.langrefset', 'conceptConstant', 'userservice'];
	
	function FcpDialogController($scope, $mdDialog, apiservice, $timeout, descriptionConstant, utilservice, langrefset, conceptConstant, userservice) {
		
		$scope.dialogLoaded = false;
		$scope.statedConcepts = [];
		$scope.inferredConcepts = [];
		if(angular.isDefined(this.fcId)) {
			var fcId = this.fcId;
			//var isStated = true; //true=>stated or false=> Inferred
			$timeout(function(){
				$scope.dialogLoaded = true;
				apiservice.getComponent(fcId, userservice.effectiveTime, true)
				.then(function (statedData) {
					apiservice.getComponent(fcId, userservice.effectiveTime, false)
					.then(function(inferredData){
						$scope.statedConcepts = convRelations(statedData[2], 1);
						$scope.inferredConcepts = convRelations(inferredData[2], 1);
					},
					function(error){
					})
					.finally(function () {
						$scope.dialogLoaded = false;
					});
				}, function(error) {
					$scope.dialogLoaded = false;
				});
			},100);
		}
		
		$scope.ok = function() {
			$mdDialog.hide();
		};
		
		function convRelations (relationships, type) {
			var arr = [];
			angular.forEach(relationships, function(relationship, key) {
				var clone = angular.copy(relationship);
				var priority = clone.type.conceptId === '116680003' ? 1 : 2;
				clone.priority = priority;
				
				var cType = {type:'',name:''};
				if(clone.destination.definitionStatus.id != null) {
					var i = clone.destination.definitionStatus.id;
					if (i.length == 19) {
						i = i.substring(0, i.length-1);
					}
					if(i === '900000000000074008') {
						cType.type = 'P'; cType.name = 'Primitive';
					} else if(i === '900000000000073002') {
						cType.type = 'F'; cType.name = 'Fully defined';
					}
				}
				clone.cType = cType;
				clone.checkClass = '';
				if(clone.priority == type) this.push(clone);
				//this.push(clone);
			}, arr);
			return arr;
		}
		
	}
	
})();