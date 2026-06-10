/**
 * Browser
 */

(function() {
	'use strict';
	
	angular
		.module('app.browser' , ['ui.router','ngMaterial']);
	
	angular
		.module('app.browser')
		.config(BrowserConfig);
	
	/**
	 * @name BrowserConfig
	 * @param $stateProvider
	 * @returns
	 */
	function BrowserConfig($stateProvider) {
		
		$stateProvider
			.state('test', {
				url: '/b',
				templateUrl: 'resources/template/browser/layout.html',
				controller: 'BrowserLayoutController',
				controllerAs: 'layoutVm'
			});
	};
})();