(function () {
	
	'use strict';
	
	angular
		.module('app.common',
				[
				 'ngMaterial',
				 'pascalprecht.translate',
				 'tmh.dynamicLocale',
				 'angularMoment', 
				 'app.common',
				 'react'
				 ]);
	
	angular
		.module('app.common')
		.config(CommonConfig);
	
	function CommonConfig($translateProvider, tmhDynamicLocaleProvider, LOCALE) {
		$translateProvider.useStaticFilesLoader({
			prefix: 'resources/i18n/locale-', // path to translations files
			suffix: '.json' // suffix, currently- expression of the translations
		});

		$translateProvider.preferredLanguage(LOCALE.preferredLocale); // is applied on first load
		// remember language
		//$translateProvider.useLocalStorage(); // saves selected language to localStorage
		tmhDynamicLocaleProvider.localeLocationPattern('resources/lib/angular-i18n/angular-locale-{{locale}}.js');

	};
	CommonConfig.$inject = ['$translateProvider', 'tmhDynamicLocaleProvider', 'common.locale'];


	angular
		.module('app.common')
		.directive('resize', ['$window', 'utilservice', function ($window, utilSvc) {
			return {
				link: link,
				restrict: 'A'
			};
			
			function link(scope, element, attrs) {
				var w = angular.element($window);

				function onResize() {
					var width = w.width();
					if (width > 980) {
		            	element.css('width', w.width() + 'px');
					} else {
						element.css('width', '980px');
					}
		            		element.css('height', (w.height() - 50) + 'px');
				};
				
				function cleanUp() {
					angular.element($window).off('resize', onResize);
				};

				angular.element($window)
		          .on('resize', onResize)
		          .trigger('resize');
				
	            scope.$on('$destroy', cleanUp);
			};
		}]);
})();
