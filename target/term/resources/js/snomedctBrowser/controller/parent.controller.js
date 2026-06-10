(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.controller('BrowserParentController', BrowserParentController);
	
	BrowserParentController.$inject = ['$scope', 'browserservice', 'apiservice', 'userservice', 'utilservice'];
	function BrowserParentController($scope, browserservice, apiservice, userservice, utilservice) {
		
		var vm = this;
		
		vm.getDefinitionStatusFlagValue = utilservice.getDefinitionStatusFlagValue;
		vm.getDefinitionStatusName = utilservice.getDefinitionStatusName;
		vm.termBeautifier = utilservice.termBeautifier;
		
		vm.getParentList = getParentList;
		
		vm.parents = [];
		vm.loading = false;
		
		
		var ROOT = '138875005';
		
		
		
		activate();
		
		
		
		function activate() {}
		
		
		
		function getParentList(node) {
			if (node.conceptId == ROOT) {
				return;
			}
			
			node.open = !node.open;
			
			if (angular.isDefined(node.parents)) {
				return;
			}
			
			node.loading = true;
			
			apiservice.getParentList(node.conceptId, userservice.effectiveTime)
			.then(function (parents) {
				node.parents = parents;
			})
			.finally(function () {
				node.loading = false;
			});
		};
		
		
		
		$scope.$watch(function () {
			return browserservice.loading.parents;
		}, function (loading, oldLoading) {
			vm.loading = loading;
		});
		
		
		
		$scope.$watchCollection(function () {
			return browserservice.context.parents;
		}, function (parents) {
			vm.parents = parents;
		});
	};
	
})();