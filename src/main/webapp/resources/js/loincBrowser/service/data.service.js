(function() {

	'use strict';

	angular
		.module('app.loincBrowser')
		.service('dataservice', DataService);

	DataService.$inject = [ '$q', 'apiservice', 'userservice' ];
	function DataService($q, apiSvc, usrSvc) {
	
		var service = this;
		
		// ----------------------------------------
		// Method declare
		// ----------------------------------------
		
		service.getEntity = getEntity;
		service.getCodeType = getCodeType; //
		service.getLP = getLP //
		service.getLPLink = getLPLink; //
		service.getLPMap = getLPMap; //
		service.getLA = getLA; //
		service.getLG = getLG; //
                service.getLGTerm = getLGTerm; //
		service.getChildren = getChildren;
		service.getHierarchyRoot = getHierarchyRoot;
		service.getHierarchyList = getHierarchyList;
		service.search = search;
		service.getPanel = getPanel;
		service.getLV = getLV;	

		// ----------------------------------------
		// Method implements
		// ----------------------------------------
	
		/**
		 * @name getEntity
		 * @desc LOINC의 Entity를 호출
		 * @param {string} code  LOINC code
		 * @param {string} ver version
		 */
		function getEntity(code, ver) {
			// operator, 20200411 by Yu
			if (code.substring(0, 2) == 'LP') {
				var oper = '/LP/LOINC/' + code + '?version=' + ver;
			} else if (code.substring(0, 2) == 'LG') {
				var oper = '/LG/LOINC/' + code + '?version=' + ver;
			} else {	
				var oper = '/entity/LOINC/' + code + '?version=' + ver;
			}
			console.log('code : ' + code + ' oper : ' + oper);
			// request
			
			return apiSvc.get(oper)
			.then(getEntityComplete, getEntityFailed);
			
			// complete
			function getEntityComplete(response) {
				//console.log("getEntityComplete : " + response.data);
				return response.data;
			};
			
			// failed
			function getEntityFailed() {
				//return null;
				// FIXME: logger.error();
			};
		};
		
		function getCodeType(code) { // 20200412 by Yu
			var codeType;
			if (code.substring(0, 2) == 'LP') {
				codeType = 'LP';
			} else if (code.substring(0, 2) == 'LG') {
				codeType = 'LG';
			} else {
				codeType = '';
			}
			console.log("dataSvc.getCodeType : " + codeType);
			return codeType;
		};
		
		function getLP(code) {
                        // operator, 20200411 by Yu
                        var oper = '/LP/LOINC/' + code;
                        //console.log('code : ' + code + ' oper : ' + oper);
                        // request

                        return apiSvc.get(oper)
                        .then(getLPComplete, getLPFailed);
                        // complete
                        function getLPComplete(response) {
                                //console.log("getLPLinkComplete : " + response.data);
                                return response.data;
                        };

                        // failed
                        function getLPFailed() {
                                //return null;
                                // FIXME: logger.error();
                        };
                };

		function getLPLink(code) {
                        // operator, 20200411 by Yu
                        var oper = '/LPLINK/LOINC/' + code;
                        //console.log('code : ' + code + ' oper : ' + oper);
                        // request

                        return apiSvc.get(oper)
                        .then(getLPLinkComplete, getLPLinkFailed);
                        // complete
                        function getLPLinkComplete(response) {
                                //console.log("getLPLinkComplete : " + response.data);
                                return response.data;
                        };

                        // failed
                        function getLPLinkFailed() {
                                //return null;
                                // FIXME: logger.error();
                        };
                };

		function getLPMap(code) {
                        // operator, 20200411 by Yu
                        var oper = '/LPMAP/LOINC/' + code;
                        //console.log('code : ' + code + ' oper : ' + oper);
                        // request

                        return apiSvc.get(oper)
                        .then(getLPMapComplete, getLPMapFailed);
                        // complete
                        function getLPMapComplete(response) {
                                //console.log("getLPMapComplete : " + response.data);
                                return response.data;
                        };

                        // failed
                        function getLPMapFailed() {
                                //return null;
                                // FIXME: logger.error();
                        };
                };

		function getLA(code) {
                        // operator, 20200411 by Yu
                        var oper = '/LA/LOINC/' + code;
                        //console.log('code : ' + code + ' oper : ' + oper);
                        // request

                        return apiSvc.get(oper)
                        .then(getLAComplete, getLAFailed);
                        // complete
                        function getLAComplete(response) {
                                //console.log("getLAComplete : " + response.data);
                                return response.data;
                        };

                        // failed
                        function getLAFailed() {
                                //return null;
                                // FIXME: logger.error();
                        };
                };

		function getLG(code) {
                        // operator, 20200411 by Yu
                        var oper = '/LG/LOINC/' + code;
                        //console.log('code : ' + code + ' oper : ' + oper);
                        // request

                        return apiSvc.get(oper)
                        .then(getLGComplete, getLGFailed);
                        // complete
                        function getLGComplete(response) {
                                //console.log("getLAComplete : " + response.data);
                                return response.data;
                        };

                        // failed
                        function getLGFailed() {
                                //return null;
                                // FIXME: logger.error();
                        };
                };

                function getLGTerm(code) {
                        // operator, 20200411 by Yu
                        var oper = '/LGTERM/LOINC/' + code;
                        //console.log('code : ' + code + ' oper : ' + oper);
                        // request

                        return apiSvc.get(oper)
                        .then(getLGTermComplete, getLGTermFailed);
                        // complete
                        function getLGTermComplete(response) {
                                //console.log("getLAComplete : " + response.data);
                                return response.data;
                        };

                        // failed
                        function getLGTermFailed() {
                                //return null;
                                // FIXME: logger.error();
                        };
                };

                function getLV(code) {
                        // operator, 20200411 by Yu
                        var oper = '/LV/LOINC/' + code;
                        //console.log('code : ' + code + ' oper : ' + oper);
                        // request

                        return apiSvc.get(oper)
                        .then(getLVComplete, getLVFailed);
                        // complete
                        function getLVComplete(response) {
                                //console.log("getLAComplete : " + response.data);
                                return response.data;
                        };

                        // failed
                        function getLVFailed() {
                                //return null;
                                // FIXME: logger.error();
                        };
                };

		/**
		 * @name getChildren
		 * @desc LOINC Entity의 자식 목록 호출
		 * @param {string} code  LOINC code
		 * @param {string} ver version
		 * @param {
		 */
		function getChildren(code, ver, opts) {
			// operator
			var oper = '/children/LOINC/' + code + '?version=' + ver;
			
			oper += addOpts(opts);
			
			//oper += oper.contains('?') ? '&lang=zh-CN':'?lang=zh-CN';
			//oper += oper.contains('?') ? '&lang=ko-KR':'?lang=ko-KR';
			
			// request
			console.log("operator : " + oper)
			return apiSvc.get(oper)
			.then(getChildrenComplete, getChildrenFailed);
			
			// complete
			function getChildrenComplete(response) {
				console.log("getChildrenComplete");
				return response.data;
			};
			
			// failed
			function getChildrenFailed() {
				console.log("getChildrenFailed");
				// FIXME: logger.error();
			};
		};
		
		
		/**
		 * @name getHierarchyRoot
		 * @desc 계층구조의 루트 및 루트의 자식 목록 호출
		 * @param {string} ver LOINC version
		 */
		function getHierarchyRoot(ver, opts) {
			var deferred = $q.defer();
			
			// request
			$q.all([
				getChildren('CLASS', ver, opts),
				getChildren('PARTS', ver, opts),
				getChildren('GROUP', ver, opts)
			])
			.then(getHierarchyRootComplete, getHierarchyRootFailed);
			
			// complete
			function getHierarchyRootComplete(response) {
				deferred.resolve(response);
			};
			
			// failed
			function getHierarchyRootFailed() {
				deferred.reject(error);
			};
			
			return deferred.promise;
		};
		
		
		/**
		 * @name getHierarchyList
		 * @desc 해당 코드의 계층정보 목록을 호출
		 * @param {string} code LOINC code
		 * @param {string} ver Loinc version
		 */
		function getHierarchyList(code, ver) {
			// operator
			var oper = '/paths/LOINC/' + code + '?version=' + ver;
			
			// request
			return apiSvc.get(oper)
			.then(getHierarchyListComplete, getHierarchyListFailed);
			
			// complete
			function getHierarchyListComplete(response) {
				return response.data;
			};
			
			// failed
			function getHierarchyListFailed() {
				// FIXME: logger.error();
			};
		};
		
		
		/**
		 * @name search
		 * @param {string} q 검색어
		 */
		function search(q, ver, size, page) {
			// operator
			var oper = '/search/LOINC';
			
			oper += '?version=' + ver + '&q=' + q + '&size=' + size + '&page=' + page;
			
			// request
			return apiSvc.get(oper)
			.then(searchComplete, searchFailed);
			
			// complete
			function searchComplete(response) {
				return response.data;
			};
			
			// failed
			function searchFailed() {
				// FIXME: logger.error();
			};
		};
		
		
		function getPanel(code, ver) {
			// operator
			var oper = '/panel/LOINC/' + code;
			
			oper += '?version=' + ver;
			
			// request
			return apiSvc.get(oper)
			.then(getPanelComplete, getPanelFailed);
			
			// complete
			function getPanelComplete(response) {
				return response.data;
			};
			
			// failed
			function getPanelFailed() {
				// FIXME: logger.error();
			};
		};
		
		
		// ----------------------------------------
		// Private Methods
		// ----------------------------------------
		
		/**
		 * @name addOpts
		 * @desc URL뒤에 붙는 매개변수들을 문자열로 반환
		 * @param {Object} opts 매개변수 목록
		 */
		function addOpts(opts) {
			var o = '';
			
			if (typeof opts !== 'undefined') {
				o = '&';
				var cnt = 0;
				for (var k in opts) {
					// 두번째 매개변수부터 '&' 붙임
					if (cnt > 0) {
						o += '&'
					}
					
					// 키값 존재를 확인 후 키와 값을 붙임
					if (opts.hasOwnProperty(k)) {
						o += k + '=' + opts[k];
					}
					
					// 카운트 + 1
					cnt++;
				}
			}
			//console.log("opts : " + o);
			return o;
		};
	}
}());
