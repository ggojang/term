/*
 * InfoClinic.Co.,Ltd.
 * terminology.js
 * IIFE scoping: To avoid polluting the global scope with our function declarations that get passed into Angular, ensure build tasks wrap the concatenated files inside an IIFE
 * All the code is wrapped in the IIFE
 * Reference: http://www.codestyle.co/Guidelines/angularjs
 * Reference: https://github.com/toddmotto/angular-styleguide
 */
(function () {

  'use strict';

  angular
    .module('terminology', []);

  // Config.js
  var configData = {
    'GENERAL_CONFIG': {
      //'API_URL': 'http://localhost:9000'
      'API_URL': 'http://was.infoclinic.co/snomedct'
    }
  }

  angular
    .forEach(configData, function(value, key) {
      angular
        .module('terminology')
        .constant(key, value);
    });
})();
