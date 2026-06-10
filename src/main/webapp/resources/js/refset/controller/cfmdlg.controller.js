(function () {
	
	'use strict';
	
	angular
		.module('app.referenceset')
		.controller('ConfirmDialogController', ConfirmDialogController);
	
	ConfirmDialogController.$inject = ['$scope', '$mdDialog', '$timeout', 'userservice', 'refsetservice'];
	function ConfirmDialogController($scope, $mdDialog, $timeout, userservice, refsetservice) {
		
		var vm = this;
		
		vm.apply = apply;
		vm.close = close;
		vm.ok = ok;
		
		vm.refset = {};
		vm.moduleId = '';
		vm.newMbrs = [];
		vm.inactMbrs = [];
		vm.delMbrs = [];
		
		vm.applying = false;
		vm.complete = false;
		vm.error = false;
		
		activate();
		
		/**
		 * @name activate
		 * @desc 초기 실행 메소드
		 */
		function activate() {
			vm.refset = refsetservice.getRefsetIdNm();
			vm.moduleId = userservice.extension.moduleId;
			vm.newMbrs = refsetservice.pathNewMembers;
			vm.inactMbrs = refsetservice.inactMbrs;
			vm.delMbrs = refsetservice.delMbrs;
		};

		/**
		 * @name apply
		 * @desc [apply] 버튼 선택 이벤트 처리 메소드
		 */
		function apply() {
			vm.applying = true;
			
			refsetservice.apply()
			.then(function (response) {
				// resolve
				vm.complete = true;
			}, function (err) {
				// reject
				//alert(err);
				vm.error = true;
			})
			.finally(function () {
				vm.applying = false;
			});
		};
		
		/**
		 * @name close
		 * @desc 변경사항을 서버에 반영하지 않고 dialog를 닫는 메소드
		 */
		function close() {
			$mdDialog.cancel();
		};
		
		/**
		 * @name ok
		 * @desc 변경사항을 서버에 반영 후 [ok] 버튼 선택 시 dialog를 닫는 메소드
		 */
		function ok() {
			$mdDialog.hide();
		};
		
	};
	
})();