(function () {
	
	'use strict';
	
	angular
		.module('app.common')
		.directive('locale', locale);
	
	locale.$inject = ['LocaleService', 'amMoment'];
	function locale(LocaleService, amMoment) {
		var directive = {
			restrict: 'EA',
			templateUrl: 'resources/template/translate.html',
			controller: controller 
		};
		
		controller.$inject = ['$scope', '$mdDialog'];
		function controller($scope, $mdDialog) {
			$scope.isOpen = false;
        	LocaleService.setLocaleByDisplayName("한국어");

	        $scope.currentLocaleDisplayName = LocaleService.getLocaleDisplayName();
	        $scope.localesDisplayNames = LocaleService.getLocalesDisplayNames();
	        $scope.visible = $scope.localesDisplayNames &&
	        $scope.localesDisplayNames.length > 1;
	
	        var originatorEv;
	        this.openMenu = function($mdOpenMenu, ev) {
					      originatorEv = ev;
					      $mdOpenMenu(ev);
					    };
	
	        $scope.changeLanguage = function (locale) {
	            LocaleService.setLocaleByDisplayName(locale);
	            $scope.currentLocaleDisplayName = locale;
	            $scope.isOpen = false;
	            //console.log(LocaleService.getLocale(locale).substring(0,2));
	            
	            amMoment.changeLocale(LocaleService.getLocale(locale).substring(0,2));
	        };
	
	        $scope.flag = function (locale) {
	        	var lc = LocaleService.getLocale(locale).toLowerCase();
	        	return 'flag-icon-' + lc.substr(lc.length - 2);
	        };
	
	        $scope.click = function () {
	        	$scope.isOpen = !$scope.isOpen;
	        };
		};
		return directive;
	};

})();
