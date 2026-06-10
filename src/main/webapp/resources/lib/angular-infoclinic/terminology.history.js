/*
* InfoClinic.Co.,Ltd.
* terminology.history.js
* IIFE scoping: To avoid polluting the global scope with our function declarations that get passed into Angular, ensure build tasks wrap the concatenated files inside an IIFE
* All the code is wrapped in the IIFE
* Reference: http://www.codestyle.co/Guidelines/angularjs
* Reference: https://github.com/toddmotto/angular-styleguide
*/
(function () {

  'use strict';

  // PreDeclare terminology.js
  angular
  .module('terminology.history', []);

  // Config.js
  var configData = {
    'GENERAL_CONFIG': {
      //'API_URL': 'http://localhost:9000'
      //'API_URL': 'http://was.infoclinic.co/snomedct'
      'API_URL': 'http://api.infoclinic.co/snomedct'
    }
  }

  angular
  .forEach(configData, function(value, key) {
    angular
    .module('terminology.history')
    .constant(key, value);
  });

  // HistoryFactory.js
  var dependencies = [
    '$resource',
    'GENERAL_CONFIG'
  ];

  function HistoryFactory ($resource, GENERAL_CONFIG) {
    return $resource(
      GENERAL_CONFIG.API_URL + '/history?id=:id&effectiveTime=:effectiveTime',
      {id: '@id', effectiveTime: '@effectiveTime'},
      {'get': {method:'GET', isArray:true}});
  };

  angular
  .module('terminology.history')
  .factory('History', dependencies.concat(HistoryFactory));


  //
})();
