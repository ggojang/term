(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.constant(
			'browser.description', {
				'type': {
					'fsn': '900000000000003001',
					'preferred': '900000000000548007',
					'synonym': '900000000000013009',
					'definition': '900000000000550004'
				},
				'acceptability' : {
					'preferred': '900000000000548007',
					'acceptable': '900000000000549004'
				}
			} 
		);
})();