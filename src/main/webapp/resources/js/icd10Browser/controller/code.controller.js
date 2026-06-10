(function () {
	
	'use strict';
	
	angular
		.module('app.icd10Browser')
		.controller('CodeController', CodeController);
	
	CodeController.$inject = ['$scope', '$sce', '$stateParams', '$element', 'data2service', 'codeservice', '$timeout', '$log', '$localstorage'];

	function CodeController($scope, $sce, $stateParams, $element, data2Svc, codeSvc, $timeout, $log, $localstorage) {
		
		$scope.trustAsHtml = function(string) {
			return $sce.trustAsHtml(string);
		}
		// ----------------------------------------
		// Local variable
		// ----------------------------------------
		var vm = this;
		
		var ctx = {
			ver: '2016'
		};
		
		vm.pcode = $stateParams.code;
		
		//vm.isLeafCode = false;
		//vm.codes = [];
		vm.rubrics = [];
		//vm.chapter = [];
		//vm.children = [];
		vm.ancestors = [];
		vm.siblings = [];

		vm.loading = false;

		//vm.getLabel = getLabel;
		vm.scrollTo = scrollTo;

		
		// ----------------------------------------
		// Init
		// ----------------------------------------
		activate();
		
		
		// ----------------------------------------
		// Methods
		// ----------------------------------------
		
		/**
		 * @name activate
		 * @desc Entry Point
		 */
		function activate() {
			vm.isLeafCode = codeSvc.isLeafCode(vm.pcode);
			
			// 로딩 시작
			goScrollTop();
			vm.loading = true;
			
			
			data2Svc.getCode(vm.pcode, ctx.ver)
			.then(function (e) {
				vm.pcode = e;
				
				codeSvc.setSelectedCode(vm.pcode);
				
				//local storage setting
				$localstorage.set('oldIcd10Code', vm.pcode.code);
				//console.log("oldIcd10Code : " + $localstorage.get('oldIcd10Code'));
			})
			.finally(function () {
				$timeout(function () {
					vm.loading = false;
				}, 500);
				$log.debug('<Service Call> Code: ' + vm.pcode + '  // ICD10 ' + ctx.ver);
			});

			data2Svc.getRubric(vm.pcode, ctx.ver)
                        .then(function (rubrics) {
                                vm.rubrics = rubrics;
                        })
                        .finally(function () {
                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);
                                $log.debug('<Service Call> Rubric: ' + vm.rubrics + '  // ICD10 ' + ctx.ver);
                        });
			
			data2Svc.getAncestor(vm.pcode, ctx.ver)
                        .then(function (ancestors) {
                                vm.ancestors = ancestors;
				//ancestorList = vm.ancestors.slice(0);
				//console.log(vm.ancestors);
                        })
                        .finally(function () {
                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);
                                $log.debug('<Service Call> Ancestors: ' + vm.ancestors + '  // ICD10 ' + ctx.ver);
			});
			
			data2Svc.getSibling(vm.pcode, ctx.ver)
			.then(function (siblings) {
				vm.siblings = siblings;
			})
			.finally(function () {
				$timeout(function () {
                                        vm.loading = false;
                                }, 500);
                                $log.debug('<Service Call> Siblings: ' + vm.siblings + '  // ICD10 ' + ctx.ver);
                        });
			
		};

		/**
		 * @name getLabel
		 * @desc preferred 반환
		 * @return {string} label
		  
		function getLabel() {
			var label;
			if (vm.isLeafCode) {
				label = codeSvc.getLabel(vm.pcode);
			} //else {
				// TODO LP Code일 경우 처리
			//	label = '';
			}
			$log.debug('controller:code, getLabel()');	
			return  label;
		};
		 */
		
		function goScrollTop() {
			$element.parent().scrollTop('0');
		};

		function scrollTo(code) {
    			location.hash = "#" + code;
		};
	}
}());
