/**
 * Concept Editor on concept form
 */
(function() {
	'use strict';
	
	angular.module('app.concept').controller('ConceptTab1Controller', ConceptTab1Controller);
	
	ConceptTab1Controller.$inject = ['$scope', '$mdDialog', '$filter', '$timeout', 'browser.langrefset', 'conceptConstant', '$element', 'apiservice', 'userservice'];
	function ConceptTab1Controller($scope, $mdDialog, $filter, $timeout, langrefset, conceptConstant, $element, apiservice, userservice) {
		
		$scope.$on('addFocusConcept', function(event, data, editType){
			initTab1Ctrl ();
			/**
			var inferredConcept = convRelations(inferredData[2], 1);
			var statedConcept = convRelations(statedData[2], 1);
			$scope.focusConcept = {inferred:inferredConcept, stated:statedConcept};
			*/
			var focusConcept = data[0];
			if(focusConcept.definitionStatus.id === '900000000000074008') {
				focusConcept.definitionStatus.type = 'P';
			} else if(focusConcept.definitionStatus.id === '900000000000073002') {
				focusConcept.definitionStatus.type = 'F';
			}
			$scope.focusConcept.push(focusConcept);
			$scope.fsnArray = data[6];
			//console.log(getLangFSN(data[6], $scope.languageDropdown[0]));
			$scope.dsSets = convDescriptions(data[6], editType);
			$scope.refinements.parent = convRelations(data[2], 2);
			console.log($scope.refinements.parent);
			$scope.refParGrpNo = getRefAddGrpNo($scope.refinements.parent);
			var rgArr = [];
			angular.forEach($scope.refinements.parent, function(arr, idx){
				this.push(parseInt(arr.relationshipGroup));
			},rgArr);
			$scope.relationshipGroupMax = $scope.refinements.parent.length == 0 ? 0 : Math.max.apply(null, rgArr);
			//console.log($scope.refParGrpNo);
			//console.log($scope.relationshipGroupMax);
			$scope.qualifiers = data[5].sort(function(a, b){
				var nameA = a.name.toUpperCase();
				var nameB = b.name.toUpperCase();
				if (nameA < nameB) {
					return -1;
				}
				if (nameA > nameB) {
					return 1;
				}
				return 0;
			});
			//console.log($scope.qualifiers);
			//$scope.dsSets = convDescriptions(statedData[1], editType);
			//console.log(data[0]);
			$scope.$emit('setConceptEditData', $scope.vals, null, $scope.refinements, $scope.dsSets);
		});
		//console.log($filter('translate')('concept.locale.name.kr'));
		/** Concept S */
		$scope.vals = {
			effectiveTime : $scope.concept.effectiveTime == null ? null : new Date(parseInt($scope.concept.effectiveTime.substring(0, 4)), parseInt($scope.concept.effectiveTime.substring(4, 6)), parseInt($scope.concept.effectiveTime.substring(6))),
			openedDt : false,
			definitionStatus :  $scope.concept.definitionStatusId === '900000000000074008' ? 1 : 0,
			active : $scope.concept.active
		};
		
		$scope.dateOptions = {
				formatYear: 'yy',
				startingDay: 1
		};
		
		$scope.openDatepicker = function() {
			$scope.vals.openedDt = true;
		};
		//$scope.openedDt = false;
		//$scope.definitionStatus = $scope.concept.definitionStatusId === '900000000000074008' ? 1 : 0;
		//$scope.active = $scope.concept.active;
		
		
		
		$scope.openFcp = function($event, conceptId) {
			$mdDialog.show({
				locals: {fcId:conceptId},
				bindToController: true,
				controller: 'FcpDialogController',
				templateUrl: 'resources/template/routes/concept/dialogFocusConceptParents.html',
				parent: angular.element(document.body),
				targetEvent: $event,
				clickOutsideToClose:true,
				fullscreen: true
			});
		};
		/** Concept E */
		/**
		 * var cIsA = "116680003";
          var cFullyDefined = "900000000000073002";
          var cPrimitive = "900000000000074008";
          clone.destination.definitionStatus.id
		 */
		$scope.focusConcept = [];
		$scope.refinements = {parent:[], add:[]};
		$scope.refParGrpNo = [];
		$scope.qualifiers = [];
		$scope.relationshipGroupMax = 0;
		$scope.dsSets = [];
		$scope.fsnArray = [];
		
		$scope.$watchCollection('vals', function(newObj, oldObj){
			$scope.$emit('setConceptEditData', newObj);
		});
		$scope.$watchCollection('focusConcept', function(newObj, oldObj){
			$scope.$emit('setConceptEditData', null, newObj);
		});
		
		/** Descriptions S */
		$scope.languageDropdown = [];
		
		//Change language...
		$scope.changeLangSet = function($index, langset) {
			$scope.dsSets[$index].id = langset.id;
			$scope.dsSets[$index].lang = langset.lang;
			$scope.dsSets[$index].langName = langset.langName;
			$scope.dsSets[$index].langCountry = langset.country;
			var fsn = getLangFSN($scope.fsnArray, langset);
			angular.forEach($scope.dsSets[$index].descriptions, function(description, key){
				if(key == 0) description.term = fsn.term+'('+fsn.semanticTag+')';
				else if(key == 1) description.term = fsn.term;
				else description.term = '';
				description.languageCode = langset.lang;
			});
			$scope.$emit('setConceptEditData', null, null, null, $scope.dsSets);
		};
		//Preferred term copy to FSN
		$scope.copyTo = function(event, pidx, idx) {
			if(idx == 1) {
				var t = angular.element(event.target);
				var semanticTag = $scope.dsSets[pidx].descriptions[0].term.substring($scope.dsSets[pidx].descriptions[0].term.lastIndexOf("("));
				$scope.dsSets[pidx].descriptions[0].term = t.val()+semanticTag;
			}
			$scope.$emit('setConceptEditData', null, null, null, $scope.dsSets);
		};
		
		angular.forEach(conceptConstant.svcLanguage, function(svcLang, key){
			angular.forEach(langrefset, function(langs, k){
				if(svcLang === langs.countryCode){
					var langName = $filter('translate')('concept.locale.name.'+langs.countryCode);
					$scope.languageDropdown.push({id:langs.id, lang:langs.languageCode, langName:langName, country:langs.countryCode});
				}
			});
		});
		
		$scope.ctxMenuDescriptionOpt = [
			        		             {key:'addset',name:'Add description set',icon:'glyphicon-edit',disabled:false},
			        		             {key:'removeset',name:'Remove description set',icon:'glyphicon-share',disabled:false},
			        		             {key:'addterm',name:'Add synonym term',icon:'glyphicon-plus',disabled:false},
			        		             {key:'removeterm',name:'Remove synonym term',icon:'glyphicon-remove',disabled:false}
			        		     ];
		
		$scope.selectCtxMenuDescription = function(event, pidx, idx) {
			//console.log(event.ngcmKey+', '+pidx+', '+idx);
			
			if(event.ngcmKey === 'addterm') {
				var d = $scope.dsSets[pidx].descriptions[$scope.dsSets[pidx].descriptions.length - 1];
				var n =  {
		            	active : true,
						term : '',
						effectiveTime : '',
						moduleId : d.moduleId,
						typeId : d.typeId,
						languageCode : d.languageCode
		            };
				$scope.dsSets[pidx].descriptions.push(n);
				$scope.$apply();
			}
			else if(event.ngcmKey === 'removeterm') {
				$scope.dsSets[pidx].descriptions.splice(idx,1);
				$scope.$apply();
			}
			else if(event.ngcmKey === 'addset') {
				$scope.dsSets.push(setDescriptionSet($scope.fsnArray, $scope.languageDropdown[0]));
				if($scope.dsSets.length >= conceptConstant.svcLanguage.length) {
					$scope.ctxMenuDescriptionOpt[0].disabled = true;
				}
				$scope.$apply();
			}
			else if(event.ngcmKey === 'removeset') {
				$scope.dsSets.splice(pidx,1);
				if($scope.dsSets.length < conceptConstant.svcLanguage.length) {
					$scope.ctxMenuDescriptionOpt[0].disabled = false;
				}
				$scope.$apply();
				//console.log(pidx);
			}
			//console.log(event.ngcmKey);
		};
		function getLangFSN(fsnArr, langSet) {
			var en = {}, ts = {};
			for(var i=0; i<fsnArr.length; i++) {
				if(fsnArr[i].languageCode === 'en'){
					en.semanticTag = fsnArr[i].term.substring(fsnArr[i].term.lastIndexOf("(")+1, fsnArr[i].term.lastIndexOf(")"));
					en.term = fsnArr[i].term.substring(0, fsnArr[i].term.lastIndexOf("(")).trim();
				}
				if(fsnArr[i].languageCode === langSet.lang){
					ts.semanticTag = fsnArr[i].term.substring(fsnArr[i].term.lastIndexOf("(")+1, fsnArr[i].term.lastIndexOf(")"));
					ts.term = fsnArr[i].term.substring(0, fsnArr[i].term.lastIndexOf("(")).trim();
				}
			}
			if(angular.isUndefined(ts.term)) ts = en;
			return ts;
		}
		function setDescriptionSet(fsnArr, langSet) {
			var fsn = getLangFSN(fsnArr, langSet);
			var dd = {
					id : langSet.id,
					lang : langSet.lang,
					langName : langSet.langName,
					langCountry : langSet.country,
					descriptions : [
					            {
					            	active : true,
									term : fsn.term+'('+fsn.semanticTag+')',
									effectiveTime : '',
									moduleId : userservice.extension.moduleId,
									typeId : '900000000000003001', // fsn userservice
									languageCode : langSet.lang
					            },
					            {
					            	active : true,
									term : fsn.term,
									effectiveTime : '',
									moduleId : userservice.extension.moduleId, //247771000300108
									typeId : '900000000000013009', // synonym
									languageCode : langSet.lang
					            }
					]
			};
			return dd;
		}
		function convDescriptions (fsnArr, edit) {
			var desList = [];
			
			if(edit === 'N') {
				var dd = setDescriptionSet(fsnArr, $scope.languageDropdown[0]);
				desList.push(dd);
			}
			if(desList.length >= conceptConstant.svcLanguage.length) {
				$scope.ctxMenuDescriptionOpt[0].disabled = true;
			}
			return desList;
		}
		
		/** Descriptions E */
		
		function convRelations (relationships, type) {
			var arr = [];
			angular.forEach(relationships, function(relationship, key) {
				var clone = angular.copy(relationship);
				var priority = clone.type.conceptId === '116680003' ? 1 : 2;
				clone.priority = priority;
				
				var cType = {type:'',name:''};
				if(clone.destination.definitionStatus.id != null) {
					var i = clone.destination.definitionStatus.id;
					if (i.length == 19) {
						i = i.substring(0, i.length-1);
					}
					if(i === '900000000000074008') {
						cType.type = 'P'; cType.name = 'Primitive';
					} else if(i === '900000000000073002') {
						cType.type = 'F'; cType.name = 'Fully defined';
					}
				}
				clone.cType = cType;
				clone.checkClass = '';
				if(clone.priority == type) this.push(clone);
				//this.push(clone);
			}, arr);
			return arr;
		}
		
		
		/** Focusconcept Context Menu S */
		/**
		$scope.ctxMenuFocusConceptOpt = [
		                     {key:'add',name:'Add focus concept',icon:'glyphicon-console',disabled:false}, //icon:'bootstrap Glyphicons class'=>optional, disabled:boolean=>optional
		                     {key:'change',name:'Change the selected concept',icon:'glyphicon-transfer',disabled:false},
		                     {key:'remove',name:'Remove the selected concept',icon:'glyphicon-erase',disabled:false} //glyphicon-erase
		];
		*/
		/** Focusconcept Context Menu E */
		function dialogOpen($event, index) {
			$mdDialog.show({
				controller: 'ConceptDialogController',
				templateUrl: 'resources/template/routes/concept/dialogConcept.html',
				parent: angular.element(document.body),
				targetEvent: $event,
				clickOutsideToClose:true,
				fullscreen: true
			})
			.then(
					function(result) {
						var cls = $event.ngcmKey === 'change' ? 'danger' : $event.ngcmKey === 'add' ? 'success' : '';
						//$scope.focusConcept[index].checkClass = cls;
					},
					function() {
						//$scope.focusConcept[index].checkClass = '';
					}
			);
		}
		
		/** Refinements S */
		$scope.selAttr = 0;
		$scope.checkup = function(chk) {
			if(chk == true) $scope.selAttr++;
			else $scope.selAttr--;
			
		};
		$scope.comboEvent = function($event, attr) {
			if($event.selComboIndex > -1) {
				$scope.$apply(function(){
					attr.type.term = $scope.qualifiers[$event.selComboIndex].name;
					attr.type.conceptId = $scope.qualifiers[$event.selComboIndex].id;
				});
			}	
		};

		$scope.srchAttrVal = function(event, md, attr) {
			if(attr.type.conceptId == undefined) return false;
			var t = angular.element(event.target);
			if(3 <= t.val().length) {//ce-loding-input
				attr.loading = 'ce-loding-input';
				var req = md === 'add' ? apiservice.getMrcmValueList(attr.type.conceptId, t.val(), 10) : apiservice.getNarrowSuggestResultList(attr.destination.conceptId, t.val(), 10);
				req.then(function(data){
					attr.options = data;
					$timeout(function(){
						t.next().children('button').triggerHandler('click');
					});
				}
				,function(error){})
				.finally(function () {
					attr.loading = '';
					t.trigger('click');
				});
			} else {
				attr.options = [];
				$timeout(function(){
					t.next().children('button').triggerHandler('click');
				});
			}
			
		};
		$scope.comboValueEvent = function($event, attr) {
			if($event.selComboIndex > -1) {
				$scope.$apply(function(){
					attr.destination.term = attr.options[$event.selComboIndex].fsn;
					attr.destination.conceptId = attr.options[$event.selComboIndex].conceptId;
					angular.element($event.target).parent().prev('input').val('');
					$scope.$emit('setConceptEditData', null, null, $scope.refinements);
				});
			}
		};
		/**
		$scope.checkInputConceptId = function($event, attr) {
			if(attr.type.conceptId != undefined) {
				var val = angular.element($event.target).val();
				attr.destination.term = val;
				attr.destination.conceptId = val;
				angular.element($event.target).val('');
			}
		};
		*/
		$scope.ctxMenuRefinementsOpt = [
			        		             {key:'addattr',name:'Add attribute',icon:'glyphicon-font',disabled:false},
			        		             {key:'removeattr',name:'Remove attribute',icon:'glyphicon-text-background',disabled:false},
			        		             {key:'addgrp',name:'Add attribute group',icon:'glyphicon-resize-small',disabled:false},
			        		             {key:'removegrp',name:'Remove attribute group',icon:'glyphicon-resize-full',disabled:false}
			        		     ];
		$scope.refAddGrpNo = [];
		function getRefAddGrpNo (arr) {
			var array = [];
			angular.forEach(arr, function(attr, i){
				var no = parseInt(attr.relationshipGroup);
				if(array.indexOf(no) == -1) this.push(no);
			}, array);
			return array.sort(function(a, b){return b-a});
		}
		$scope.selectCtxMenuRefinements = function(event, val) {//ngcmKey $scope.relationshipGroupMax
			if(event.ngcmKey === 'addattr') {
				$scope.$apply(function(){
					var today = new Date();
					var addAttrInit = {
							type: {term:'Attribute Name'},
							destination: {term:'Attribute Value',conceptId:today.getTime()},
							priority:2,
							relationshipGroup:"0",
							valid: false,
							checked: false
						};
					$scope.refinements.add.push(addAttrInit);
					$scope.refAddGrpNo = getRefAddGrpNo($scope.refinements.add);
				});
			} else if(event.ngcmKey === 'removeattr') {
				$scope.$apply(function(){
					var idx = -1;
					angular.forEach($scope.refinements.add, function(attr, i){
						if(attr.$$hashKey === val) idx = i;
					});
					if(idx != -1) $scope.refinements.add.splice(idx,1);
					$scope.refAddGrpNo = getRefAddGrpNo($scope.refinements.add);
				});
			} else if(event.ngcmKey === 'addgrp') {
				$scope.$apply(function(){
					$scope.relationshipGroupMax = $scope.relationshipGroupMax+1;
					angular.forEach($scope.refinements.add, function(attr, idx){
						if(attr.checked == true){
							attr.relationshipGroup = ''+$scope.relationshipGroupMax;
							attr.checked = false;
						}
					});
					$scope.refAddGrpNo = getRefAddGrpNo($scope.refinements.add);
					$scope.selAttr = 0;
				});
			} else if(event.ngcmKey === 'removegrp') {
				$scope.$apply(function(){
					angular.forEach($scope.refinements.add, function(attr, idx){
						if(val+'' === attr.relationshipGroup) attr.relationshipGroup = '0';
						if(val < parseInt(attr.relationshipGroup)) attr.relationshipGroup = ''+(parseInt(attr.relationshipGroup)-1);
					});
					$scope.relationshipGroupMax = $scope.relationshipGroupMax-1;
					$scope.refAddGrpNo = getRefAddGrpNo($scope.refinements.add);
				});
			}
			//$scope.$apply();
		};
		/** Refinements E */
		function initTab1Ctrl () {
			$scope.focusConcept = [];
			$scope.vals = {
					effectiveTime : $scope.concept.effectiveTime == null ? null : new Date(parseInt($scope.concept.effectiveTime.substring(0, 4)), parseInt($scope.concept.effectiveTime.substring(4, 6)), parseInt($scope.concept.effectiveTime.substring(6))),
					openedDt : false,
					definitionStatus :  $scope.concept.definitionStatusId === '900000000000074008' ? 1 : 0,
					active : $scope.concept.active
				};
			$scope.refinements = {parent:[], add:[]};
			$scope.refParGrpNo = [];
			$scope.qualifiers = [];
			$scope.relationshipGroupMax = 0;
			$scope.fsnArray = [];
			//$scope.languageDropdown = [];
			$scope.selAttr = 0;
			$scope.refAddGrpNo = [];
		}	
	}
})();