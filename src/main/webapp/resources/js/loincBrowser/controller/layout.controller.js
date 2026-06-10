(function () {
	
	'use strict';
	
	angular
		.module('app.loincBrowser')
		.controller('LayoutController', LayoutController);

	LayoutController.$inject = ['$window', '$log', '$stateParams', '$scope', 'lncbrowserservice'];
		  function LayoutController($window, $log, $stateParams, $scope, browserSvc) {
			$log.debug('<Page Load> Loinc Browser ' + $stateParams.lang);
		  
			var vm = this;
				  
			var w = angular.element($window);
			var height = 0;
			var rule;
				  
			activate();
				  
			function activate() {
					  
				browserSvc.setLanguage($stateParams.lang);
				$scope.lang = browserSvc.getLanguage();
					  
				var sheet = createSheet();
				var ruleIdx = sheet.insertRule('.lb-srch-vw {}');
				rule = sheet.cssRules.item(ruleIdx);
					  
				angular.element($window)
			          .on('resize', windowResize)
			          .trigger('resize');
			};
				  
			function windowResize($event) {
				// 사이즈 변경 대상
				// .lb-srch-vw
				height = w.height() - 175; // - 50(header) - 35(tab) - 60(searchbox) - 30(message);
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
		  }
	
	BrowserConfig.$inject = ['$urlRouterProvider', '$stateProvider'];
	
})();
