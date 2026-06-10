(function () {
	
	'use strict';
	
	//$('body').height(window.innerHeight);
	
	// manual: angular.bootstrap(element, [modules], [config]);

    // 이 부분은 명시된 모든 디펜던시들이 다 로드된 뒤에 호출된다.
    // (주의) 디펜던시 로드 완료 시점이 페이지가 완전히 로드되기 전 일수도 있다.
	
	angular
		.module('app', [
		                
		         /* Angular modules */
		         'ngSanitize',
		         
		         /* 3rd-party modules */
		         'ui.router',
		         'ngMaterial',
				 
		         /* Shared modules */
		         'app.common',
		         'app.snomedctBrowser',
		         'app.loincBrowser',
		         'app.icd10Browser',
		         'app.mapBrowser'
		])
	    .factory('$localstorage', ['$window', function($window) {
	    	$window.localStorage['oldLoincVer']= '';
	    	$window.localStorage['oldLoincCode']= '';
	    	$window.localStorage['oldIcd10Code']= '';
	    	$window.localStorage['oldSnomedConcept']= '';
	    	return {
	    		set: function(key, value) {
	    			$window.localStorage[key] = value;
	    		},
	    		get: function(key, defaultValue) {
	    			return $window.localStorage[key] || defaultValue;
	    		}
	    	}
	    }]);
	
	angular
		.module('app')
		.config(AppConfig)
	
	;
	
	/**
	 * @class AppConfig
	 * @classdesc App Configuration
	 * @ngInject
	 */
	function AppConfig($stateProvider, $urlRouterProvider, $locationProvider) {
		
		$urlRouterProvider
			.otherwise('/license')
			;
		
		$stateProvider
			.state('license', {
				url: '/license',
				controller: LicenseController
			})
			;
			
		
		// use the HTML5 History API
		//$locationProvider.html5Mode(true);
		
		function LicenseController($state, $scope, $mdDialog) {
			function DialogController($scope, $mdDialog, $window) {
			  $scope.hide = function() {
			    $mdDialog.hide();
			  };
			  $scope.cancel = function() {
			    $window.open('http://infoclinic.co','_self');
			  };
			  $scope.answer = function(answer) {

			    if (answer == "Accept") {
			    	$mdDialog.hide(answer);
				$window.onbeforeunload = function(e) { return "";};
			    }
			  };
			};

			$scope.showAdvanced = function() {
				$mdDialog.show({
					controller: DialogController,
					templateUrl: 'resources/template/dialogLicense.html',
					parent: angular.element(document.body),
					clickOutsideToClose:false,
					fullscreen: true
				})
				.then(function(answer) {
					$state.go(
						'snomedctBrowser.concept', {conceptId:'138875005'}
					);
				}, function() {
					//reject
				});
			};

			$scope.showAdvanced();
		};
		LicenseController.$inject = ['$state', '$scope', '$mdDialog'];
	};
	AppConfig.$inject = ['$stateProvider', '$urlRouterProvider', '$locationProvider'];
	
	
	angular
		.module('app')
		.run(AppRun);
	
	/**
	 * @class App Run
	 */
	function AppRun($rootScope, $state, $stateParams, $location, $window) {
		
		$rootScope.$state = $state;
		$rootScope.$stateParams = $stateParams;

		$rootScope
		.$on('$stateChangeSuccess',
		function(event) {

			if (!$window.ga)
			return;
			$window.ga('send', 'pageview', { page: $location.path() });
		});
		
		$rootScope.disabledNavStyle = {
			'pointer-events': 'none',
			'opacity':'0.3'
		};
	};
	AppRun.$inject = ['$rootScope', '$state', '$stateParams', '$location', '$window'];
	
	
	
	// baseUrl 셋팅: http://host:port/pathname
	var apiUrl = window.location.origin, locationPath = window.location.pathname;
	if (locationPath && locationPath.length > 0 && locationPath !== '/') {
		// locationPath가 있다면 /term/ 처럼 가져온다.
		// '/'가 있다면 제거 후 앞에 '/'를 붙여서 baseUrl에 더 할 수 있도록 한다.
		// g: 발생할 모든 pattern에 대한 전역 검색 
		// i: 대/소문자 구분 안함
		// m: 여러줄 검색
		locationPath = '/' + locationPath.replace(/\//g, '');
		apiUrl += locationPath;
	}
	
	angular
		.module('app')
		.constant('CONFIG', {
			appName: 'Terminology Service',
			//appVersion: '0.1',
			//apiUrl: apiUrl
			apiUrl: 'http://api.infoclinic.co'
		});
	
	// Bootstrap
	angular
		.bootstrap(document.documentElement, ['app']);
})();
