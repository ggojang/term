(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.constant(
			'browser.langrefset', [
               {
                   'id' : '900000000000509007',
                   'name' : 'United States of America English language reference set (foundation metadata concept)',
                   'countryCode' : 'us',
                   'languageCode' : 'en'
                 },
                 {
                   'id' : '900000000000508004',
                   'name' : 'Great Britain English language reference set (foundation metadata concept)',
                   'countryCode' : 'gb',
                	  'languageCode' : 'en'
                 },
                 {
                   'id' : '247781000300103',
                   'name' : 'Korean language reference set (foundation metadata concept)',
                   'countryCode' : 'kr',
                   'languageCode' : 'kr'
                 }
           		] 
		);
})();