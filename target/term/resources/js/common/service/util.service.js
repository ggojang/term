(function () {
	
	'use strict';
	
	angular
		.module('app.common')
		.factory('utilservice', UtilService);
	
	UtilService.$inject = ['$window'];
	function UtilService($window) {
		
		var FSN = "900000000000003001";
		var SYN = "900000000000013009";
		var DEF = "900000000000550004";
		var PRF = "900000000000548007";
		
		var FULLY_DEFINED = "900000000000073002";
		var PRIMITIVE = "900000000000074008";
		
		return {
			isUndefined: isUndefined,
			safeApply: safeApply,
			_: $window._,
			
			/* SNOMED CT utils*/
			getConceptType: getConceptType,
			getConceptTypeSimple: getConceptTypeSimple,
			getDescriptionPriority: getDescriptionPriority,
			getDefinitionStatusFlagValue: getDefinitionStatusFlagValue,
			getDefinitionStatusName: getDefinitionStatusName,
			termBeautifier: termBeautifier,
			getSemanticTag: getSemanticTag
		};
		
		
		function isUndefined(object) {
			return angular.isUndefined(object);
		};

		// View가 Scope또는 Controller의 값이 변경됨을 인지하지 못한 상황에서도
		// 인지하게끔 해주는 부분
		// $apply로만 접근할 경우 중복 실행할 경우가 있으므로 충돌을 일으키기도한다.
		// 따라서 $$phase로 현재 스코프가 어느 단계에 있는지 알수 있으므로
		// $$phase가 $apply이거나 $digest이면 그냥 스코프를 변경하고
		// 그렇지 않으면 $apply를 사용한다.
		// https://siongui.github.io/2013/04/04/angularjs-safe-scope-apply/
		function safeApply(scope, fn) {
			var phase = scope.$root.$$phase;
			if (phase == '$apply' || phase == '$digest') {
				scope.$eval(fn);
			} else {
				scope.$apply(fn);
			}
		};

		function getConceptType(typeId, acceptabilityId) {
			var type = '';

			if (typeId === FSN) {
				type = "Fully Specified Name";
			} else if (typeId === SYN) {
				type = angular.isDefined(acceptabilityId) && acceptabilityId === PRF ? 'Preferred':'Synonym';
			} else if (typeId === DEF) {
				type = "Definition";
			} else {
				type = "undefined";
			}

			return type;
		};

		function getConceptTypeSimple(typeId, acceptabilityId) {
			var type = '';

			if (typeId === FSN) {
				type = 'fsn';
			} else if (typeId === SYN) {
				type = angular.isDefined(acceptabilityId) && acceptabilityId === PRF ? 'preferred':'synonym';
			} else if (typeId === DEF) {
				type = 'definition';
			}

			return type;
		};

		function getDescriptionPriority(typeId, acceptabilityId) {
			var priority = 0;
			if (typeId === FSN) {
				priority = 1;
			} else if (typeId === SYN) {
				if (acceptabilityId === PRF) {
					priority = 2; // Preferred
				} else {
					priority = 3;
				}
			} else {
				priority = 99;
			}

			return priority;
		};

		function getDefinitionStatusFlagValue(definitionStatusId) {
			if (!angular.isDefined(definitionStatusId) || definitionStatusId.length === 0) {
				return '';
			}

			var flag = '';

			if (definitionStatusId === FULLY_DEFINED) {
				flag = "≡";
			} else if (definitionStatusId === PRIMITIVE) {
				flag = "&nbsp;&nbsp;";
			}
			
			return flag;
		};
		
		function getDefinitionStatusName(definitionStatusId) {
			if (!angular.isDefined(definitionStatusId) || definitionStatusId.length === 0) {
				return '';
			}
			
			var type = '';

			if (definitionStatusId === PRIMITIVE) {
				type = "Primitive";
			} else if (definitionStatusId === FULLY_DEFINED) {
				type = "Fully Defined";
			}

			return type;
		};


		function termBeautifier(term) {
			if (!angular.isDefined(term) || term.length === 0) {
				return '';
			}

			var idx = term.indexOf(')'); 
			// modfied by Yu, 20191014	
			//return idx != -1 ? term.substring(0, term.lastIndexOf('(')) : term;
			return idx == (term.length + 1) ? term.substring(0, term.lastIndexOf('(')) : term;	
		};

		function getSemanticTag(term) {
			if (angular.isDefined(term) || term.length === 0) {
				return '';
			}

			return term.substring(term.lastIndexOf('(') + 1, term
					.lastIndexOf(')'));
		};
	};
	
})();
