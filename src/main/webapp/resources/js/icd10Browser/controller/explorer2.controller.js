(function () {
	
	'use strict';
	
	angular
		.module('app.icd10Browser')
		.controller('Explorer2Controller', Explorer2Controller);
	
	Explorer2Controller.$inject = ['$scope', '$log', '$document', '$element', '$stateParams', '$timeout', 'data2service', 'codeservice', 'utilservice', 'icd10browserservice'];
	function Explorer2Controller($scope, $log, $document, $element, $stateParams, $timeout, data2Svc, codeSvc, utilSvc, browserSvc) {
		
		// ----------------------------------------
		// Local variable
		// ----------------------------------------
		var vm = this;
		
		var ctx = {
			ver: '2016'
		};
		
		vm.tree = [];
		vm.srch = '';
		vm.srchRslts = [];
		vm.srchTotalSize = 0;
		vm.srchTotalPage = 0;
		vm.srchCurrentPage = 1;
		vm.srchSize = 100;
		
		vm.srchHistories = [];
		
		vm.srchFocus = false;
		vm.srchCall = false;
		
		vm.srchLoading = false;
		
		
		// ----------------------------------------
		// Public methods
		// ----------------------------------------
		
		vm.getChildren = getChildren;
		vm.getArrowClass = getArrowClass;
		vm.search = search;
		vm.clear = clear;
		
		vm.isSelected = isSelected;
		vm.onKeyUp = onKeyUp;
		vm.onFocus = onFocus;
		vm.getAutoframeViewName = getAutoframeViewName;
		vm.removeSearchHistoryAll = removeSearchHistoryAll;
		
		
		// ----------------------------------------
		// Init
		// ----------------------------------------
		activate();
		
		
		// ----------------------------------------
		// Public methods implements
		// ----------------------------------------

		/**
		 * @name activate
		 * @desc Entry Point
		 */
		function activate() {
			
			vm.srchLoading = true;
			
			data2Svc.getCodeRoot(ctx.ver /*, {lang:browserSvc.getLanguage()}*/)
			.then(function (root) {
				// class hierarchy
				var cls = root[0];
				
				vm.tree.push({
					label: 'ICD10 2016 (18776)',
					open: true,
					children: cls
				});
			})
			.finally(function () {
				
				$timeout(function () {
					vm.srchLoading = false;
				}, 500);
				
				$log.debug('<Initialize> Explorer Controller Activated.');
			});
		};
		
		
		/**
		 * @name getChildren
		 * @desc 현재노드의 ICD10 code의 자식 목록 호출
		 * @param {Object} node
		 */
		function getChildren(node) {
			
			// 자식 수가 존재하며, 현재 자식목록을 가지고 있지 않다면
			if (  /*node.childrenCount > 0 && */ typeof node.children === 'undefined') {
				
				/* 매개변수 옵션
				var opts = {
					path: node.path,
					lang: browserSvc.getLanguage()
				};
				console.log(opts);
				*/
				// 서버로 부터 자식 목록 가져옴
				data2Svc.getChildren(node.code, ctx.ver)
				.then(function (chdrn) {
					// 노드에 자식 목록 반영
					node.children = chdrn;
				})
				.finally(function () {
					$log.debug('<Service Call> Children: ' + node.code + '  // ICD10 ' + ctx.ver);
				});
			}
			
			// 노드의 오픈상태 전환
			node.open = !node.open;
		};
		
		
		/**
		 * @name search
		 * @desc ICD10 검색
		 * @param {string} q 검색어
		 */
		function search(q, next) {
			
			if (typeof q === 'undefined' || q.length === 0) { alert('type less 1 words'); return false; }
			
			vm.srchLoading = true;
			// 요청할 페이지
			var page = typeof next === 'undefined' ? 1 : vm.srchCurrentPage+1 ;
			
			// 검색 수행 시 첫번째 페이지를 요청하면 스크롤 초기화
			if (page === 1) {
				goScrollTop();
				
				browserSvc.addSearchHistory(q);
			}
			
			if (vm.srchFocus) {
				vm.srchFocus = false;
			}
			
			// 검색서비스 호출여부
			vm.srchCall = true;
			
			
			// 검색 요청
			data2Svc.search(q, ctx.ver, vm.srchSize, page)
			.then(function (srchRslt) {
				// 검색 결과
				
				// 전체 결과 수
				vm.srchTotalSize = srchRslt.totalElements;
				// 전체 페이지 수
				vm.srchTotalPage = srchRslt.totalPages;
				// 현재 페이지
				vm.srchCurrentPage = srchRslt.number + 1;
				
				// 첫번째 페이지인 경우
				if (srchRslt.first) vm.srchRslts = srchRslt.content;
				// 첫번째 페이지가 아닌 경우
				else Array.prototype.push.apply(vm.srchRslts, srchRslt.content);
			})
			.finally(function () {
				vm.srchLoading = false;
			});
			
			
			$log.debug('<Service Call> Search' + (typeof next === 'undefined' ? '':' (More)') + ': "' + q + '"  // ICD10 ' + ctx.ver + ', page=' + page + ', size=' + vm.srchSize);
		}
		
		
		/**
		 * @name getArrowClass
		 * @desc 트리노드의 화살표 아이콘을 반환
		 * @param {Object} node 
		 */
		function getArrowClass(node) {
			//node.childrenCount > 0 ? === 0 ? 'glyphicon-minus'
			// :(!node.open ? 'glyphicon-chevron-right':(node.loading ? 'glyphicon-refresh icon-spin':'glyphicon-chevron-down'))
			return node.childrenCount === 0 ? 'glyphicon-minus':(!node.open ? 'glyphicon-chevron-right':'glyphicon-chevron-down');
		};
		
		
		/**
		 * @name clear
		 * @desc
		 * @returns
		 */
		function clear() {
			vm.srch = '';
			vm.srchRslts.length = 0;
			vm.srchTotalSize = 0;
			vm.srchTotalPage = 0;
			vm.srchCurrentPage = 1;
			vm.srchSize = 100;
			
			// 검색서비스 호출여부
			vm.srchCall = false;
		};
		
		
		/**
		 * @name isSelected
		 * @desc
		 * @param {string} cd
		 * @returns
		 */
		function isSelected(cd) {
			return cd === codeSvc.getSelectedCode() ? true:false;
		};
		
		
		/**
		 * @name goScrollTop
		 * @desc
		 * @returns
		 */
		function goScrollTop() {
			$element.find('.lb-srch-vw').scrollTop('0');
		};
		
		
		/**
		 * @name onKeyUp
		 * @desc
		 * @param {Object} $event
		 * @returns
		 */
		function onKeyUp($event) {
			var keyCode = $event.which || $event.keyCode;
			// if enter key
			if (keyCode === 13) {
				search(vm.srch);
			} else if (!vm.srchFocus) {
				vm.srchFocus = true;
			}
		};
		
		
		/**
		 * @name onFocus
		 * @desc
		 * @param {Object} $event
		 * @returns
		 */
		function onFocus($event) {
			var isBound = false;
			var clicks = $document.data('events')['click'];
			for (var i = 0; i < clicks.length; i++) {
				if (clicks[i].handler.toString() === 'onBodyClick') {
					isBound = true;
					break;
				}
			}
			
			if (!isBound) {
				$document.on('click', onBodyClick);
			}
			
			vm.srchFocus = true;
		};
		
		
		/**
		 * @name onBodyClick
		 * @desc
		 * @param {Object} $event
		 * @returns
		 */
		function onBodyClick($event) {
			// .lb-srch-box내에 $event.target이 존재하는지 확인
			var len = angular.element('.lb-srch-box').find($event.target).length;
			
			// 존재하지않으면 허용하는 범위에서 클릭이 발생된것이 아님
			if (len === 0) {
				$document.off('click', onBodyClick);
				
				utilSvc.safeApply($scope, function () {
					vm.srchFocus = false;
				});
			}
		};
		
		
		/**
		 * @name getAutoframeViewName
		 * @desc
		 * @returns
		 */
		function getAutoframeViewName() {
			var name = '';
			
			if (vm.srchFocus) {
				if (typeof vm.srch === 'undefined' || vm.srch.length === 0) {
					name = 'history';
				} else {
					name = 'suggest';
				}
			}
			
			return name;
		};
		
		
		/**
		 * @name getSearchHistoryList
		 * @desc 검색 이력 목록 조회
		 * @returns
		 */
		function getSearchHistoryList() {
			return JSON.parse(browserSvc.getSearchHistoryList());
		};
		
		
		function removeSearchHistoryAll() {
			return browserSvc.removeSearchHistoryAll();
		};
		
		
		// controller destroy event
		$scope.$on('$destroy', function () {
			$log.debug('<Destory> Explorer Controller');
			
			// DOM의 양이 많을 경우 StateChange에서 Delay가 발생하므로 DOM을 제거하면 속도향상 가능
			$element.find('.lb-srch-vw').remove(); 
			
		});
		
		
		
		
		// $watch - localStorage search history
		$scope.$watchCollection(function () { return browserSvc.getSearchHistoryList(); }, function (n, o) {
			
			$log.debug('localstorage search history');
			
			if (n !== o || (n == o && vm.srchHistories.length === 0)) {
				$log.debug('local apply');
				vm.srchHistories.length = 0;

				utilSvc.safeApply($scope, function () {
					Array.prototype.push.apply(vm.srchHistories, JSON.parse(n));
				});
			}
			
		});
	}
}());
