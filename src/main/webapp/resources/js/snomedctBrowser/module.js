(function() {
  'use strict';

  angular
    .module('app.snomedctBrowser', [  
      /* 3rd-party modules */
      'ui.router',
      'ngMaterial',
      'ui.bootstrap',
      'angular.filter',
      'ngtimeago',
      'vAccordion',
      'app'
  ]);
  
  angular
    .module('app.snomedctBrowser')
    .config(BrowserConfig);

  function BrowserConfig($urlRouterProvider, $stateProvider) {
	  
	  var STATE_BROWSER = 'snomedctBrowser',
  	      STATE_BROWSER_CONCEPT = STATE_BROWSER + '.concept',
	      STATE_BROWSER_DESCRIPTION_EDITOR = STATE_BROWSER_CONCEPT + '.descriptionEdit',
	      URL_BROWSER = '/browser/snomedct',
	      URL_BROWSER_CONCEPT = '/{conceptId}',
	      URL_BROWSER_DESCRIPTION_EDITOR = '/description/edit',
	      ROOT_CONCEPT_ID = '138875005';
		
	  function scrollTo(element, to, duration) {
		    if (duration <= 0) return;
		    var difference = to - element.scrollTop;
		    var perTick = difference / duration * 10;

		    setTimeout(function() {
		        element.scrollTop = element.scrollTop + perTick;
		        if (element.scrollTop === to) return;
		        scrollTo(element, to, duration - 10);
		    }, 10);
		};
		
    $stateProvider
    	.state(STATE_BROWSER, {
		url : URL_BROWSER,
		templateUrl : 'resources/template/snomedctBrowser/layout.html',
		controller : 'LayoutController',
		controllerAs : 'layoutVm'
	})
	.state(STATE_BROWSER_CONCEPT, {
		url : URL_BROWSER_CONCEPT,
		params : {
			conceptId : null
		},
		resolve : {
			selectConcept : function($state, $stateParams, browserservice, $localstorage) {
				//$timeout(function() {
				/*
				var cnptId = $stateParams.conceptId;
				if (cnptId != null) {
					browserservice.setConceptId(cnptId);
				} else {
					// conceptId가 없으면 root인 138875005로 설정
					$state.go(STATE_BROWSER_CONCEPT, {
						conceptId : ROOT_CONCEPT_ID //20200424 by Yu
					});
				}
				*/
				//});
				
				// localstorage setting 20200428 by Yu
				var cnptId = $stateParams.conceptId;
				var snomedConcept = $localstorage.get('oldSnomedConcept');
				//console.log('$stateParams.conceptId: ' + concpetId);
				//console.log('oldSnomedConcept: ' + snomedConcept);
				if (cnptId != null) {
					browserservice.setConceptId(cnptId);
				} else if (snomedConcept != null) {
					browserservice.setConceptId(oldSnomedConcept);
				} else {
					$state.go(STATE_BROWSER_CONCEPT, {
						conceptId : ROOT_CONCEPT_ID});
				} 
			}
		}
	})
	.state(STATE_BROWSER_DESCRIPTION_EDITOR, {
		url: URL_BROWSER_DESCRIPTION_EDITOR,
		resolve: {
			editDescription: function($state, $stateParams, $timeout) {
					
				var hasFocusConceptId = $stateParams.conceptId !== null ? true:false;
				// 포커스컨셉 존재 확인
				if (hasFocusConceptId) {
					// 우측 창 닫기
					angular.element(document.querySelector('.sctb-wrap')).scope().layoutVm.setCollapsed();
					// 용어 Accordion Top값 확인
					var offsetTop = angular.element(document.querySelector('#sctb-description')).position().top;
					// 용어 Accordion Top값으로 스크롤 이동
					scrollTo(document.querySelector('#sctb-detail'), offsetTop, 300);
					// 용어 에디터 활성화
					angular.element(document.querySelector('description')).scope().de.setEnabled(true);
				} else {
					alert('Please Select Any Concept');
						
					$timeout(function() {
						$state.go(STATE_BROWSER_CONCEPT, {conceptId: ROOT_CONCEPT_ID}, {reload:true});
					}, 10);
				}
			}
		},
		onExit: function() {
			angular.element(document.querySelector('.sctb-wrap')).scope().layoutVm.setExpanded();
			angular.element(document.querySelector('description')).scope().de.setEnabled(false);
			// Detail 최상단으로 스크롤 이동
			//document.querySelector('#sctb-detail').scrollTop = 0;
		}
	});
  };
  
  BrowserConfig.$inject = ['$urlRouterProvider', '$stateProvider'];
  
  
  
})();
