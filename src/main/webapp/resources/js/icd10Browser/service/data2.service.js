(function() {

	'use strict';

	angular
		.module('app.icd10Browser')
		.service('data2service', Data2Service);

	Data2Service.$inject = [ '$q', 'apiservice', 'userservice' ];
	function Data2Service($q, apiSvc, usrSvc) {
	
		var service = this;
		var result = [];

		// ----------------------------------------
		// Method declare
		// ----------------------------------------
		
		service.getCode = getCode;
		service.getChildren = getChildren;
		service.getCodeRoot = getCodeRoot;
		service.getRubric = getRubric;
		service.getSibling = getSibling;
		service.getAncestor = getAncestor;
		//service.getCodeList = getCodeList;
		service.search = search;
		
		
		// ----------------------------------------
		// Method implements
		// ----------------------------------------
	
		/**
		 * @name getCode
		 * @desc ICD10의 Code를 호출
		 * @param {string} code  ICD10 code
		 * @param {string} ver version
		 */
		function getCode(code, ver) {
			// operator
			var tmp = code;	
			if ( tmp.search("\\*") !== -1) {
				code = tmp.slice(0, code.search("\\*"));
			} else if ( tmp.search("\\&dagger;") !== -1) {
				code = tmp.slice(0, tmp.search("\\&dagger;"));
			}  else if ( tmp.search("\\+") !== -1) {
                                code = tmp.slice(0, tmp.search("\\+"));
                        } else if ( tmp.search("\\.-") !== -1) {
				code = tmp.slice(0, tmp.search("\\.-"));
			}
			
			var oper = '/entity/ICD10/' + code + '?version=' + ver;
			
			// request
			return apiSvc.get(oper)
			.then(getCodeComplete, getCodeFailed);
			
			// complete
			function getCodeComplete(response) {
				return response.data;
			};
			
			// failed
			function getCodeFailed() {
				// FIXME: logger.error();
			};
		};
		
		
		/**
		 * @name getChildren
		 * @desc ICD10 Code의 자식 목록 호출
		 * @param {string} code  ICD10 code
		 * @param {string} ver version
		 * @param {
		 */
		function getChildren(code, ver) {
			// operator
			var oper = '/children/ICD10/' + code + '?version=' + ver;
			
			//oper += addOpts(opts);
			
			//oper += oper.contains('?') ? '&lang=zh-CN':'?lang=zh-CN';
			//oper += oper.contains('?') ? '&lang=ko-KR':'?lang=ko-KR';
			
			// request
			return apiSvc.get(oper)
			.then(getChildrenComplete, getChildrenFailed);
			
			// complete
			function getChildrenComplete(response) {
				return response.data;
			};
			
			// failed
			function getChildrenFailed() {
				// FIXME: logger.error();
			};
		};
		
		
		/**
		 * @name getHierarchyRoot
		 * @desc 계층구조의 루트 및 루트의 자식 목록 호출
		 * @param {string} ver ICD10 version
		 */
		function getCodeRoot(ver) {
			var deferred = $q.defer();
			
			// request
			$q.all([
				getChildren('ICD10 2016', ver),
				//getChildren('PARTS', ver, opts)
			])
			.then(getCodeRootComplete, getCodeRootFailed);
			
			// complete
			function getCodeRootComplete(response) {
				deferred.resolve(response);
			};
			
			// failed
			function getCodeRootFailed() {
				deferred.reject(error);
			};
			
			return deferred.promise;
		};
		
		function getRubric(code, ver) {
			// operator
                        var oper = '/rubric/ICD10/' + code + '?version=' + ver;

                        // request
                        return apiSvc.get(oper)
                        .then(getRubricComplete, getRubricFailed);

                        // complete
                        function getRubricComplete(response) {
                                return response.data;
                        };

                        // failed
                        function getRubricFailed() {
                                // FIXME: logger.error();
                        };
                };

		function getAncestor(code, ver) {
			var tmp = code;
                        if ( tmp.search("\\*") !== -1) {
                                code = tmp.slice(0, code.search("\\*"));
                        } else if ( tmp.search("\\&dagger;") !== -1) {
                                code = tmp.slice(0, tmp.search("\\&dagger;"));
                        }  else if ( tmp.search("\\+") !== -1) {
                                code = tmp.slice(0, tmp.search("\\+"));
                        } else if ( tmp.search("\\.-") !== -1) {
                                code = tmp.slice(0, tmp.search("\\.-"));
                        }
			
			// operator
                        var oper = '/ancestor/ICD10/' + code + '?version=' + ver;

                        // request
                        return apiSvc.get(oper)
                        .then(getAncestorComplete, getAncestorFailed);

                        // complete
                        function getAncestorComplete(response) {
                                return response.data;
                        };

                        // failed
                        function getAncestorFailed() {
                                // FIXME: logger.error();
                        };
                };

		function getSibling(code, ver) {
			var tmp = code;
                        if ( tmp.search("\\*") !== -1) {
                                code = tmp.slice(0, code.search("\\*"));
                        } else if ( tmp.search("\\&dagger;") !== -1) {
                                code = tmp.slice(0, tmp.search("\\&dagger;"));
			} else if ( tmp.search("\\+") !== -1) {
                                code = tmp.slice(0, tmp.search("\\+"));
			} else if ( tmp.search("\\.-") !== -1) {
                                code = tmp.slice(0, tmp.search("\\.-"));
                        }
			
			var oper = '/sibling/ICD10/' + code + '?version' + ver;

			return apiSvc.get(oper)
			.then(getSiblingComplete, getSiblingFailed);

                        // complete
                        function getSiblingComplete(response) {
                                return response.data;
                        };

                        // failed
                        function getSiblingFailed() {
                                // FIXME: logger.error();
                        };
                };

		/*
		function getChapter(code, ver) {
		*/
			/*
			 * 선택된 코드의 path 정보를 이용해 ancestor 코드를 모두 찾아 
			 *   chapter를 최상위에 출력
			 *   block은 그 다음에 출력
			 *   category는 그 다음에 출력
			 *
			 * 선택된 code의 path를 읽어서 "*~" 패턴으로 문자열 배열로 그룹핑
			 * 정규식의 positive lookbehind (?<= )를 이용함
			 *
			 * var path = response.data.path.match( /(?<=~)([\w-]+)/g );
			 * 처리의 예. ICD10 2016~XX~X85-Y09
			 *            => ["XX", "X85-Y09"]
			 */
			/*
			var oper = '/entity/ICD10/' + code + '?version=' + ver;

			return apiSvc.get(oper)
                        .then(getChapterComplete, getChapterFailed);

                        // complete
                        function getChapterComplete(response) {
				if (response.data.classKind !== 'chapter') {
					oper = '/entity/ICD10/' + response.data.superClass + '?version=' + ver;
					return apiSvc.get(oper)
 	                      		.then(getChapterComplete, getChapterFailed);
				} else {
					oper = '/rubric/ICD10/' + response.data.code + '?version=' + ver;
					return apiSvc.get(oper)
					.then(getChapterComplete2, getChapterFailed2);
                                	//return response.data;
				}
					
                        };
                        
			// failed
                        function getChapterFailed() {
                                // FIXME: logger.error();
                        };
			
			function getChapterComplete2(response) {
				return response.data
			}

			function getChapterFailed2() {
                                // FIXME: logger.error();
                        };
		};
		*/	

		/**
		 * @name getCodeList
		 * @desc 해당 코드의 계층정보 목록을 호출
		 * @param {string} code ICD10 code
		 * @param {string} ver ICD10 version
		 
		function getCodeList(code, ver) {
			// operator
			var oper = '/paths/ICD10/' + code + '?version=' + ver;
			
			// request
			return apiSvc.get(oper)
			.then(getCodeListComplete, getCodeListFailed);
			
			// complete
			function getCodeListComplete(response) {
				return response.data;
			};
			
			// failed
			function getCodeListFailed() {
				// FIXME: logger.error();
			};
		};
		*/
		
		/**
		 * @name search
		 * @param {string} q 검색어
		 */
		function search(q, ver, size, page) {
			// operator
			var oper = '/search/ICD10';
			
			oper += '?version=' + ver + '&q=' + q + '&size=' + size + '&page=' + page;
			
			// request
			return apiSvc.get(oper)
			.then(searchComplete, searchFailed);
			
			// complete
			function searchComplete(response) {

				if (response.data.number+1 === response.data.totalPages) {
					var limit = (response.data.totalElements % response.data.size) - 1;
				} else {
					var limit = response.data.size -1;
				}

				//console.log(response.data);
				/*for (var i=0; i <= limit ; i++) {
					
					if (response.data.content[i].kind === 'preferred') {
						response.data.content[i].label = '[P] ' + response.data.content[i].label;
					} else if (response.data.content[i].kind === 'inclusion') {
						response.data.content[i].label = '[I] ' + response.data.content[i].label;
					}
					
				}*/
				
				//console.log(response.data);
				return response.data;
			};
			
			// failed
			function searchFailed() {
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
		 
		function addOpts(opts) {
			var o = '';
			
			if (typeof opts !== 'undefined') {
				o = '?';
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
			
			return o;
		};
		*/
	}
}());
