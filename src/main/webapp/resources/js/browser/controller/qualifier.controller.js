(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.controller('BrowserQualifierController', BrowserQualifierController);

	BrowserQualifierController.$inject = ['$scope', 'browserservice', 'userservice'];
	function BrowserQualifierController($scope, browserservice, userservice) {
		var vm = this;
		
		vm.getRangeValue = getRangeValue;
		vm.toggleQualifier = toggleQualifier;
		
		vm.qualifiers = [];
		vm.activeRangeId = '';
		
		vm.loading = false;
		
		activate();
		
		function activate() {
			vm.qualifiers = browserservice.getQualifierList();
		};
		
		function getRangeValue(conceptId) {
			vm.activeRangeId = conceptId;
			
			browserservice.getChildren(conceptId, userservice.effectiveTime);
		};
		
		
		function toggleQualifier(range) {
			range.show = !range.show;
		};
		
		
		$scope.$watch(function () {
			return browserservice.loading.qualifiers;
		}, function (loading) {
			vm.loading = loading;
		});
		
	};
	
})();