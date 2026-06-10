(function () {
	
	'use strict';
	
	angular
		.module('app.icd10Browser')
		.service('codeservice', CodeService);
	
	CodeService.$inject = ['$q', '$window', 'utilservice'];
	function CodeService($q, $window, utilSvc) {

		var service = this;
		
		service.getSelectedCode = getSelectedCode;
		service.setSelectedCode = setSelectedCode;
		//service.getLabel = getLabel;
		service.isLeafCode = isLeafCode;
		
		var data = {
			cd: 0
		};
	
		function getSelectedCode() {
			return data.cd;
		}
		
		function setSelectedCode(cd) {
			data.cd = cd;
		}
		
		/**
		 * @name getLabel
		 * @desc 
		 * @param {Object} e Code
		  
		function getLabel(e) {
			var label='';
			
			for (let e2 in e) {
				if (e2.kind === 'preferred') {
					label = '[P] ' + e2.label;
				} else if (e2.kind === 'inclusion') {
					label = '[I] ' + e2.label;
				}
			}
			
			$log.debug('Service:code, getLabel()');	
			return label;
		};
		 */
		/**
                 * @name isLeafCode
                 * @desc 코드의 리프 코드 여부
                 * @param {string} code LOINC Code
                 */
                function isLeafCode(code) {
                        return !isNaN(code[0]) && code.contains('-') ? true: false;
                };
	}
})();
