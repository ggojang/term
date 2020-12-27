(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.controller('BrowserChildrenController', BrowserChildrenController);

	BrowserChildrenController.$inject = ['$scope', '$timeout', 'browserservice', 'apiservice', 'utilservice', 'userservice'];
	function BrowserChildrenController($scope, $timeout, browserservice, apiservice, utilservice, userservice) {
		var vm = this;
		
		vm.children = [];
		vm.loading = false;
		
		vm.getChildren = getChildren;
		
		vm.getDefinitionStatusFlagValue = utilservice.getDefinitionStatusFlagValue;
		vm.getDefinitionStatusName = utilservice.getDefinitionStatusName;
		vm.termBeautifier = utilservice.termBeautifier;
		
		activate();
		
		function activate() {};
		
		function getChildren(node) {
			if (node.descendantCount === 0) {
				return;
			}
			
			node.open = !node.open;
			
			if (angular.isDefined(node.children)) {
				return;
			}
			
			node.loading = true;
			
			apiservice.getChildren(node.conceptId, userservice.effectiveTime)
			.then(function (children) {
				node.children = children;
			})
			.finally(function () {
				node.loading = false;
			});
		};
		
		$scope.$watch(function () {
			return browserservice.loading.children;
		}, function (loading, oldLoading) {
			vm.loading = loading;
		});
		
		$scope.$watchCollection(function () {
			return browserservice.context.children;
		}, function (children) {
			vm.children = children;
		});
	};
	
})();
