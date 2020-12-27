(function() {

	'use strict';

	angular
		.module('app.referenceset')
		.factory('srchservice', SrchService);

	SrchService.$inject = [ 'apiservice', 'utilservice', 'userservice' ];
	function SrchService(apiservice, utilservice, userservice) {
		
		var service = {
			model: {},
			tree: [], // {Array} 자동완성에서 선택한 컨셉의 트리
			matches: [],
			initVar: initVar
		};
		
		function initVar() {
			service.model.length = 0;
			service.tree.length = 0;
			service.matches.length = 0;
		}
		
		return service;
		
	}
	
})();