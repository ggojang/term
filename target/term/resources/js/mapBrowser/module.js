(function() {
  'use strict';

  angular
    .module('app.mapBrowser', [  
      /* 3rd-party modules */
      'ui.router',
      'ngMaterial',
      'ui.bootstrap',
      'angular.filter',
      'ngtimeago',
      'app'
  ]);
  
  angular
    .module('app.mapBrowser')
    .config(BrowserConfig);

  BrowserConfig.$inject = ['$urlRouterProvider', '$stateProvider'];
  function BrowserConfig($urlRouterProvider, $stateProvider) {

    $stateProvider
    .state( 'mapBrowser', {
		url: '/map',
		templateUrl: 'resources/template/mapBrowser/layout.html',
		controller: ['$window', '$log', '$stateParams', '$scope', '$state', '$localstorage'
		  , function ($window, $log, $stateParams, $scope, $state, $localstorage) {
			$log.debug('<Page Load> Map Browser ');
		  
			var vm = this;
				  
			var w = angular.element($window);
			var height = 0;
			var rule;
				  
			activate();
				  
			function activate() {
									  
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


				// WebKit hack :(
				style.appendChild(document.createTextNode(""));

				// Add the <style> element to the page
				document.head.appendChild(style);

				return style.sheet;
			};
		  }
		]
    });
  };
})();
