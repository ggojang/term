(function () {
	
	'use strict';
	
	angular
		.module('app.referenceset')
		.controller('MemberController', MemberController);
	
	MemberController.$inject = ['$scope', '$log', '$state', '$stateParams', '$window', '$timeout', '$element', '$document', '$mdDialog', 'userservice', 'apiservice', 'utilservice', 'refsetservice', 'srchservice', 'rsdataservice'];
	function MemberController($scope, $log, $state, $stateParams, $window, $timeout, $element, $document, $mdDialog, userservice, apiservice, utilservice, refsetservice, srchservice, rsdataservice) {
		
		var vm = this;
		
		/* ---------------------------------------- */
		/* Declare methods */
		/* ---------------------------------------- */
		
		vm.termBeautifier = utilservice.termBeautifier;
		vm.alt = refsetservice.alt;
		vm.apply = apply;

		vm.getChildren = getChildren;
		
		vm.deleteCandidateMbr = refsetservice.deleteCandidateMbr;
		
		vm.getStateClass = getStateClass;
		vm.setMode = setMode;
		vm.inactiveMbr = refsetservice.inactiveMbr;
		vm.deleteMbr = refsetservice.deleteMbr;
		vm.showProp = showProp;
		
		vm.onKeyup = onKeyup;
		vm.search = search;
		
		vm.nextPage = nextPage;
		vm.previousPage = previousPage;
		
		/* ---------------------------------------- */
		/* Declare variables */
		/* ---------------------------------------- */
		
		vm.refsetId = $stateParams.code;
		vm.refsetNm = $stateParams.name;
		vm.release = $stateParams.release;
		
		// 필드뷰 보여짐에 대한 판단
		vm.viewProp = false;
		vm.srch = '';
		
		//vm.refset;
		//vm.candidateMbrs;
		//vm.mbrs;
		//vm.srchRslts;
		
		// 페이지 관련
		var DEFAULT_PAGE = 1,
			DEFAULT_SIZE = 500;
		
		// 스타일 관련
		var clsROpen = 'glyphicon-triangle-bottom';
		var clsRClose = 'glyphicon-triangle-right';
		var clsLeaf = 'glyphicon-minus';
		var clsLoad = 'glyphicon-refresh icon-spin';
	
		var elPanel;
		
		
		activate();
		
		/**
		 * @name active
		 * @desc 엔트리 포인트
		 */
		function activate() {
			
			if (vm.release === 'user') {
				refsetservice.getMemberList(vm.refsetId, vm.refsetNm, DEFAULT_PAGE, DEFAULT_SIZE, true);
			} else if (vm.release === 'int') {
				refsetservice.getMemberList(vm.refsetId, vm.refsetNm, DEFAULT_PAGE, DEFAULT_SIZE, false);
			} else {
				alert('Release Not Found!');
				$state.go('refset');
				return false;
			}
			
			// 아이디와 이름 객체 참조
			vm.refset = refsetservice.getRefsetIdNm();
			// 멤버 목록 참조
			vm.mbrs = refsetservice.members;
			// 예비 멤버 목록 참조
			vm.candidateMbrs = refsetservice.pathNewMembers;
			// 멤버 검색 결과 목록 참조
			//vm.srchRslts = refsetservice.srchRslts;
			
			// 브라우저 크기 조절 이벤트 설정
			angular.element($window)
				.bind('resize', resize);
			
			// 브라우저 크기 조절 실행
			$timeout(function() {
				resize();
			});
			
			// 필드 속성 엘리먼트 참조
			elPanel = $element.find('#re-m-panel');
			elPanel.children('.p_splitter').on('mousedown', function (e) {
				e.preventDefault();
				
				// top: 도큐멘트를 기준으로 패널의 Y
				// left: 도큐멘트를 기준으로 패널의 X
				var elPOffset = elPanel.offset();
				
				$document.on('mousemove', function (e) {
					splrResize(elPanel.outerWidth(), 5, elPOffset.left ,e.pageX);
				});
				
				$document.on('mouseup mouseleave', function (e) {
					console.log('off event');
					$document.off('mousemove mouseup');
				});
				
			});
		};
		
		/**
		 * @name apply
		 * @desc 서버에 멤버들의 변경사항(추가, 수정, 삭제) 반영
		 * @param {string} refsetId 레퍼런스세트 아이디
		 * @param {Array} mbrs 멤버 목록
		 */
		function apply($event) {
			console.log($event);
			$mdDialog.show({
				controller: 'ConfirmDialogController',
				templateUrl: 'resources/template/refset/cfmDlg.html',
				parent: angular.element(document.body),
				clickOutsideToClose:false,
				targetEvent: $event
			})
			.then(function () {
				// resolved
				// 사용자 멤버 목록 호출
				refsetservice.getUserMemberList(vm.refset.id, vm.refset.name);
			}, function () {
				// reject
			});
		};
		
		/**
		 * @name _getChildren
		 * @desc 
		 * @param {Object} mbr
		 * @param {string} mbr.memberId
		 * @param {string} mbr.conceptId
		 * @param {string} mbr.term
		 * @param {boolean} mbr.single
		 * @param {number} mbr.childrenCount
		 * @param {number} mbr.descendantCount
		 * @param {Array} mbr.subtypes
		 */
		function getChildren(mbr) {
			
			var type = mbr.type;
			
			// 오픈 상태 반전
			mbr.ctx.open = !mbr.ctx.open;
			
			// 반환 조건(각 조건간의 관계는 OR)
			// 1. 자식 수가 0
			// 2. 이미 불러온 경우(배열이 존재)
			// 3. 자식 수가 존재하지만 하위가 멤버로 포함되지 않은 경우
			if (mbr.childrenCount === 0 || angular.isDefined(mbr.subtypes) || (mbr.childrenCount > 0 && !mbr.inclSubtypes)) {
				return;
			}
			
			// 로딩 시작
			mbr.ctx.loading = true;
			
			refsetservice.getChildren(mbr.conceptId)
			.then(function (chdrn) {
				
				if (type === 1) {
					// if member
					refsetservice.setMbrChdrn(mbr, chdrn);
				} else {
					// if candidate member
					refsetservice.setCandiMbrChdrn(mbr, chdrn);
				}
				
			})
			.finally(function () {
				
				// 로딩 종료
				mbr.ctx.loading = false;
			});
		};


		/**
		 * @name _getChildren
		 * @desc 
		 * @param {Object} mbr
		 * @param {string} mbr.memberId
		 * @param {string} mbr.conceptId
		 * @param {string} mbr.term
		 * @param {boolean} mbr.single
		 * @param {number} mbr.childrenCount
		 * @param {number} mbr.descendantCount
		 * @param {Array} mbr.subtypes
		 * 
		 * @deprecated
		 */
		function _getChildren(mbr) {
			
			//ng-click nm.childrenCount === 0 ? (nm.subtypes ? m.getChildren(nm):null):m.getChildren(nm)
			
			// 오픈 상태 반대로 설정
			mbr.ctx.open = !mbr.ctx.open;
			
			// 반환 조건(각 조건간의 관계는 OR)
			// 1. 자식 수가 0
			// 2. 이미 불러온 경우(배열이 존재)
			// 3. 자식 수가 존재하지만 하위가 멤버로 포함되지 않은 경우
			if (mbr.childrenCount === 0 || angular.isDefined(mbr.subtypes) || (mbr.type === 1 && mbr.childrenCount > 0 && !mbr.inclSubtypes)) {
				return;
			}
			
			// 로딩 시작
			mbr.ctx.loading = true;
			
			apiservice.getChildren(mbr.conceptId, userservice.effectiveTime)
			.then(function (chdrn) {
				/* Example :
				 * mbr의 구조와 children의 구조가 다름
				 * {
						"active": true,
						"conceptId": "38033009",
						"term": "Amputation stump",
						"semanticTag": "body structure",
						"definitionStatus": {
							"id": "900000000000074008",
							"name": "Primitive"
						},
						"childrenCount": 0,
						"descendantCount": 0,
						"$$hashKey": "object:1566"
					}
				 */
				var type = mbr.type, inclSubtypes = mbr.inclSubtypes, i, chdLen;
				for (i = 0, chdLen = chdrn.length; i < chdLen; i++) {
					chdrn[i].ctx = {
						open: false,
						loading: false
					};
					// 부모 타입을 자식 타입으로 설정
					chdrn[i].type = type;
					if (type === 1 && chdrn[i].childrenCount > 0) {
						chdrn[i].inclSubtypes = true;
					} else {
						chdrn[i].inclSubtypes = false;
					}
				}
				
				mbr.subtypes = chdrn;
			})
			.finally(function () {
				mbr.ctx.loading = false;
			});
		};
		
		/**
		 * @name getStateClass
		 * @desc 현재 상태에 대한 클래스를 반환
		 * @param {Object} mbr 멤버
		 * @param {string} mbr.memberId
		 * @param {string} mbr.conceptId
		 * @param {string} mbr.term
		 * @param {number} mbr.type 0: non-member, 1: member
		 * @param {number} mbr.childrenCount
		 * @param {number} mbr.descendantCount
		 * @param {boolean} mbr.single
		 * @param {boolean} mbr.inclSubtypes
		 * @returns {string} cls className
		 */
		function getStateClass(mbr) {
			/*
			 *  var clsROpn = 'glyphicon-triangle-bottom',
				clsRCls = 'glyphicon-triangle-right',
				clsLeaf = 'glyphicon-minus',
				clsLoad = 'glyphicon-refresh icon-spin';
			 */
			
			var cls = '', chdCnt = 0,
				inclSub = false, open = false, loading = false, existSub = false;
			
			chdCnt = mbr.childrenCount;
			existSub = angular.isDefined(mbr.subtypes);
			inclSub = angular.isDefined(mbr.inclSubtypes) ? mbr.inclSubtypes:null;//(angular.isDefined(mbr.subtypes) ? true:false),
			
			if (angular.isDefined(mbr.ctx)) {
				open = angular.isDefined(mbr.ctx.open) ? mbr.ctx.open:false;
				loading = angular.isDefined(mbr.ctx.loading) ? mbr.ctx.loading:false;//(angular.isDefined(mbr.subtypes) ? true:false),
				
				if (loading) {
					cls = clsLoad;
				} else {
					cls = chdCnt > 0 ? (existSub ? (open ? clsROpen:clsRClose):(inclSub === null ? clsRClose:(inclSub ? clsRClose:clsLeaf)) ):clsLeaf;
				}
			}
			
			//cls = chdCnt > 0 && inclSub || inclSub ? (!open ? clsRClose:(loading ? clsLoad:clsROpen)):clsLeaf;
			return cls;
			
			//;nm.childrenCount === 0 ? (nm.subtypes ? (nm.open ? 'glyphicon-triangle-bottom':'glyphicon-triangle-right'):'glyphicon-minus'):(!nm.open ? 'glyphicon-triangle-right':(nm.loading ?  'glyphicon-refresh icon-spin':'glyphicon-triangle-bottom'))
		};
		
		/**
		 * @name setMode
		 * @desc
		 * @param {boolean} mode
		 */
		function setMode(mode) {
			//;m.refsetId.length > 7 ? (rs.viewSrch = !rs.viewSrch):m.alt('레퍼런스세트를 선택해 주세요!')
			var m = mode;
			if (vm.refset.id.length < 8 || vm.refset.id.length > 18) {
				alert('레퍼런스세트를 선택해 주세요!');
				m = !m;
			}
			
			if (!m) {
				// 편집모드 해제로 변경 할 경우
				// restore variables releate mbrs
				refsetservice.restoreMbr();
				
				// init variables related srch
				srchservice.initVar();
				
				vm.viewProp = false;
			} else {
				vm.viewProp = true;
				showProp(true);
			}
			
			refsetservice.initSrchVar();
			
			return m;
		};
		
		/**
		 * @name onKeyup
		 * @desc 멤버 검색창에서 키 입력시 발생하는 이벤트를 처리하는 메소드
		 * @param {string} word
		 * @param {Object} $event
		 * @returns
		 */
		function onKeyup(word, $event) {
			// 키코드
			var keyCode = $event.keyCode;
			
			// 엔터키
			if (keyCode === 13) {
				// 멤버 검색
				search(word, DEFAULT_PAGE, DEFAULT_SIZE);
			}
		};
		
		
		/**
		 * @name search
		 * @desc 멤버 검색
		 * @param {String} word 검색어
		 */
		function search(word, page, size) {
			refsetservice.getMemberSearchResult(refsetservice.getRefsetIdNm().id, word, page, size)
			.then(function (data) {
				// FIXME ui-sref 방식으로 변경시 수정
				refsetservice.members.length = 0;
				if (data.numberOfElements > 0) {
					Array.prototype.push.apply(refsetservice.members, data.content);
					
					// 페이지 번호 반영
					setPage(data.number, data.totalPages);
					
					vm.refset.totalCount = data.totalElements;
				} else {
					setPage(0, 0);
					vm.refset.totalCount = 0;
				}
			});
		};
		
		
		/**
		 * @name setPage
		 * @desc 현재 페이지 수와 전체 페이지 수 설정
		 * @param {Numeric} crntPage 현재 페이지 수
		 * @param {Numeric} totalPage 전체 페이지 수
		 */
		function setPage(crntPage, totalPage) {
			vm.refset.crntPage = crntPage + 1;
			vm.refset.totalPage = totalPage;
		};
		
		
		function nextPage(crntPage) {
			callPage(vm.refset.crntPage + 1, DEFAULT_SIZE);
		};
		
		function previousPage(crntPage) {
			callPage(vm.refset.crntPage - 1, DEFAULT_SIZE);
		};
		
		
		function callPage(page, size) {
			if (vm.release === 'user') {
				refsetservice.getMemberList(vm.refsetId, vm.refsetNm, page, size, true);
			} else if (vm.release === 'int') {
				if (typeof vm.refset.mbrSrch !== 'undefined') {
					search(vm.refset.mbrSrch, page, size);
				} else {
					refsetservice.getMemberList(vm.refsetId, vm.refsetNm, page, size, false);
				}
				
			} else {
				alert('Release Not Found!');
			}
		};
		
		
		/**
		 * @name splrResize
		 * @desc Member리스트에서 Properties가 보이는 상태에서 스플리터를 움직일때 사이즈를 조절
		 * @param {number} panelW
		 * @param {number} splrW
		 * @param {number} panelX
		 * @param {number} moveX
		 */
		function splrResize(panelW, splrW, panelX, moveX) {
			var mW = 0, sW = 0, sL = 0;
			
			moveX = moveX === -1 ? panelX+300: moveX;
			
			mW = moveX - panelX;
			sW = panelW - mW - splrW;
			sL = mW + splrW;
			if (mW >= 300 && sW >= 300) {
				elPanel.find('.p_master').css('width', mW + 'px');
				elPanel.find('.p_splitter').css('left', mW + 'px');
				elPanel.find('.p_slave').css('left', sL + 'px').css('width', sW + 'px');
			}
		};
		
		/**
		 * @name resize
		 * @desc 브라우저 크기가 변경 되었을 경우 Member내 높이 조절
		 */
		function resize() {
			var bodyH = 0, headerH = 0, toolH = 0, titleH = 0, srchH = 0;
			
			bodyH = angular.element('body').css('height').replace('px', '');
			headerH = angular.element('#header').css('height').replace('px', '');
			toolH = $element.find('.rs_head').css('height').replace('px', '');
			titleH = $element.find('.rs_head_titlebar').css('height').replace('px', '');
			srchH = $element.find('.rs_head_srch').css('height').replace('px', '');
			// treeTable 높이 설정
			angular.element('#re-m-mbrs').css('height', (bodyH-headerH-toolH-titleH-srchH-35-1) + 'px');
			// -1은 border-top
			elPanel.css('height', (bodyH-headerH-toolH-titleH-1) + 'px');
		};
		
		/**
		 * @name showProp
		 * @desc
		 * @param edit
		 * @returns
		 */
		function showProp(edit) {
			var bodyW = 0, panelW = 0, mstrW = 0, slvW = 0, splrW = 5, sdbarW = 0,
				panelX = 0,
				slvMinW = 300;
			// properties의 최소 너비는 300px
			// properties가 활성화 된 상태일 때, 감지해야할 이벤트 목록
			// 1. Edit버튼 선택 시
			// 2. Done버튼 선택 시
			// 3. >>  버튼 선택 시
			// 4. <<  버튼 선택 시
			// 5. 서치&쿼리패널 Splitter 이동 시
			// 6. 세트목록 패널 Splitter 이동 시
			// 7. Window 리사이즈 시
			
			
			bodyW = parseInt(angular.element('body').css('width'), 10);
			panelW = parseInt(elPanel.css('width'), 10);
			// sidebar width
			sdbarW = edit ? 40 : 0; //parseInt(angular.element('.iui-sidebar').css('width'), 10);
	
			panelW = panelW - sdbarW;
			
			panelX = bodyW - sdbarW - panelW;
			
			mstrW = panelW - slvMinW - splrW;
			
			// member 영역의 각종 너비 설정
			// 왼쪽 패널 너비 설정
			elPanel.find('.p_master').css('width', mstrW + 'px');
			// Splitter 왼쪽 시작지점 설정
			elPanel.find('.p_splitter').css('left', mstrW + 'px');
			// 오른쪽 패널 왼쪽 시작지점, 너비 설정
			elPanel.find('.p_slave').css('left', (mstrW + splrW) + 'px').css('width', slvMinW + 'px');
		};
		
	};
	
})();