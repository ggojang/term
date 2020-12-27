(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.controller('LayoutController', LayoutController);

	LayoutController.$inject = ['$window', '$element', '$compile', '$log', '$stateParams', '$scope'];
	function LayoutController($window, $element, $compile, $log, $stateParams, $scope) {

		var vm = this;

		vm.toggleExpand = toggleExpand;
		vm.setExpanded = setExpanded;
		vm.setCollapsed = setCollapsed;
		vm.openSidebarAndTab = openSidebarAndTab;
		
		
		vm.toggleParentTab = toggleParentTab;
		vm.clsParentTab = '';
		
		vm.isExpanded = true;
		vm.asideSelectedIndex = 0;
		
		vm.parentTab = {
			isCollapsed: false,
			toggle: toggleParentTab
		};
		
		vm.childrenTab = {
			isCollapsed: false,
			toggle: toggleChildrenTab
		};

		var navEl = undefined;
		var w = angular.element($window);
		var height = 0;
		var rule;

		
		
		
		activate();

		
		function activate() {

			// 브라우저 버튼그룹 추가
			var navTpl = [
					'<!-- 열기/닫기/핀 툴 -->',
					'<div class="expand-collapse-tools" ng-class="layoutVm.isExpanded ? \'\':\'collapsed\'">',
					'<a class="expand-collapse-layer" ng-click="layoutVm.setCollapsed()"></a>',
					'</div>' ].join('');
			var linkFn = $compile(navTpl);
			var navHtml = linkFn($scope);

			navEl = $element.find('.main-content md-tabs-wrapper').append(navHtml);

			var sheet = createSheet();
			var ruleIdx = sheet.insertRule('.main-content > md-content, .main-nav > md-content, .main-sidebar > md-content {}');
			rule = sheet.cssRules.item(ruleIdx);

			angular.element($window).on('resize', windowResize).trigger('resize');
		};

		
		function windowResize($event) {

			height = w.height() - 50; // - 50 - 60 - 30;
			rule.style.height = height + 'px';
		};

		
		/**
		 * https://davidwalsh.name/add-rules-stylesheets
		 */
		function createSheet() {
			// Create the <style> tag
			var style = document.createElement("style");

			// Add a media (and/or media query) here if you'd like!
			// style.setAttribute("media", "screen")
			// style.setAttribute("media", "only screen and (max-width : 1024px)")

			// WebKit hack :(
			style.appendChild(document.createTextNode(""));

			// Add the <style> element to the page
			document.head.appendChild(style);

			return style.sheet;
		};
		
		
		function toggleExpand() {
			vm.isExpanded = !vm.isExpanded;

		};
		
		function setExpanded() {
			vm.isExpanded = true;
		};
		
		function setCollapsed() {
			vm.isExpanded = false;
		};

		function openSidebarAndTab(index) {
			vm.asideSelectedIndex = index;
			vm.isExpanded = true;
		};
		
		
		function toggleParentTab() {
			vm.parentTab.isCollapsed = !vm.parentTab.isCollapsed;
		}
		
		function toggleChildrenTab() {
			vm.childrenTab.isCollapsed = !vm.childrenTab.isCollapsed;
		}

		
		// 하위 BrowserConceptController로부터 받을 메시지
		$scope.$on('selectRelationshipGroup', function(event) {
			// aside영역의 탭을 Qualifier 보기로 설정
			vm.asideSelectedIndex = 1;
			vm.isExpanded = true;
		});
		
		// 하위 BrowserConceptController로부터 받을 메시지
		$scope.$on('openParentChildTab', function(event) {
			// aside영역의 탭을 Qualifier 보기로 설정
			vm.asideSelectedIndex = 0;
		});
	};
	
})();