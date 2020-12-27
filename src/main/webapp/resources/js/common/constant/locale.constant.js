(function () {
	
	'use strict';
	
	angular
		.module('app.common')
		.constant(
			'common.locale', {
				'locales': {
					'en-US': 'English',
					'ko-KR': '한국어'
				},
				'preferredLocale': 'ko-KR'
			});
})();