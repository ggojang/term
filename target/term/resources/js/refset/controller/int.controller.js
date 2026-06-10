/**
 * International Release Referenceset Controller
 */
(function () {
	
	'use strict';
	
	angular
		.module('app.referenceset')
		.controller('IntRefsetController', IntRefsetController);
	
	IntRefsetController.$inject = ['$scope', 'refsetservice', 'userservice', 'rsdataservice'];
	function IntRefsetController($scope, refsetservice, userservice, rsdataservice) {
		
		var vm = this;
		
		// @deprecated vm.selectRefset = selectRefset;
		vm.getChildren = getChildren;
		
		/* 트리 배열 */
		vm.tree = [];
		/* 멤버가 존재하는 레퍼런스세트의 아이디 목록 */
		vm.existIds = [];
		
		var rootReferenceset = {
			active: true,
			conceptId: '900000000000455006',
			term: 'Reference set',
			semanticTag: 'foundation metadata concept',
			definitionStatus: 'Primitive',
			childrenCount: 16,
			descendantCount: 75
		};
		
		
		
		activate();
		
		/**
		 * @name activate
		 * @desc end point
		 */
		function activate() {
			// 국제배포판의 레퍼런스세트 목록 호출
			rsdataservice.getIntRefsetList()
			.then(function (response) {
				var children = response[0], // subtypes
					existIds = response[1]; // exists id
				
				rootReferenceset.children = children;
				vm.existIds = existIds;
				
				rootReferenceset.open = true;
				
				vm.tree.push(rootReferenceset);
			});
		};
		
		
		/**
		 * @name selectRefset
		 * @desc 국제배포판 탭에서 레퍼런스세트를 선택했을 때 이벤트를 처리하는 메소드
		 * @param {String} refsetId
		 * @param {String} refsetNm
		 */
		//function selectRefset(refsetId, refsetNm) {
		//	refsetservice.getIntMemberList(refsetId, refsetNm);
		//};

		
		/**
		 * @name getChildren
		 * @desc 자식을 호출하는 메소드
		 * @param {Object} node
		 */
		function getChildren(node) {
			if (node.childrenCount > 0 && typeof node.children === 'undefined') {
				node.loading = true;
				
				rsdataservice.getChildren(node.conceptId)
				.then(function (children) {
					node.children = children;
				})
				.finally(function () {
					node.loading = false;
				});
			}
			
			node.open = !node.open;
		};
	};
	
})();