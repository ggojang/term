/**
 *  Concept Editor Main Controller
 */
(function() {
	'use strict';
	
	angular.module('app.concept').controller('ConceptController', ConceptController);
	
	ConceptController.$inject = ['$scope', 'CONFIG', 'conceptConstant', 'apiservice', 'conceptEditServcie', '$element', '$timeout', 'userservice', 'utilservice'];
	
	function ConceptController($scope, CONFIG, conceptConstant, apiservice, conceptEditServcie, $element, $timeout, userservice, utilservice) {
		
		$scope.termBeautifier = utilservice.termBeautifier;
		$scope.getDefinitionStatusFlagValue = utilservice.getDefinitionStatusFlagValue;
		$scope.getDefinitionStatusName = utilservice.getDefinitionStatusName;
		$scope.conceptDetailLoaded = false;
		$scope.$on('$destroy', function(){
			if($scope.wsk != null) {
				$scope.wsk.close();
			}
		});
		$scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams){
			if(angular.isDefined(toParams.editMode) && angular.isDefined(toParams.targetId)) {
				//$scope.logMessage = [];
				//if($scope.wsk != null) $scope.wsk.close();
				$scope.wsk = null;
				$scope.wskInit();
				initConceptCtrl();
				$scope.concept.type = toParams.editMode === 'child' ? 'N' : toParams.editMode === 'sibiling' ? 'E' : 'L'; //L => Edit Data Load
				if($scope.concept.type === 'N' || $scope.concept.type === 'E') {
					$scope.conceptDetailLoaded = true;
					//var isStated = true; //true=>stated or false=> Inferred//
					apiservice.getComponent(toParams.targetId, userservice.effectiveTime, true)
					.then(function (data) {
						apiservice.getDescriptionList(toParams.targetId, userservice.effectiveTime, {typeId:'900000000000003001'})
						.then(function(des) {
							data.push(des);
							$scope.$broadcast('addFocusConcept', data, $scope.concept.type);
							$scope.funcBtn.save.disabled = '';
							$scope.funcBtn.stated.disabled = '';
						},
						function(err){
						})
						.finally(function () {
							$scope.conceptDetailLoaded = false;
						});
					}, function(error) {
					});
				}
				
			} else {

			}
			if($element.find('.outer-west').find('#tree-root > li').length == 0) {
				$scope.loaded = true;
				apiservice.getChildren('138875005', userservice.effectiveTime) //conceptId, effectivetime
				.then(function(data) {
					$timeout(function () {
						$scope.$broadcast('initCHT', '138875005', userservice.effectiveTime, data);
					});
					
				}, function(error){	
				})
				.finally(function () {
					$scope.loaded = false;
				});
			}
			//if($element.find('.outer-west > ul#tree-root li').length == 0)
			//console.log($element.find('.outer-west').find('#tree-root > li').length);
		});
		//$scope.loaded = true;
		$scope.logMessage = [];
		$scope.wsk = null;
		$scope.wskInit = function() {
			$scope.wsk = new SockJS(CONFIG.apiUrl + '/logger/tail');
			$scope.wsk.onopen = function() {
			     console.log('Open log message');
			 };
			 $scope.wsk.onmessage = function(e) {
			     console.log('Log message', e.data);
			     //if($scope.logMessage.length == 0) $scope.logMessage.push(e.data);
			     $scope.logMessage.unshift(e.data);
			     $timeout(function(){
			    	 $scope.$apply('logMessage');
			     });
			 };
			 $scope.wsk.onclose = function(e) {
				console.log(e);
				console.log('Close log message');
			 };
		};	
		$scope.clearLog = function() {
			$scope.logMessage = [];
		};
		
		$scope.templateUrl = conceptConstant.template;
		// Function Area Button
		$scope.funcBtn = {
				apply : {disabled:'disabled'},
				save : {disabled:'disabled'},
				load : {disabled:''},
				del : {disabled:'disabled'},
				stated : {disabled:'disabled'},
				inferred : {disabled:'disabled'}
		};
		
		$scope.concept = {
				type : 'N', // 'N'=> New Concept, 'L'=> TEMPORARY Load Data, 'E'=> Concept Edit
				conceptId : null,
				active : 1, // 0 => inactive, 1 => active
				effectiveTime : null,
				moduleId : null,
				definitionStatusId : null,
				parents : null,
				attributes : null,
				descriptions : null
		};
		
		$scope.$on('setConceptEditData', function(event, vals, par, ref, des){
			if(vals != null) {
				$scope.concept.active = vals.active;
				$scope.concept.definitionStatusId = vals.definitionStatus == 1 ? '900000000000074008' : '900000000000073002';
				if(vals.effectiveTime != null) {
					var year = vals.effectiveTime.getFullYear();
					var month = vals.effectiveTime.getMonth()+1;
					if(month < 10) month = '0'+month;
					var day = vals.effectiveTime.getDate() < 10 ? '0'+vals.effectiveTime.getDate() : vals.effectiveTime.getDate();
					$scope.concept.effectiveTime = year+''+month+''+day;
				}
			}
			if(par != undefined && par != null) $scope.concept.parents = par;
			if(ref != undefined && ref != null) $scope.concept.attributes = ref;
			if(des != undefined && des != null) $scope.concept.descriptions = des;
			//console.log($scope.concept);
		});
		//$scope.wskInit();
		$scope.relationStated = function() {
			//$scope.wsk = null;
			//$scope.logMessage = [];
			//$scope.wskInit();
			$scope.wsk.send('WSK Start');
			//return false;
			//console.log($scope.concept.definitionStatusId);
			//return false;
			$scope.conceptDetailLoaded = true;
			var param = {
					conceptId : $scope.concept.conceptId,
					active : $scope.concept.active,
					effectiveTime : $scope.concept.effectiveTime,
					moduleId : userservice.extension.moduleId,
					definitionStatusId : $scope.concept.definitionStatusId,
					parents : $scope.concept.parents
			};
			console.log($scope.concept.descriptions);
			//return false;
			var attributes = [], conformAttributes = [];
			angular.forEach($scope.concept.attributes.parent, function(attr, key) {
				attributes.push({type:'P', name:attr.type.conceptId, value:attr.destination.conceptId, group:attr.relationshipGroup});
				conformAttributes.push({name:{conceptId:attr.type.conceptId, term:attr.type.term}, value:{conceptId:attr.destination.conceptId, term:attr.destination.term}, group:attr.relationshipGroup});
			});
			angular.forEach($scope.concept.attributes.add, function(attr, key) {
				attributes.push({type:'A', name:attr.type.conceptId, value:attr.destination.conceptId, group:attr.relationshipGroup});
				conformAttributes.push({name:{conceptId:attr.type.conceptId, term:attr.type.term}, value:{conceptId:attr.destination.conceptId, term:attr.destination.term}, group:attr.relationshipGroup});
			});
			param.attributes = attributes;
			param.des = $scope.concept.descriptions;
			//console.log(param);
			//angular.element('.fa').next('md-content').find('md-tab-item').eq(1).trigger('click');
			//console.log(param.des[0].descriptions[0].term);
			
			conceptEditServcie.callStated(param)
			.then(function(data){
				$scope.conformAttributes = conformAttributes;
				$scope.statedReturnData = data;
				$scope.funcBtn.inferred.disabled = '';
				$scope.funcBtn.stated.disabled = 'disabled';
			}, function(error){
				
			})
			.finally(function () {
				$scope.conceptDetailLoaded = false;
			});
		};
		
		$scope.conformAttributes = [];
		$scope.statedReturnData = null;
		//$scope.selectedTabIndex = 0;
		$scope.relationInferred = function() {
			/**
			var param = {
					//conceptId : '121000300107',
					conceptId : '269304002',
					path : '/home/jangduckjin/snomedct_test_owlf.owl'
			};
			*/
			$scope.conceptDetailLoaded = true;
			conceptEditServcie.callInferred($scope.statedReturnData)
			.then(function(data){
				console.log(data);
				var equalClasses = [{conceptId:$scope.statedReturnData.conceptId, term:$scope.concept.descriptions[0].descriptions[0].term, attrs:$scope.conformAttributes}];
				if(data.equalClasses == null){
					data.equalClasses = equalClasses;
				} else {
					data.equalClasses.push(equalClasses[0])
				}
				console.log(data);
				$scope.$broadcast('loadInferredData', data);
				$scope.funcBtn.inferred.disabled = 'disabled';
			}, function(error){
				
			})
			.finally(function () {
				$scope.conceptDetailLoaded = false;
				//$scope.selectedTabIndex = 1;
				angular.element($element).find('div.outer-center').find('md-pagination-wrapper').scope().$mdTabsCtrl.select(1);
			});
		};
		
		//$scope.changeTab = function(idx) {
		//	$scope.selectedTabIndex = idx;
		//};
		
		function initConceptCtrl() {
			$scope.funcBtn = {
					apply : {disabled:'disabled'},
					save : {disabled:'disabled'},
					load : {disabled:''},
					del : {disabled:'disabled'},
					stated : {disabled:'disabled'},
					inferred : {disabled:'disabled'}
			};
			$scope.concept = {
					type : 'N', // 'N'=> New Concept, 'L'=> TEMPORARY Load Data, 'E'=> Concept Edit
					conceptId : null,
					active : 1, // 0 => inactive, 1 => active
					effectiveTime : null,
					moduleId : null,
					definitionStatusId : null,
					parents : null,
					attributes : null,
					descriptions : null
			};
			
			$scope.conformAttributes = [];
			$scope.statedReturnData = null;
			angular.element($element).find('div.outer-center').find('md-pagination-wrapper').scope().$mdTabsCtrl.select(0);
			//$scope.wsk = null;
			//$scope.logMessage = [];
			//$scope.wskInit();
			//$scope.wsk.send('WSK Start');
			//$scope.selectedTabIndex = 0;
		}
	};
})();