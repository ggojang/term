(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.directive('relationship', relationship);
	
	relationship.$inject = ['$timeout'];
	function relationship($timeout) {
		var directive = {
			restrict: 'EA',
			bindToController: {
	          concept: '=',
	          relationships: '=',
	          select: '&' // function
			},
			controllerAs: 'rv',
			controller: controller,
			templateUrl: 'resources/template/snomedctBrowser/relationship.html'
		};
		
		
		controller.$inject = ['$scope', '$log', 'utilservice'];
		function controller($scope, $log, utilservice) {
			var vm = this;
			
			var ISA = "116680003";
			
			vm.isaRels = [];
			vm.defNonGroupRels = [];
			vm.defGroupRels = [];
			
			vm.selectedGroupNum = undefined;

			// Declare Util functions
			vm.termBeautifier = utilservice.termBeautifier;
			vm.getSemanticTag = utilservice.getSemanticTag;
			
			vm.getDefinitionStatusFlagValue = utilservice.getDefinitionStatusFlagValue;
			vm.getDefinitionStatusName = utilservice.getDefinitionStatusName;

			vm.onClick = onClick;
			vm.onChange = onChange;
			
			
			activate();
			
			function activate() {
				$log.debug('[Directive] Relationship: Activate');
			};
			
			
			/**
			 * @name splitByRelationshipType
			 * @desc Relationship Type에 따라 분리
			 * @param {Array} rels
			 */
			function splitByRelationshipType(rels) {
				// IS-A Relationships
				var isaRels = [];
				// Defining Non-Group Relationships
				var defNonGroupRels = [];
				// Defining Group Relationships
				var defGroupRels = [];

				angular.forEach(rels, function(rel, key) {
					var isIsa = checkIsa(rel.type.conceptId);
					if (isIsa) {
						isaRels.push(rel);
					} else {
						if (rel.relationshipGroup === "0") {
							defNonGroupRels.push(rel);
						} else {
							defGroupRels.push(rel);
						}
					}
				});
				
				vm.isaRels = isaRels;
				vm.defNonGroupRels = defNonGroupRels;
				vm.defGroupRels = bindByRelationshipGroup(defGroupRels);
			};

			
			/**
			 * @name checkIsa
			 * @desc type이 IS-A Relationship인지 확인
			 * @param {String} typeId Relationship Type Id
			 * @return {Boolean}
			 */
			function checkIsa(typeId) {
				return typeId === ISA ? true:false;
			};
			
			
			/**
			 * @name bindByRelationshipGroup
			 * @desc Relationship Group에 따라 분류
			 * @param {Array} rels
			 * @return {Array}
			 */
			function bindByRelationshipGroup(rels) {
				var newArr = [],
	                types = {},
	                key = 'relationshipGroup',
	                newItem, i, j, cur;
	            for (i = 0, j = rels.length; i < j; i++) {
	                cur = rels[i];
	                if (!(cur[key] in types)) {
	                    types[cur[key]] = { type: cur[key], data: [] };
	                    newArr.push(types[cur[key]]);
	                }
	                types[cur[key]].data.push(cur);
	            }
	            return newArr;
			};

			
			/**
			 * @name onClick
			 * @desc defining relationship에서 특정그룹에서의 클릭이벤트를 처리
			 * @param Object e event
			 */
			function onClick(e, selectGroupRel, ungroupRel) {
				var elem;
				if (e.srcElement) {
					// for ie
					elem = angular.element(e.srcElement);
				} else {
					// for most other browser
					elem = angular.element(e.target);
				}

				var relGroup = undefined;
				var isChkd = false;
				var inputElem = elem.closest('.sctb-rel-group').find('.rel-groupbox-checkstate > input');
				//var isRootElem = elem.hasClass('sctb-rel-group');

				relGroup = elem.closest('.sctb-rel-group').find('.rel-groupbox-checkstate > input').prop('value');
				isChkd = inputElem.prop('checked');
				if (!isChkd) {
					inputElem.prop('checked', true);
					vm.selectedGroupNum = relGroup;
					
					var rels = [];
					Array.prototype.push.apply(rels, selectGroupRel);
					
					if (ungroupRel.length > 0) {
						Array.prototype.push.apply(rels, ungroupRel);
					}
					
					onChange(rels, true);
				}
			};
			
			
			function onChange(relGroup, isChangeView) {
				vm.select({relGroup: relGroup, isChangeView: isChangeView});
			};
			

			$scope.$watchCollection('rv.relationships', function(r) {
				var rels = angular.copy(r);
				if (!angular.isDefined(rels)) {
					rels = [];
				}
				splitByRelationshipType(rels);
				
				if (vm.defNonGroupRels.length > 0) {
					onChange(vm.defNonGroupRels, false);
				}
				
				// 라디오 체크 초기화
				vm.selectedGroupNum = undefined;
			});
		};
		
		function link(scope, elem, attrs, ctrl) {
		};
		
		return directive;
	};
	
})();
