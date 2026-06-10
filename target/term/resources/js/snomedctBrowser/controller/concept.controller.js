(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.controller('BrowserConceptController', BrowserConceptController);
	
	BrowserConceptController.$inject = ['$scope', '$element', '$compile', '$timeout', '$mdDialog', 'browserservice', 'utilservice', 'userservice'];
	function BrowserConceptController($scope, $element, $compile, $timeout, $mdDialog, browserservice, utilservice, userservice) {
		
		var vm = this;
		
		vm.getDefinitionStatusFlagValue = utilservice.getDefinitionStatusFlagValue;
		vm.getDefinitionStatusName = utilservice.getDefinitionStatusName;
		vm.termBeautifier = utilservice.termBeautifier;
		vm.isUndefined = utilservice.isUndefined;
		vm.otherRefsetPredicate = _otherRefsetPredicate;
		vm.orderByAttributeOrder = _orderByAttributeOrder;
		vm.orderByMapPriority = _orderByMapPriority;
		vm.groupPredicate = _groupPredicate;
		
		
		vm.component = {};
		vm.isStated = false;
		vm.loading = false;
	
		var definedFormatIds = [
            		'900000000000480006',           // Attribute value type reference set
			'900000000000496009',           // Simple map type reference set
			'447250001',                    // Complex map type reference set
			'609331003',                    // Extended map type reference set
			'900000000000456007',           // Reference set descriptor reference set
			'900000000000521006',           // Association type reference set
			'900000000000538005',           // Description format reference set
		 	'705111002'                     // Map correlation and origin type reference set
		];
		
		activate();
		
		function activate() {
			
			// 브라우저 버튼그룹 추가
			var navTpl = ['<!-- Inferred/Stated View Buttons -->',
			              '<div class="sctb-overview-nav btn-group">',
			                 '<button type="button" class="btn btn-default btn-xs" ng-model="vm.isStated" uib-btn-radio="true">Stated</button>',
			                 '<button type="button" class="btn btn-default btn-xs" ng-model="vm.isStated" uib-btn-radio="false">Inferred</button>',
			               '</div>',
			               '<!-- Inferred/Stated View Buttons END-->'].join('');

			
			var linkFn = $compile(navTpl);
			var navHtml = linkFn($scope);
			
			$element.closest('md-tabs').find('md-tabs-wrapper > md-tabs-canvas').append(navHtml);
			
		};
		
		
		function _otherRefsetPredicate(refsetId) {
			var isFormat = false; 
			var index = definedFormatIds.indexOf(refsetId);
			
			// definedFormatIds에 refsetId가 없다면 true
			isFormat = index === -1 ? true:false;
			return isFormat;
		};
		
		function _orderByAttributeOrder(val) {
			return parseInt(val.extra['Attribute order'].id, 10);
		};
		
		function _orderByMapPriority(val) {
			return parseInt(val.extra['Map priority'].id, 10);
		};
		
		function _groupPredicate(val) {
			return parseInt(val.extra['Map group'].id, 10);
		};
		
		function goScrollTop() {
			$element.parent().scrollTop('0');
		}
		
		$scope.$watchCollection(function () {
			return browserservice.context;
		}, function (component) {
			vm.component = component;
		});
		
		$scope.$watch(function () {
			return browserservice.loading.component;
		}, function (loading) {
			$scope.$emit('openParentChildTab');
			
			vm.loading = loading;
			
			goScrollTop();
			
		});
		
		$scope.$watch(function () {
			return vm.isStated;
		}, function (isStated, oldIsStated) {
			if (isStated !== oldIsStated) {
				browserservice.getRelationshipAndDefiningAttributeList(vm.component.concept.conceptId, userservice.effectiveTime, isStated);
				browserservice.isStated = isStated;
			}
			
		});
		
		
		// ----------------------------------------
		// Relationship
		// ----------------------------------------
		
		vm.showDiagram = showDiagram;
		
		function showDiagram($event) {
			$mdDialog.show({
				locals: {
					source: browserservice.context
				},
				bindToController: true,
				controller: 'BrowserDiagramController',
				controllerAs: 'diagramVm',
				templateUrl: 'resources/template/snomedctBrowser/diagram.html',
				parent: angular.element(document.body),
				clickOutsideToClose: true,
				targetEvent: $event,
				fullscreen: true,
			});
		};
		
		// ----------------------------------------
		// Relationship END
		// ----------------------------------------
		
		
		// ----------------------------------------
		// MRCM Qualifier
		// ----------------------------------------
		
		vm.select = select;
		
		function select(relGroup, isChangeView) {
			browserservice.setFocusRelationshipGroup(relGroup);
			
			if (angular.isDefined(isChangeView) && isChangeView) {
				// BrowserController에 전달
				$scope.$emit('selectRelationshipGroup');
			}
		};
		
		// ----------------------------------------
		// MRCM Qualifier END
		// ----------------------------------------
	};
	
})();
