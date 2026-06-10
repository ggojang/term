(function() {

	'use strict';

	angular
		.module('app.referenceset')
		.service('refsetservice', RefsetService);

	RefsetService.$inject = [ '$q', 'rsdataservice', 'utilservice', 'userservice' ];
	function RefsetService($q, rsdataservice, utilservice, userservice) {

		var service = this;
		
		service.initSrchVar = initSrchVar;
		
		service.getRefsetIdNm = getRefsetIdNm;
		service.getMemberList = getMemberList;
		service.getUserMemberList = getUserMemberList;
		service.getIntMemberList = getIntMemberList;
		service.apply = apply;
		service.addMember = addMember;
		service.inactiveMbr = inactiveMbr;
		service.deleteMbr = deleteMbr;
		service.restoreMbr = restoreMbr;
		service.deleteCandidateMbr = deleteCandidateMbr;
		
		service.getChildren = getChildren;
		service.setCandiMbrChdrn = setCandiMbrChdrn;
		service.setMbrChdrn = setMbrChdrn;
		service.getMemberSearchResult = getMemberSearchResult;
		
		service.convertToMember = convertToMember;
		

		/* 레퍼런스세트의 필드 정보 배열 */
		service.fields = [];
		/* 기존 멤버 정보 배열 */
		service.members = [];
		/* 새 멤버를 등록하기 위한 멤버 정보 배열 */
		service.newMembers = [];
		
		service.pathNewMembers = [];
		/* 비활성화할 멤버 목록 */
		service.inactMbrs = [];
		/* 삭제할 멤버 목록 */
		service.delMbrs = [];
		
		service.srchRslts = [];
		
		var refset = {
			id: '', // 레퍼런스세트 아이디
			name: '', // 레퍼런스세트 명
			loadingMbrs: false,
			mbrSrch: '',
			crntPage: 0,
			totalPage: 0,
			totalCount: 0
		};
		
		var originMbrs = [];
		

		/* ---------------------------------------- */
		/* Public methods */
		/* ---------------------------------------- */

		/**
		 * @name initVarAll
		 * @desc 변수 초기화
		 */
		function initVarAll() {
			service.fields.length = 0;
			initMbrVar();
			initSrchVar();
		};
		
		/**
		 * @name initMbrVar
		 * @desc 멤버관련 객체 초기화
		 */
		function initMbrVar() {
			service.pathNewMembers.length = 0;
			service.newMembers.length = 0;
			service.members.length = 0;
			service.inactMbrs.length = 0;
			service.delMbrs.length = 0;
		};
		
		/**
		 * @name initSrchVar
		 * @desc 검색관련 객체 초기화
		 */
		function initSrchVar() {
			// 멤버 검색어 초기화
			refset.mbrSrch = '';
			// 멤버 검색결과 목록 초기화
			service.srchRslts.length = 0;
		};
		
		/**
		 * @name getRefsetIdNm
		 * @desc 레퍼런스세트 아이디와 이름이 담긴 객체를 반환하는 메소드
		 */
		function getRefsetIdNm() {
			return refset;
		};

		/**
		 * @name getUserMemberList
		 * @desc 사용자 레퍼런스세트의 멤버들을 가져오는 메소드
		 * @param {string} refsetId referenceset id
		 * @param {string} refsetNm referenceset name
		 */
		function getUserMemberList(refsetId, refsetNm) {
			getMemberList(refsetId, refsetNm, 1, 500, true);
		};

		/**
		 * @name getIntMemberList
		 * @desc 국제배포판 레퍼런스세트의 목록을 반환
		 * @param {String} refsetId 레퍼런스세트 아이디
		 * @param {String} refsetNm 레퍼런스세트 이름
		 */
		function getIntMemberList(refsetId, refsetNm) {
			getMemberList(refsetId, refsetNm, 1, 500, false);
		};

		/**
		 * @name getMemberList
		 * @desc 레퍼런스세트의 멤버들을 가져오는 메소드
		 */
		function getMemberList(refsetId, refsetNm, page, size, isUser) {
			// view type; list,page,tree
			var vw = 'list';
			
			// 변수 초기화
			initVarAll();
			
			refset.loadingMbrs = true;
			
			// 멤버 목록과 필드 목록 호출
			rsdataservice.getMemberListWithDescriptorList(refsetId, vw, page, size)
			.then(function (response) {
				// members
				var mbrs = response[0].content;
				// fields
				var fields = response[1];
				// temp members
				var tmpMbrs = [];
				
				// merge fields
				angular.merge(service.fields, fields);
				
				// if view type is 1(tree)
				if (vw === 'list') {
					
				} else if (vw === 'page') {
					
				} else if (vw === 'tree') {
					// FIXME: to tree
					// tmpMbrs = getMemberTree(mbrs);
					// angular.merge(service.members, tmpMbrs);
				}
				
				// store origin members
				originMbrs = mbrs;
				// FIXME: to remove
				angular.merge(service.members, mbrs);
				
				refset.crntPage = response[0].number + 1;
				refset.totalPage = response[0].totalPages;
				refset.totalCount = response[0].totalElements;
			})
			.finally(function () {
				// 로딩 종료
				refset.loadingMbrs = false;
			});
			
			refset.id = refsetId;
			refset.name = refsetNm;
		};

		/**
		 * @name apply
		 * @desc 서버에 멤버들의 변경사항(추가, 수정, 삭제) 반영
		 * @type {Function}
		 */
		function apply() {
			updateCandiMbrs();
			
			var deferred = $q.defer();
			// {string} refsetId 레퍼런스세트 아이디
			var refsetId = refset.id;
			// {Array} mbrs 새로 추가 될 멤버 객체
			var mbrs = service.pathNewMembers;
			// {Array} 새로운 멤버 목록
			var nMbrs = [];
			// {string} 모듈 아이디
			var mdId = userservice.extension.moduleId; 
			
			var inactMbrs = [];
			var delMbrs = [];

			if ((typeof mbrs === 'undefined' || mbrs == null || mbrs.length === 0) && service.inactMbrs.length === 0 && service.delMbrs.length === 0) {
				alert('member 객체가 유효하지 않거나 상태변경 내역이 존재하지 않습니다.');
				return false;
			}
			
			// 객체 하위를 검사한 후 서버로 보낼 최종 객체를 만들어 반환
			// [{id:string, isSelf:bool}]
			
			nMbrs = checkSubtypes(mbrs);
			
			// 비활성화할 멤버가 있다면 객체 생성 후 추가
			if (service.inactMbrs.length > 0) {
				var inactMbrsLen = service.inactMbrs.length;
				for (var i = 0;i < inactMbrsLen; i++) {
					inactMbrs.push(memberMaker(service.inactMbrs[i].id, false, false));
				}
			}
			
			if (service.delMbrs.length > 0) {
				var delMbrsLen = service.delMbrs.length;
				for (var i = 0;i < delMbrsLen; i++) {
					delMbrs.push(memberMaker(service.delMbrs[i].id, false, false));
				}
			}
			// RS_DataService로부터 addRefsetMemberList메소드 호출
			if (nMbrs.length > 0 || inactMbrs.length > 0 || delMbrs.length > 0) {
				rsdataservice.addMemberList(refsetId, mdId, nMbrs, inactMbrs, delMbrs)
				.then(function(response) {
					
					deferred.resolve(true);
				}, function(err) {
					//alert('member 저장 중 문제가 발생 했습니다. : ' + err);
					return deferred.reject('member 저장 중 문제가 발생 했습니다. : ' + err);
				})
				.finally(function () {
					service.inactMbrs.length = 0;
					service.delMbrs.length = 0;
				});
			} else {
				//alert('member 객체가 유효하지 않습니다.');
				return deferred.reject('member 객체가 유효하지 않습니다.');
			}
			
			return deferred.promise;
		};
		
		
		/**
		 * @name addMember
		 * @desc 새로운 멤버를 추가하는 메소드
		 * @param {Object|Array} mbr 멤버 객체|members 멤버 객체 목록
		 * @param {string} mbr[].memberId 멤버 아이디(SNOMED CT Component ID)
		 * @param {string} mbr[].conceptId 컨셉 아이디(SNOMED CT Concept ID)
		 * @param {string} mbr[].term 용어
		 * @param {boolean} mbr[].single 자동완성으로 부터 선택했은가
		 * @param {number} mbr[].childrenCount 자식 개수
		 * @param {number} mbr[].descendantCount 자손 개수
		 */
		function addMember(mbr) {

			
			
			
			if (Array.isArray(mbr)) {
				Array.prototype.push.apply(service.newMembers, mbr);
			} else {
				service.newMembers.push(mbr);
			}
			
			// 계층구조 재생성
			updateCandiMbrs();
		};
		
		/**
		 * @name inactiveMbr
		 * @desc 멤버의 상태(active)를 변경하는 메소드. apply 실행 전까지는 로컬에서만 유효함.
		 * @param {Object}  m member object
		 * @param {string}  m.memberId
		 * @param {string}  m.conceptId
		 * @param {string}  m.term
		 * @param {number}  m.type 0: non-member, 1: member
		 * @param {number}  m.childrenCount
		 * @param {number}  m.descendantCount
		 * @param {boolean} m.single
		 * @param {boolean} m.inclSubtypes
		 * @param {Object}  m.ctx
		 * @param {boolean} m.ctx.open
		 * @param {boolean} m.ctx.loading
		 * @param {Object}  m.ctx.inact
		 * @param {boolean} m.ctx.inact.self
		 * @param {boolean} m.ctx.inact.all
		 * @param {Array}   m.subtypes
		 * @param {boolean} self 자신만 상태를 변경할 것인지? 자신을 포함한 하위 모두의 상태를 변경할 것인지?
		 */
		function inactiveMbr(m, self) {
			var ctx = m.ctx;
			if (self) {
				// inactive self
				ctx.inact.self = !ctx.inact.self;
				
				if (ctx.inact.self) {
					// to inactive
					addInactive(m);
				} else {
					// to active
					removeInactive(m);
				}
				
				//if (angular.isDefined(m.subtypes) && m.subtypes.length > 0) {
				//	_setInactSubtypes(m.subtypes, false);
				//}
				
				ctx.inact.all = false;
			}
			/*else {
				// inactive all
				m.inact.all = !m.inact.all;
				m.inact.self = false;
				
				if (angular.isDefined(m.subtypes) && m.subtypes.length > 0) {
					_setInactSubtypes(m.subtypes, m.inact.all);
				}
			}
			*/
		};
		
		/**
		 * @name deleteMbr
		 * @desc 기존 멤버를 삭제하는 메소드
		 * @param {Object} member
		 * @param {boolean} self selfOf or descendantOrSelfOf
		 */
		function deleteMbr(m, self) {
			var ctx = m.ctx;
			if (self) {
				ctx.del = !ctx.del;
				if (ctx.del) {
					service.delMbrs.push(m);
				} else {
					service.delMbrs.splice(m, 1);
				}
			}
		};
		
		function addInactive(m) {
			// inactivation
			service.inactMbrs.push(m);
		};
		
		function removeInactive(m) {
			// activation
			service.inactMbrs.splice(m, 1);
		};
		
		function _setInactSubtypes(subtypes, inact) {
			var i, len = subtypes.length, s = {};
			for (i = 0; i < len; i++) {
				s = subtypes[i];
				if (angular.isDefined(s.subtypes) && s.subtypes.length > 0) {
					s.inact.all = inact;
					s.inact.self = false;

					_setInactSubtypes(s.subtypes, inact);
				} else if (s.inclSubtypes) {
					s.inact.all = inact;
					s.inact.self = false;
				} else {
					s.inact.self = inact;
					s.inact.all = false;
				}
			}
		};
		
		function restoreMbr() {
			initMbrVar();
			
			//var hierMbrs = getMemberTree(originMbrs);
			//angular.merge(service.members, hierMbrs);
			angular.merge(service.members, originMbrs);
		};

		/**
		 * @name deleteCandidateMbr
		 * @desc 멤버 후보를 제거하는 메소드. 옵션에 따라 자신 혹은 자신을 포함한 하위 모두를 지울 수 있음
		 * @param {Object} candiMbr 삭제 대상
		 * @param {boolean} all 하위모두를 지운다면 true, 자신만 지운다면 false
		 */
		function deleteCandidateMbr(candiMbr, all) {
			console.log(candiMbr);
			var obj = {};
			var idx = 0;
			var candiMbrsLen = service.newMembers.length;
			var hasSubtype = false;
			
			if (!all) {
				// 자신만 지우는 경우
				obj = service.newMembers.find(function(o) {return o.memberId === candiMbr.conceptId});
				idx = service.newMembers.indexOf(obj);
				service.newMembers.splice(idx, 1);
			} else {
				// 자신을 포함한 하위(자식, 자손) 모두를 지우는 경우
				for (var i = candiMbrsLen - 1; i >= 0; i--) {
					
					// 경로에 지우려는 대상의 컨셉아이디가 존재하는지 확인
					hasSubtype = service.newMembers[i].path.contains(candiMbr.conceptId);
					if (hasSubtype) {
						
						// 존재한다면 삭제
						service.newMembers.splice(i, 1);
					}
				}
			}
			
			// 계층구조 재생성
			updateCandiMbrs();
		};
		
		/**
		 * @name updateCandiMbrs
		 * @desc 예비 멤버 목록으로부터 계층구조 데이터 업데이트(재생성)
		 */
		function updateCandiMbrs() {
			var tmp = [];
			tmp = getMemberTree(service.newMembers);
			service.pathNewMembers.length = 0;
			angular.merge(service.pathNewMembers, tmp);
		};
		
		/**
		 * @name getChildren
		 * @desc focus concept의 자식을 반환하는 메소드
		 * @param {string} cnptId conceptId
		 * @returns {Array} children
		 */
		function getChildren(cnptId) {
			return rsdataservice.getChildren(cnptId)
			.then(function (chdrn) {
				return chdrn;
			});
		};
		
		/**
		 * @name setCandiMbrChdrn
		 * @desc 예비 멤버의 자식을 반환받고, 예비멤버 목록과 예비멤버 계층에 각각 반영하는 메소드
		 * @param {Object} candiMbr candidate member
		 * @param {string} candiMbr.memberId
		 * @param {string} candiMbr.conceptId
		 * @param {string} candiMbr.term
		 * @param {boolean} candiMbr.single
		 * @param {number} candiMbr.childrenCount
		 * @param {number} candiMbr.descendantCount
		 * @param {Array} candiMbr.subtypes
		 * @param {Object} mbr.ctx context
		 * @param {boolean} mbr.ctx.open
		 * @param {boolean} mbr.ctx.loading
		 * @returns
		 */
		function setCandiMbrChdrn(candiMbr, chdrn) {
			console.log('@@', JSON.stringify(candiMbr));
			var chdrnLen = chdrn.length;
			var type = candiMbr.type;
			var cmbrs = [];
			var chd;
			for (var i = 0; i < chdrnLen; i++) {
				// 용도1: 예비멤버목록
				// 용도2: 예비멤버계층
				// 용도 구분 사유: 자식을 호출하는데 목록에 반영후 계층을 재생성하면 뷰전체가 움직인다.
				//             따라서, 예비멤버목록과 계층에 각각 반영하면 현 상태에서 자식이 반영되고
				//             예비멤버를 지우게 될 때는 예베멤버목록에 이미 반영되어있으므로 지우고 계층을 재생성하면 된다.
				// FIXME: impl
				// 자식 객체
				chd = chdrn[i];
				
				// 콘텍스트 객체 설정
				chd.ctx = {
					open: false,
					loading: false
				};
				// 타입 설정
				chd.type = type;
				
				if (chd.childrenCount > 0) {
					chd.inclSubtypes = true;
				}
				// 자식의 하위 포함여부 설정
				chd.inclSubtypes = chd.childrenCount > 0 ? true:false;
				
				cmbrs.push(convertToMember(chd, false));
			}
			candiMbr.subtypes = chdrn;
			// service.newMembers 구조
			/*
			{
				"memberId": "74400008",
				"term": "Appendicitis",
				"path": "138875005~404684003~64572001~128139000~363170005~363169009~302168000~74400008",
				"isSingle": false,
				"isPrim": "900000000000073002",
				"childrenCount": 14,
				"descendantCount": 30
			}
			 */
			
			
			
			// 예비 멤버목록에 자식 추가
			Array.prototype.push.apply(service.newMembers, cmbrs);
		};
		
		/**
		 * @name setMbrChdrn
		 * @desc 
		 * @param {Object} mbr member
		 * @param {string} mbr.memberId
		 * @param {string} mbr.conceptId
		 * @param {string} mbr.term
		 * @param {boolean} mbr.single
		 * @param {number} mbr.childrenCount
		 * @param {number} mbr.descendantCount
		 * @param {Array} mbr.subtypes
		 * @param {Object} mbr.ctx context
		 * @param {boolean} mbr.ctx.open
		 * @param {boolean} mbr.ctx.loading
		 * @returns
		 */
		function setMbrChdrn(mbr, chdrn) {
			
			var chdrnLen = chdrn.length;
			var type = mbr.type;
			var chd;
			for (var i = 0; i < chdrnLen; i++) {
				// 용도1: 예비멤버목록
				// 용도2: 예비멤버계층
				// 용도 구분 사유: 자식을 호출하는데 목록에 반영후 계층을 재생성하면 뷰전체가 움직인다.
				//             따라서, 예비멤버목록과 계층에 각각 반영하면 현 상태에서 자식이 반영되고
				//             예비멤버를 지우게 될 때는 예베멤버목록에 이미 반영되어있으므로 지우고 계층을 재생성하면 된다.
				// FIXME: 멤버용 객체로 전부 변환할 것
				
				// 자식 객체
				chd = chdrn[i];
				
				// 콘텍스트 객체 설정
				chd.ctx = {
					open: false,
					loading: false
				};
				// 타입 설정
				chd.type = type;
				
				if (chd.childrenCount > 0) {
					chd.inclSubtypes = true;
				}
				// 자식의 하위 포함여부 설정
				chd.inclSubtypes = chd.childrenCount > 0 ? true:false;
			}
			mbr.subtypes = chdrn;
		};
		
		/**
		 * @name getMemberSearchResult
		 * @desc 
		 * @param {string} refsetId 레퍼런스세트 아이디
		 * @param {string} srch 검색어
		 * @returns
		 */
		function getMemberSearchResult(refsetId, srch, page, size) {
			
			// FIXME: check if numeric & digit(6~18)
			
			// 멤버검색결과 목록 초기화
			service.srchRslts.length = 0;
			
			return rsdataservice.getMemberSearchResult(refsetId, srch, page, size)
			.then(function (rslt) {
				angular.merge(service.srchRslts, rslt);
				
				return rslt;
			});
		};
		
		/**
		 * @name convertToMember
		 * @desc 
		 * @param {Object} cpt 컨셉
		 * @param {boolean} cpt.conceptActive
		 * @param {boolean} cpt.descriptionActive
		 * @param {string} cpt.conceptId
		 * @param {string} cpt.fsn
		 * @param {number} cpt.length // TODO 무조건 0으로 들어옴
		 * @param {string} cpt.term
		 * @param {string} cpt.path
		 * @param {boolean} single 자동완성에서 선택한 단건인지 여부
		 * @returns {Object} mbr 멤버 
		 */
		function convertToMember(cnpt, single) {
			var isPrim = false
			var path = '';
			
			// isPrim 처리
			// TODO 자동완성 검색결과에서 얻는 객체와 트리에서 얻는 객체가 달라 발생하는 처리
			if (angular.isDefined(cnpt.definitionStatus)) {
				isPrim = cnpt.definitionStatus.id;
			} else if (angular.isDefined(cnpt.definitionStatusId)) {
				isPrim = cnpt.definitionStatusId === '900000000000074008' ? true:false;
			} else {
				alert('convertToMember:definitionStatusId가 올바르지 않습니다. : ' + JSON.stringify(cnpt));
			}
			
			// path 처리
			path = cnpt.path + '~' + cnpt.conceptId;
			
			return {
				memberId: cnpt.conceptId,
				term: cnpt.term,
				path: path,
				isSingle: single,
				isPrim: isPrim,
				childrenCount: cnpt.childrenCount ? cnpt.childrenCount:0,
				descendantCount: cnpt.descendantCount ? cnpt.descendantCount:0
			};
		};
		
		
		/* ---------------------------------------- */
		/* Private methods */
		/* ---------------------------------------- */

		// list of method
		//
		// _recursiveMemberCheck
		// _isSelf
		// memberMaker

		
		/**
		 * @name checkSubtypes
		 * @desc
		 * @param {Array} mbrs member 후보 목록
		 * @param {string} mbrs[].memberId 멤버 아이디(SNOMED CT Component ID)
		 * @param {string} mbrs[].conceptId 컨셉 아이디(SNOMED CT Concept ID)
		 * @param {string} mbrs[].term 용어
		 * @param {boolean} mbrs[].single 자동완성으로 부터 선택했은가
		 * @param {number} mbrs[].childrenCount 자식 개수
		 * @param {number} mbrs[].descendantCount 자손 개수
		 * @returns {Array}
		 */
		function checkSubtypes(mbrs) {
			// 선택 후 멤버리스트에서 편집 하지 않은 경우
			// 경우1. (자동완성) 단건 추가:  = true
			// 경우2. (계층구조) 단건/다건 추가: isSingle = false
			// 경우3. (자동완성) 단건 추가 + (계층구조) 하위 추가

			// 선택 후 멤버리스트에서 편집 된 경우
			// 경우4. (자동완성) 단건 추가 후 삭제 -> 이 함수로 오기전에 해당객체는 제거됨.
			// 경우5. (계층구조) 자식이있는 단건/다건 추가 후 자식 삭제 

			// 객체구조
			// obj = { id: 'conceptId', memberType: typeNum };
			// isSelf 정의: 0=self, 1=descendantOrSelfOf, descendantOf는 고려히자않음(0과 1으로 해결가능)

			// 변수 선언 및 초기화
			var nMbrs = [], nMbr = null, mbr = null, mbrSubtypes = [], len = mbrs.length, mbrId = '',
				inclSubtypes = false, single = false,
				origChdCnt = 0;

			for (var i = 0; i < len; i++) {
				// i번째 member후보
				mbr = mbrs[i];
				// 멤버 아이디
				mbrId = angular.isDefined(mbr.memberId) ? mbr.memberId:mbr.conceptId;
				// 자식 수
				origChdCnt = mbr.childrenCount;
				// 자식객체 목록(실제 자식이 아닐 수 있음 => 트리로 객체를 구성했기때문에 descendant라도 자식으로 붙게 구성됨)
				mbrSubtypes = mbr.subtypes ? mbr.subtypes : [];
				// 자동완성 또는 계층구조의 컨텍스트메뉴로 부터 추가되었는지에 대한 여부
				single = angular.isDefined(mbr.single) ? mbr.single: false;
				// 나만 추가되거나 나를 포함하여 하위 일부를 포함한다면 true, 나를 포함한 하위 전체를 포함한다면 false
				inclSubtypes = _isInclSubtypes(origChdCnt, single, mbrSubtypes);

				// i번째 member후보의 정보를 이용하여 신규 member 오브젝트 생성
				nMbrs.push(
					memberMaker(
						mbrId, // SNOMEDCT Referenceset의 referencedComponentId로 기록되는 아이디
						inclSubtypes, // true: self, false: descendantOrSelfOf로 (서버에서) 처리
						true // active
					)
				);
				// toRemove: console.log('mbr: ' + mbr.conceptId + ' / ' + mbr.term);

				if (mbrSubtypes.length > 0) {
					var rtn = checkSubtypes(mbrSubtypes);
					Array.prototype.push.apply(nMbrs, rtn);
				}
			}
			return nMbrs;
		};

		/**
		 * @name _isInclSubtypes
		 * @desc 새로 추가하려는 멤버에 대하여 자신만 포함하는지 아니면 하위모두를 포함하는지 확인하는 메소드 
		 * @param {number} origChdCnt 자식 수
		 * @param {booelan} fromSgst 자동완성 결과에서 단건 선택
		 * @param {boolean} subtypes 하위 목록 
		 * @returns
		 */
		function _isInclSubtypes(origChdCnt, fromSgst, subtypes) {
			// 참: self
			//   - 조건1: 멤버후보(mbr)의 자식수(chdCnt)가 0일 경우 // from 자동완성 선택 또는 계층구조의 체크 박스 
			//   - 조건2: 자식수(chdCnt)가 0보다 크고 단건 선택일 경우 // from 자동완성 선택 또는 계층구조의 컨텍스트 메뉴
			//   - 조건3: 단건 선택이 아니면서 원래 자식이 존재하고 하위가 존재하는 경우
			// 거짓: desdendantOrSelfOf 또는 다른 조건에 의해 self
			//   - 조건1: 하위를 열지않은 트리인 경우(무편집) chdCnt > 0 && !isSingle && subtypes.length === 0
			//   - 조건2: 나머지
			return (origChdCnt > 0 && !fromSgst && subtypes.length === 0) ? true : false;
			
			//origChdCnt === 0 || (origChdCnt > 0 && fromSgst)|| (!fromSgst && origChdCnt > 0 && subtypes.length > 0) ? true: false;
		};

		/**
		 * @name memberMaker
		 * @desc 레퍼런스세트 멤버 객체 생성/반환하는 메소드
		 * @param {string} memberId 레퍼런스세트 멤버 아이디
		 * @param {boolean} inclSubtypes 자식,자손 포함 여부 
		 * @returns {Object} 레퍼런스세트 멤버 객체
		 */
		function memberMaker(mbrId, inclSubtypes, act) {
			return {
				memberId : mbrId,
				conceptId : mbrId,
				includeSubtypes : inclSubtypes//,
				//active: act
			};
		};

		/**
		 * @name getMemberTree
		 * @desc 레퍼런스세트 멤버들의 경로로 트리 객체를 반환하는 메소드
		 * @param {Array} members 멤버 객체 목록
		 * @param {number} members[].childrenCount 자식 개수
		 * @param {number} members[].descendantCount 자손 개수
		 * @param {boolean} members[].isPrim 컨셉 형태가 Primitive이거나 Defined이거나
		 * @param {boolean} members[].isSingle 자동완성에서 선택한 컨셉인가
		 * @param {string} members[].memberId 멤버 아이디
		 * @param {string} members[].path 루트부터 자신까지의 전체 경로
		 * @param {string} members[].term 용어
		 * @return {Array} 계층형태의 멤버 객체 목록
		 */
		function getMemberTree(members) {
			// 변수 선언 및 초기화
			var tree = [], paths = [], parts = [], crntNode = [], // Array
				mbrsLen = 0, pathsLen = 0, partsLen = 0, i = 0, // Number
				path = '', part = ''; // String
			
			// members 배열에서 path 속성만 가져와 paths 배열 구성
			mbrsLen = members.length;
			for (i = 0; i < mbrsLen; i++) {
				paths.push(members[i].path);
			}
			// paths 배열 길이
			pathsLen = paths.length;
			// paths
			for (i = 0; i < pathsLen; i++) {
				path = paths[i];
				
				if (path.contains('undefined')) {
					alert("[refsetservice] func:_setTree ==> 경로에 유효하지 않은 값이 존재합니다. " + path);
				}
				// 경로를 구분자로 분리
				parts = path.split('~');
				crntNode = tree;
				partsLen = parts.length;
				part = '';

				// 루트까지의 경로를 구분자로 분리 후 각 아이템(컨셉아이디)을 순환
				for (var j = 0; j < partsLen; j++) {
					// 컨셉 아이디
					part = parts[j];
					var isLast = j === partsLen - 1, partObj = crntNode.find(function(o) {return o.memberId === part});
					if (partObj) {
						crntNode = partObj.subtypes;

						if (isLast) {
							partObj.enabled = true;
						}
					} else {
						var node = {
							memberId : part,
							subtypes : []
						};

						if (isLast) {
							node.enabled = true;
						}

						crntNode.push(node);
						crntNode = node.subtypes;
					}
				}
			};
			return setExistTree(members, tree, []);
		};

		function setExistTree(members, obj, node) {
			var o = {}, tmp = [], 
				memberId = '',
				len = obj.length;
			
			for (var i = 0; i < len; i++) {
				o = obj[i];
				memberId = o.memberId;
				if (o.enabled) {
					var mbr = members.find(function(w) {
						return w.memberId === memberId;
					});
					var c = {
						memberId: memberId,
						conceptId: memberId,
						term: mbr.term,
						single: mbr.isSingle,
						type: angular.isDefined(mbr.isSingle) ? 0:1,
						childrenCount: mbr.childrenCount ? mbr.childrenCount : 0,
						descendantCount: mbr.descendantCount ? mbr.descendantCount : 0,
						inclSubtypes: angular.isDefined(mbr.includeSubtypes) ? mbr.includeSubtypes:(!mbr.isSingle && mbr.childrenCount > 0 ? true:false),
						ctx: {
							open: false,
							loading: false
						}
					};
					if (o.subtypes.length > 0) {
						c.subtypes = [];
						c.ctx.open = true;
					}
					node.push(c);
					tmp = c.subtypes;
				} else {
					tmp = node;
				}
				if (o.subtypes.length > 0) {
					setExistTree(members, o.subtypes, tmp);
				}
			}
			return node;
		};
		
		
	};

})();