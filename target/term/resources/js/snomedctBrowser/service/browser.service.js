(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.service('browserservice', BrowserService);
	
	BrowserService.$inject = ['$log', '$q', '$timeout', 'apiservice', 'utilservice', 'userservice', '$localstorage'];
	function BrowserService($log, $q, $timeout, apiservice, utilservice, userservice, $localstorage) {

		var service = this;

		service.setConceptId = setConceptId;
		
		service.getComponent = getComponent;
		service.getRelationshipAndDefiningAttributeList = getRelationshipAndDefiningAttributeList;
		service.getChildren = getChildren;
		service.getParentList = getParentList;
		service.initComponent = initComponent;
		
		service.setFocusRelationshipGroup = setFocusRelationshipGroup;
		service.getQualifierList = getQualifierList;
		
		service.conceptId = undefined;
		service.isStated = false;
		
		service.context = {
			concept: {},
			descriptions: [],
			relationships: [],
			qualifiers: [],
			referencesets: [],
			histories: {
				concept: [],
				descriptions: []
			},
			parents: [],
			children: []
		};
		service.loading = {
			component: false,
			parents: false,
			qualifiers: false,
		};
		
		var focusQualifiers = [];
		

		
		/* ---------------------------------------- */
		/* Implements */
		/* ---------------------------------------- */
		
		function setConceptId(conceptId) {
			//local storage setting
			$localstorage.set('oldSnomedConcept', conceptId);
			console.log("oldSnomedConcept : " + $localstorage.get('oldSnomedConcept'));
			
			// concept, descriptions, relationships, referencesets, histories, qualifiers
			getComponent(conceptId, userservice.effectiveTime, service.isStated);
			
			// children
			getChildren(conceptId, userservice.effectiveTime);
			
			// parents
			getParentList(conceptId, userservice.effectiveTime);
		}
		
		
		function getComponent(conceptId, effectiveTime, isStated) {
			$log.debug('getComponent', conceptId);

			if (angular.isDefined(conceptId)) {
				service.loading.component = true;
				service.loading.qualifiers = true;
				initComponent();
				apiservice.getComponent(conceptId, effectiveTime, isStated)
				.then(function (component) {
					var histories = component[4],
						conceptHistories = [],
						descriptionHistories = [];
					
					
					
					// concept
					service.context.concept = component[0];
					
					// descriptions
					Array.prototype.push.apply(service.context.descriptions, component[1]);
					
					// relationships
					Array.prototype.push.apply(service.context.relationships, component[2]);
					
					// referencesets
					Array.prototype.push.apply(service.context.referencesets, component[3]);
					
					// histories
					if (histories && histories.length > 0) {
						angular.forEach(histories, function (history, index) {
							if (angular.isDefined(history.descriptionId)) {
								descriptionHistories.push(history);
							} else {
								conceptHistories.push(history);
							}
						});
						
						Array.prototype.push.apply(service.context.histories.concept, conceptHistories);
						Array.prototype.push.apply(service.context.histories.descriptions, descriptionHistories);
					}
					
					Array.prototype.push.apply(service.context.qualifiers, component[5]);
					setFocusRelationshipGroup([]);
				}, function (error) {
					// deferred.reject(error);
				})
				.finally(function () {
					service.loading.component = false;
					service.loading.qualifiers = false;
				});
			}
			
		};
		
		
		function getRelationshipAndDefiningAttributeList(conceptId, effectiveTime, isStated) {
			// 콤포넌트 로딩 시작
			service.loading.component = true;
			service.loading.qualifiers = true;
			service.context.relationships.length = 0;
			service.context.qualifiers.length = 0;
			apiservice.getRelationshipAndDefiningAttributeList(conceptId, effectiveTime, isStated)
			.then(function (response) {
				Array.prototype.push.apply(service.context.relationships, response[0]);
				Array.prototype.push.apply(service.context.qualifiers, response[1]);
				
				setFocusRelationshipGroup([]);
			})
			.finally(function () {
				// 콤포넌트 로딩 종료
				service.loading.component = false;
				service.loading.qualifiers = false;
			});
		};
		
		
		function getChildren(conceptId, effectiveTime) {
			if (!conceptId) {
				return;
			}
			
			service.loading.children = true;
			service.context.children.length = 0;
			apiservice.getChildren(conceptId, effectiveTime)
			.then(function (children) {
				Array.prototype.push.apply(service.context.children, children);
				//deferred.resolve(children);
			}, function (error) {
				//deferred.reject(error);
			})
			.finally(function () {
				service.loading.children = false;
			});
		};
		
		
		function getParentList(conceptId, effectiveTime) {
			if (!conceptId) {
				return;
			}
			
			service.loading.parents = true;
			service.context.parents.length = 0;
			apiservice.getParentList(conceptId, effectiveTime)
			.then(function (parents) {
				Array.prototype.push.apply(service.context.parents, parents);
				service.context.parents = parents;
			}, function (error) {
			//	deferred.reject(error);
			})
			.finally(function () {
				service.loading.parents = false;
			});
		};
		
		
		function initComponent() {
			service.context.concept = {};
			service.context.descriptions.length = 0;
			service.context.relationships.length = 0;
			service.context.qualifiers.length = 0;
			service.context.referencesets.length = 0;
			service.context.histories.concept.length = 0;
			service.context.histories.descriptions.length = 0;
		};
		
		
		function setFocusRelationshipGroup(relGroup) {
			// start loading
			service.loading.qualifiers = true;

			// init model length
			focusQualifiers.length = 0;
			Array.prototype.push.apply(focusQualifiers, bindQualifierList(relGroup, service.context.qualifiers));

			 $timeout(function () {
				 // end loading
				 service.loading. qualifiers = false;
			 }, 500);
		};
		
		
		function getQualifierList() {
			return focusQualifiers;
		};
		
		
		function bindQualifierList(relationships, qualifiers) {
			
			if (!qualifiers) {
				return [];
			}
			
			if (relationships.length === 0) {
				return qualifiers;
			}
			
			var nonIsa = [],
				simpleNonIsa = [],
				cloneQualifiers = angular.copy(qualifiers),
				isA = '116680003';
		
			angular.forEach(relationships, function(relationship, index) {
	            if (relationship.type.conceptId != isA && relationship.active) {
	                nonIsa.push(relationship);
	                simpleNonIsa.push(relationship.type.conceptId);
	            }
			});
			
			angular.forEach(cloneQualifiers, function (qualifier, key) {
	              var isContain = _arrayObjectIndexOf(simpleNonIsa, qualifier.id, "");
	              if (isContain != -1) {
	            	  qualifier.used = true;
	            	  qualifier.defref = nonIsa[isContain];
	              } else {
	            	  qualifier.used = false;
	              }
			});
			
			return cloneQualifiers;
		};
		
		
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
		};
		
	};
	
})();