(function() {
  'use strict';

  angular
    .module('app.icd10Browser', [  
      /* 3rd-party modules */
      'ui.router',
      'ngMaterial',
      'ui.bootstrap',
      'angular.filter',
      'ngtimeago',
      'app'
  ]);
  
  angular
    .module('app.icd10Browser')
    .config(icd10BrowserConfig);

  function icd10BrowserConfig($urlRouterProvider, $stateProvider) {

    $stateProvider
      .state(
		  'icd10Browser', 
		  {
			  url: '/icd10',
			  templateUrl: 'resources/template/icd10Browser/layout.html',
			  controller: ['$window', '$log', '$scope', 'icd10browserservice', '$state', '$localstorage'
			               , function ($window, $log, $scope, browserSvc, $state, $localstorage) {
				  $log.debug('<Page Load> ICD10 Browser ');
		  
				  var vm = this;
				  
				  var w = angular.element($window);
				  var height = 0;
				  var rule;
				  
				  // localstorage 세팅, 20200428 by Yu
				  var icd10Code = $localstorage.get('oldIcd10Code');
					
				  //console.log('------------------------', icd10Code);	
					
				  if (icd10Code != null) {
					  $state.go('icd10Browser.code', {code:icd10Code});
				  } 
				  
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

						// Add a media (and/or media query) here if you'd like!
						// style.setAttribute("media", "screen")
						// style.setAttribute("media", "only screen and (max-width : 1024px)")

						// WebKit hack :(
						style.appendChild(document.createTextNode(""));

						// Add the <style> element to the page
						document.head.appendChild(style);

						return style.sheet;
					};
			  }]
		  }
	  )
	  .state(
		  'icd10Browser.code',
		  {
			  url: '/:code',
			  controller: 'CodeController',
			  controllerAs: 'eVm',
			  templateUrl: 'resources/template/icd10Browser/code.html'
		  }
	  );
  };
  
  icd10BrowserConfig.$inject = ['$urlRouterProvider', '$stateProvider'];
  
})();
