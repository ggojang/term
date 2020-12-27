(function() {

	'use strict';

	angular
		.module('app.referenceset')
		.service('rsdataservice', RsDataService);

	RsDataService.$inject = [ '$q', 'apiservice', 'userservice' ];
	function RsDataService($q, apiservice, userservice) {
		
		var service = this;
		
		// 국제배포판 레퍼런스세트 목록 가져오는 메소드
		service.getIntRefsetList = getIntRefsetList;
		
		// 사용자 레퍼런스세트 목록을 가져오는 메소드
		service.getUserTree = getUserTree;
		
		// 사용자 레퍼런스세트 목록을 저장하는 메소드
		service.saveUserTree = saveUserTree;
		
		// 새로운 레퍼런스 세트를 생성하는 메소드
		service.addRefset = addRefset;
		service.getChildren = getChildren;
		service.getMemberSearchResult = getMemberSearchResult;
		service.getMemberListWithDescriptorList = getMemberListWithDescriptorList;
		service.getSimpleMemberList = getSimpleMemberList;
		
		service.addMemberList = addMemberList;
		
		
		/**
		 * @name getRefsetIdList
		 * @desc 레퍼런스세트의 아이디 목록 조회
		 * @param {String} release 릴리즈형태; 허용값:itn, user
		 * @param {Boolean} hasMbrs 레퍼런스세트 내에 속한 멤버가 하나이상인지 아닌지 여부
		 */
		function getRefsetIdList(release, hasMbrs) {
			var oper = '/refsets/SNOMEDCT?release=' + release;
			
			if (typeof hasMbrs !== 'undefined') {
				oper += '&hasmbrs=' + (hasMbrs ? '1':'0');
			}
			
			return apiservice.get(oper)
			.then(getRefsetIdListComplete, getRefsetIdListFailed);
			
			function getRefsetIdListComplete(response) {
				return release === 'user' ? JSON.parse(response.data):response.data;
			};
			
			function getRefsetIdListFailed(err) {
				// FIXME: logger.error();
			};
		};
		
		
		/**
		 * @name getIntRefsetList
		 * @desc 국제 배포판의 레퍼런스세트 목록 및 자식 목록 조회
		 * 
		 */
		function getIntRefsetList() {
			var deferred = $q.defer();

			$q.all([
				getChildren('900000000000455006', userservice.effectiveTime),
				getRefsetIdList('itn', true)
			]).then(function(response) {
				deferred.resolve(response);
			}, function(error) {
				deferred.reject(error);
			});

			return deferred.promise;
		};
		
		
		/**
		 * @name getUserTree
		 * @desc 사용자 레퍼런스세트 목록을 가져오는 메소드
		 */
		function getUserTree() {
			var oper = '/refsets/SNOMEDCT?release=user';
			
			return apiservice.get(oper)
			.then(getUserTreeComplete, getUserTreeFailed);
			
			function getUserTreeComplete(response) {
				return JSON.parse(response.data);
			};
			
			function getUserTreeFailed(err) {
				// FIXME: logger.error();
			};
		};
		
		/**
		 * @name saveUserTree
		 * @desc 사용자 레퍼런스세트 목록을 저장하는 메소드
		 */
		function saveUserTree(usrs) {
			//var oper = '/snomedct/refset/user';
			var oper = '/refset/SNOMEDCT?release=user';
			
			return apiservice.post(oper, JSON.stringify(usrs), { headers: {'Content-Type':'text/plain'} })
			.then(saveUserTreeComplete, saveUserTreeFailed);
			
			function saveUserTreeComplete(response) {
				return JSON.parse(response.data);
			};
			
			function saveUserTreeFailed(err) {
				// FIXME: logger.error();
			};
		};
		
		/**
		 * @name addRefset
		 * @desc 새로운 레퍼런스 세트를 생성하는 메소드
		 */
		function addRefset(r) {
			var oper = '/snomedct/refset/members';
			return apiservice.post(oper, r)
			.then(addRefsetComplete, addRefsetFailed);
			
			function addRefsetComplete() {
				return true;
			};
			
			function addRefsetFailed(err) {
				// FIXME: logger.error();
			};
		};
		
		/**
		 * @name getChildren
		 * @desc 자식을 가져오는 메소드
		 */
		function getChildren(cnptId) {
			//var oper = '/snomedct/concept/' + userservice.effectiveTime + '/' + cnptId + '/' + 'children';
			var oper = '/children/SNOMEDCT/' + cnptId + '?version=v' + userservice.effectiveTime;
			
			return apiservice.get(oper)
			.then(getChildrenComplete, getChildrenFailed);
			
			function getChildrenComplete(response) {
				return response.data;
			};
			
			function getChildrenFailed() {
				// FIXME: logger.error();
			};
		};
		
		/**
		 * @name getDescriptorList
		 * @desc 레퍼런스세트의 필드정보를 가져오는 메소드
		 */
		function getDescriptorList(refCpntId) {
			
			// operator
			var oper = '/descriptors/SNOMEDCT/' + refCpntId;
			
			// set effective time
			//oper += '&version=v' + userservice.effectiveTime;
			
			// request
			return apiservice.get(oper)
			.then(getDescriptorListComplete, getDescriptorListFailed);
			
			// complete
			function getDescriptorListComplete(response) {
				return response.data;
			};
			
			// failed
			function getDescriptorListFailed() {
				// FIXME: logger.error();
			};
		};
		
		/**
		 * @name getMemberSearchResult
		 * @desc 멤버 내 검색결과를 가져오는 메소드
		 * @param {string} refsetId
		 * @param {string} q
		 * @param {string} page
		 * @param {string} size
		 */
		function getMemberSearchResult(refsetId, q, page, size) {
			console.log('getMemberSearchResult', q);
			// /members/SNOMEDCT?refsetId={refsetId}&q={q}&view=list
			var oper = '/members/SNOMEDCT/' + refsetId + '?q=' + q; 
			
			if (typeof page !== 'undefined') {
				oper += '&page=' + page; 
			}
			
			if (typeof size !== 'undefined') {
				oper += '&size=' + size; 
			}
			
			// request
			return apiservice.get(oper)
			.then(getMemberSearchResultComplete, getMemberSearchResultFailed);
			
			// complete
			function getMemberSearchResultComplete(response) {
				return response.data;
			};
			
			// failed
			function getMemberSearchResultFailed() {
				// FIXME: logger.error();
			};
		};
		
		/**
		 * @name getMemberList
		 * @desc 멤버 목록을 가져오는 메소드
		 * @param refsetId 레퍼런스세트 아이디
		 * @param vw 뷰; list,page,tree
		 * @param {number} page
		 * @param {number} size
		 */
		// 중복 getMemberSearchResult
		function getMemberList(refsetId, vw, page, size) {
			var oper = '/members/SNOMEDCT/' + refsetId; 
			
			if (typeof page !== 'undefined') {
				oper += '?page=' + page;
			}
			
			if (typeof size !== 'undefined') {
				oper += '&size=' + size;
			}
			
			// request
			return apiservice.get(oper)
			.then(getMemberListComplete, getMemberListFailed);
			
			// complete
			function getMemberListComplete(response) {
				return response.data;
			};
			
			// failed
			function getMemberListFailed() {
				// FIXME: logger.error();
			};
		};
		
		/**
		 * @name getSimpleMemberList
		 * @desc Simple 멤버 목록을 가져오는 메소드
		 */
		function getSimpleMemberList(refsetId) {
			// operator
			var oper = '/snomedct/refset/';
			
			// set refset id
			oper += refsetId + '/members/simple';
			
			// set effective time
			oper += '?effectiveTime=' + userservice.effectiveTime;
			
			// request
			return apiservice.get(oper)
			.then(getSimpleMemberListComplete, getSimpleMemberListFailed);
			
			// complete
			function getSimpleMemberListComplete(response) {
				return response.data;
			};
			
			// failed
			function getSimpleMemberListFailed() {
				// FIXME: logger.error();
			};
		};
		
		/**
		 * @name getMemberListWithDescriptorList
		 * @desc 멤버 목록과 필드 목록을 가져오는 메소드
		 */
		function getMemberListWithDescriptorList(refsetId, viewTyp, page, size) {
			console.log('getMemberListWithDescriptorList')
			var deferred = $q.defer();
			
			$q.all([
					// FIXME: getSimpleMemberList(refsetId),
					getMemberList(refsetId, viewTyp, page, size),
					getDescriptorList(refsetId)
			]).then(function(response) {
				deferred.resolve(response);
			}, function(error) {
				deferred.reject(error);
			});

			return deferred.promise;
		};
		
		/**
		 * @name addMemberList
		 * @desc 멤버 추가
		 */
		function addMemberList(refsetId, moduleId, candiMbrs, inactMbrs, delMbrs) {
			var oper = '/snomedct/refset/' + refsetId + '/members';
			
			var obj = {
				refsetId: refsetId,
				moduleId: moduleId,
				activeMbrs: candiMbrs,
				inactiveMbrs: inactMbrs,
				deletedMbrs: delMbrs
			};
			
			return apiservice.post(oper, JSON.stringify(obj), { headers: {'Content-Type':'application/json'} })
			.then(addMemberListComplete, addMemberListFailed);
			
			function addMemberListComplete(response) {
				return response.data;
			};
			
			function addMemberListFailed(err) {
				// FIXME: logger.error();
			};
		};
	}
	
})();