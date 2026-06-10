(function () {
	
	'use strict';
	
	angular
		.module('app.loincBrowser')
		.controller('PanelController', PanelController);
	
	PanelController.$inject = ['$scope', '$element', 'dataservice', 'entityservice', '$timeout'];
	function PanelController($scope, $element, dataSvc, entitySvc, $timeout) {
		
		// ----------------------------------------
		// Local variable
		// ----------------------------------------
		var vm = this;
		
		var ctx = {
			ver: 'v2.67'
		};
		
		vm.panel = {};
		vm.memberOfThesePanels = [];
		vm.loading = false;
		vm.isPanelType = false;
		
		
		
		// ----------------------------------------
		// Init
		// ----------------------------------------
		activate();
		
		
		// ----------------------------------------
		// Methods
		// ----------------------------------------
		
		/**
		 * @name activate
		 * @desc Entry Point
		 */
		function activate() {

		};
		
		
		function getPanel(code, ver) {
			vm.loading = true;
			vm.memberOfThesePanels.length = 0;
			goScrollTop();
			
			dataSvc.getPanel(code, ver)
			.then(function (p) {
				vm.panel = p.panel;
				Array.prototype.push.apply(vm.memberOfThesePanels, p.memberOfThesePanels);
			})
			.finally(function () {
				vm.loading = false;
			});
		};
		
		
		$scope.$watch(function () {
			return entitySvc.getSelectedCode();
		}, function (code) {
			var panelType = entitySvc.getPanelType();
			if ((angular.isDefined(code) && code.length > 0) && (angular.isDefined(panelType) && panelType.length >0)) {
				vm.isPanelType = true;
				getPanel(code, ctx.ver);
			} else {
				vm.isPanelType = false;
				vm.panel = {};
				vm.memberOfThesePanels.length = 0;
			}
		});
		
		function goScrollTop() {
			$element.scrollTop('0');
		};
	}
}());
