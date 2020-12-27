(function() {
  'use strict';

  angular
    .module('app.loincBrowser', [  
      /* 3rd-party modules */
      'ui.router',
      'ngMaterial',
      'ui.bootstrap',
      'angular.filter',
      'ngtimeago',
      'app'
  ]);
  
  angular
    .module('app.loincBrowser')
    .config(BrowserConfig);

  function BrowserConfig($urlRouterProvider, $stateProvider) {

    $stateProvider
	.state( 'loincBrowser', {
		url: '/loinc/{lang:(?:en-US|ko-KR|zh-CN)}',
		templateUrl: 'resources/template/loincBrowser/layout.html',
		controller: ['$window', '$log', '$stateParams', '$scope', 'lncbrowserservice', '$state', '$localstorage'
		  , function ($window, $log, $stateParams, $scope, browserSvc, $state, $localstorage) {
			$log.debug('<Page Load> Loinc Browser ' + $stateParams.lang);
		  
			var vm = this;
				  
			var w = angular.element($window);
			var height = 0;
			var rule;
				  
			/* localstorage 세팅 */
			
			var loincVer = $localstorage.get('oldLoincVer');
			var loincCode = $localstorage.get('oldLoincCode');
			
			//console.log('------------------------', loincVer);
			//console.log('------------------------', loincCode);
			
			
			if (loincCode != null) {
				$state.go('loincBrowser.entity', {ver:loincVer, code:loincCode});
			} 
			
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
		]
	/*
		,resolve : {
			codeView : ['$localstorage', function($state, $localstorage) {
				var loincVer = $localstorage.get('oldLoincVer');
				var loincCode = $localstorage.get('oldLoincCode');
			
				console.log('------------------------', loincVer);
				console.log('------------------------', loincCode);
			
			
				if (loincCode != '')
					$state.go('loincBrowser.entity', {ver:loincVer, code:loincCode});
			}
		]}
		*/
	})
	.state('loincBrowser.entity', {
		url: '/:ver/:code',
		controller: 'EntityController',
		controllerAs: 'eVm',
		templateUrl: 'resources/template/loincBrowser/entity.html'
			
	});

  };
  
  BrowserConfig.$inject = ['$urlRouterProvider', '$stateProvider'];
  
})();
