/**
 * 
 */
(function() {
	'use strict';
	
	angular
	.module('app.concept')
	.service('conceptEditServcie', conceptEditServcie);

	conceptEditServcie.$inject = ['utilservice', '$http', 'CONFIG'];
	function conceptEditServcie(utilservice, $http, config) {
		var service = this;
		var baseUrl = config.apiUrl+'/snomedct/ccpteditor'
			, statedUrl = baseUrl+'/stated'
			, inferredUrl = baseUrl+'/inferred';
		
		return {
			callStated : callStated,
			callInferred : callInferred
		};
		
		function callStated(param) {
			return postCall(statedUrl, param, true);
		}
		
		function callInferred(param) {
			return postCall(inferredUrl, param, true);
		}
		
		function postCall(url, param, transStr) {
			transStr = transStr == undefined ? false : transStr;
			var conf = {method : 'POST', url : url};
			if(angular.isDefined(param) && (angular.isObject(param) || angular.isArray(param))) {
				if(transStr) {
					conf.data = JSON.stringify(param);
					conf.headers = {'Content-Type': 'application/json; charset=utf-8'};
				} else {
					conf.data = param;
				}
			}
			
			return $http(conf).then(successCallback, errorCallback);
		}
		/** 
		 * data – {string|Object} – The response body transformed with the transform functions.
		 * status – {number} – HTTP status code of the response.
		 * headers – {function([headerName])} – Header getter function.
		 * config – {Object} – The configuration object that was used to generate the request.
		 * statusText – {string} – HTTP status text of the response.
		 */
		function successCallback(response) {
			return response.data;
		}
		
		function errorCallback(response) {
			return response;
		}
	};
})();
