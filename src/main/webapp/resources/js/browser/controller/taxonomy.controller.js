(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.controller('BrowserTaxonomyController', BrowserTaxonomyController);
	
	BrowserTaxonomyController.$inject = ['$scope', 'browserservice', 'apiservice', 'userservice', 'utilservice'];
	function BrowserTaxonomyController($scope, browserservice, apiservice, userservice, utilservice) {
		
		var vm = this;
		
		vm.getChildren = getChildren;
		
		vm.getDefinitionStatusFlagValue = utilservice.getDefinitionStatusFlagValue;
		vm.getDefinitionStatusName = utilservice.getDefinitionStatusName;
		vm.termBeautifier = utilservice.termBeautifier;
		
		vm.tree = [];
		
		vm.loading = false;
		
		var rootConcept = {};
		
		_activate();
		
		function _activate() {
			vm.loading = true;
			apiservice.getConceptAndChildren('138875005', userservice.effectiveTime)
			.then(function (data) {
				var r = data[0],
					c = data[1];
				
				rootConcept = r;
				rootConcept.children = c;
				rootConcept.open = true;
				
				vm.tree.push(rootConcept);
				vm.tree.totalCount = r.descendantCount + 1;
			})
			.finally(function () {
				vm.loading = false;
			});
		};
		
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
		
		/*
		$scope.$watch(function () {
			return browserservice.component.children;
		}, function (children, oldChildren) {
			vm.children = children;
		});
		*/
	};
	
})();