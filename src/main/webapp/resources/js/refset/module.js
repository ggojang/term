(function() {
	'use strict';

	angular
		.module('app.referenceset', [
		    
		    /* 3rd-party modules */
		    'ui.router',
		    'ngMaterial',
		    'ui.bootstrap',
		    'angular.filter',
		    'jsTree.directive'
	]);
	
	angular
		.module('app.referenceset')
		.config(ReferencesetConfig);

	ReferencesetConfig.$inject = ['$urlRouterProvider', '$stateProvider', '$locationProvider'];
	function ReferencesetConfig($urlRouterProvider, $stateProvider, $locationProvider) {
		$stateProvider
			.state('refset', {
				url : '/refset',
				templateUrl : 'resources/template/refset/layout.html'
			})

			.state(
				'refset.view',
				{
					url: '/:code',
					params: {name: null, release: null},
					controller: 'MemberController',
					controllerAs: 'm',
					templateUrl: 'resources/template/refset/member.html'
				}
			);
		
		console.log($locationProvider);
	};
	
})();