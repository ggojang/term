(function () {
	
	'use strict';
	
	angular
		.module('app.loincBrowser')
		.service('entityservice', EntityService);
	
	EntityService.$inject = ['$q', '$window', 'utilservice'];
	function EntityService($q, $window, utilSvc) {

		var service = this;
		
		service.getSelectedCode = getSelectedCode;
		service.setSelectedCode = setSelectedCode;
		service.getFsn = getFsn;
		service.isLeafCode = isLeafCode;
		service.setPanelType = setPanelType;
		service.getPanelType = getPanelType;
		
		
		var data = {
			cd: 0
		};
		
		var panelType = '';
		
		function getSelectedCode() {
			return data.cd;
		}
		
		function setSelectedCode(cd) {
			data.cd = cd;
		}
		
		/**
		 * @name getFsn
		 * @desc Entity에서 Part6개를 조합한 FSN 반환
		 * @param {Object} e Entity
		 */
		function getFsn(e) {
			var fsn;
			
			// part1: component
			fsn = e.component;
			// part2: property
			fsn += ':' + e.property;
			// part3: time
			fsn += ':' + e.time;
			// part4: system
			fsn += ':' + e.system;
			// part5: scale
			fsn += ':' + e.scale;
			// part6: method
			if (e.method !== '') {
				fsn += ':' + e.method;
			}
			
			return fsn;
		};
		
		/**
		 * @name getClassTypeName
		 * @desc 클래스 타입의 전체 이름을 반환
		 * @param {string} clsTyp 클래스 타입 
		 */
		function getClassTypeName(clsTyp) {
			var name;
			
			if (clsTyp === 'Lab') {
				name = 'Laboratory'; 
			} 
			
			return name;
		};
		
		
		/**
		 * @name isLeafCode
		 * @desc 코드의 리프 코드 여부
		 * @param {string} code LOINC Code
		 */
		function isLeafCode(code) {
			return !isNaN(code[0]) && code.contains('-') ? true: false;
		};
		
		
		function setPanelType(type) {
			if ( type == null) { // 20204011, by Yu
			  panelType = '';
			} else {
			  panelType = type;
			}
		}
		
		function getPanelType() {
			return panelType;
		}
	}
})();
