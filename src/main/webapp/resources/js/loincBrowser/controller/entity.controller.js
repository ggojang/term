(function () {
	
	'use strict';
	
	angular
		.module('app.loincBrowser')
		.controller('EntityController', EntityController);
	
	EntityController.$inject = ['$scope', '$stateParams', '$element', 'dataservice', 'entityservice', '$timeout', '$log', '$localstorage'];
	function EntityController($scope, $stateParams, $element, dataSvc, entitySvc, $timeout, $log, $localstorage) {
		
		// ----------------------------------------
		// Local variable
		// ----------------------------------------
		var vm = this;
		
		var ctx = {
			ver: 'v2.67'
		};
		
		var LP = "LP";

		vm.code = $stateParams.code;
		vm.CodeType = ''; //
		vm.isLeafCode = false;
		vm.entity = {};
		vm.LP = {}
		vm.LPLink = {};
                vm.LPMap = {};
		vm.LAs = {};
		vm.LGs = {};
		vm.LGAttrs = {};
		vm.LGTerms = {};
		vm.LGP = {};
		vm.LGPAttr = {};
		vm.hiers = [];
		vm.panel = {};
		vm.LV = {};
		vm.loading = false;

		vm.getFsn = getFsn;
				
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
			
			vm.isLeafCode = entitySvc.isLeafCode(vm.code);
			vm.codeType = dataSvc.getCodeType(vm.code); //	
			
			// 로딩 시작
			goScrollTop();
			vm.loading = true;
	
			dataSvc.getEntity(vm.code, ctx.ver)
			.then(function (e) {
				vm.entity = e;
				//$log.debug("vm.code, ctx.ver, e in getEntity : " + vm.code + " " + ctx.ver + " " + JSON.stringify(vm.entity) );	
				entitySvc.setSelectedCode(vm.code);
				
				//local storage setting
				$localstorage.set('oldLoincVer', ctx.ver);
				$localstorage.set('oldLoincCode', vm.code);
				//console.log("oldLoincVer : " + $localstorage.get('oldLoincVer'));
				//console.log("oldLoincCode : " + $localstorage.get('oldLoincCode'));
				
				//if (e != undefined) {
				//}

				// Panel이 있는 main-sidebar show/hide. 20200423 by Yu 
				entitySvc.setPanelType(vm.entity.panelType);
				if (entitySvc.getPanelType() == '') {
                                	angular.element(document.getElementsByClassName('main-sidebar'))[0].style.display = "none";
                        	} else {
                                	angular.element(document.getElementsByClassName('main-sidebar'))[0].style.display = "block";
                        	}
			})
			.finally(function () {
				
				$timeout(function () {
					vm.loading = false;
				}, 500);
				
				$log.debug('<Service Call> Entity: ' + vm.code + '  // LOINC ' + ctx.ver);
			});

			dataSvc.getLP(vm.code)
                          .then(function (e) {
                                //console.log("vm.LP : " + JSON.stringify(e));
                                vm.LP = e;
                          })
                          .finally(function () {

                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);
                          });

			dataSvc.getLPLink(vm.code)
                          .then(function (e) {
				//console.log("vm.LPLink : " + JSON.stringify(e));
                                vm.LPLink = e;
		  	  })
			  .finally(function () {

                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);
			});

			if ( vm.codeType == LP) {
                          dataSvc.getLPMap(vm.code)
                          .then(function (e) {
				//console.log("vm.LPMap : " + JSON.stringify(e));
                                vm.LPMap = e;
			  })
			  .finally(function () {

                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);;
                          });
                        }
			
                        dataSvc.getLA(vm.code)
                        .then(function (e) {
			  vm.LAs = e
                        })
                        .finally(function () {

                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);;
                        });			

			dataSvc.getLG(vm.code)
                        .then(function (e) {
                          vm.LGs = e;
                        })
                        .finally(function () {

                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);;
                        });

			dataSvc.getLGTerm(vm.code)
                        .then(function (e) {
                          vm.LGTerms = e;
                        })
                        .finally(function () {

                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);;
                        });

			dataSvc.getHierarchyList(vm.code, ctx.ver)
			.then(function (hiers) {
			  vm.hiers = hiers;
			})
			.finally(function () {

                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);;
                        });

			dataSvc.getPanel(vm.code, ctx.ver)
                        .then(function (e) {
                          vm.panel = e;
                        })
                        .finally(function () {

                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);;
                        });

			dataSvc.getLV(vm.code)
                        .then(function (e) {
                          vm.LV = e;
                        })
                        .finally(function () {

                                $timeout(function () {
                                        vm.loading = false;
                                }, 500);;
                        });

		};

		/**
		 * @name getFsn
		 * @desc Fully Specified Name 반환
		 * @return {string} fsn
		 */
		function getFsn() {
			var fsn;
			if (vm.isLeafCode) {
				fsn = entitySvc.getFsn(vm.entity);
			} else { 
				// TODO LP Code일 경우 처리
				fsn = '';
			}
			
			return fsn;
		};
		
		function goScrollTop() {
			$element.parent().scrollTop('0');
		};
	
	}
}());
