/**
 * Module Name : Concept Editor Configuration
 */
(function() {
	'use strict';
	
	var m = angular.module('app.concept', [
		'ui.router',
		'ngMaterial',
		'ui.bootstrap',
		'vAccordion',
		'angular.filter',
		'ngTable'
	]);
	m.constant('conceptConstant', {
		'template' : {
			'left' : 'resources/template/routes/concept/left.html',
			'tab1' : 'resources/template/routes/concept/tab1.html',
			'tab2' : 'resources/template/routes/concept/tab2.html'	
		},
		'svcLanguage' : ['us', 'kr']
	})
	m.filter('ctxmenuoption', function(){
		return function(option, arg0, arg1, arg2) {
			var outOption = option;
			if(arg0 === 'description') {
				outOption[1].disabled = arg1 == 0 ? true : false;
				outOption[3].disabled = arg2 <= 1 ? true : false;
			} else if(arg0 === 'addRefinements') {
				if(arg1 == -1) {
					outOption[1].disabled = true;
					outOption[2].disabled = true;
					outOption[3].disabled = true;
				} else if(arg1 == 0) {
					outOption[1].disabled = false;
					outOption[2].disabled = arg2 > 1 ? false : true;
					outOption[3].disabled = true;
				} else if(arg1 > 0) {
					outOption[1].disabled = true;
					outOption[2].disabled = true;
					outOption[3].disabled = false;
				}
			}
			return outOption;
		}
	})
	.config(conceptConfig);
	
	conceptConfig.$inject = ['$urlRouterProvider', '$stateProvider'];
	function conceptConfig($urlRouterProvider, $stateProvider, $scope) {
		$stateProvider.state('concept', {
			url : '/concept',
			templateUrl : 'resources/template/routes/concept.html'//,
			//,controller : 'ConceptController'
				
			, onExit :function($state){
				//console.log($scope);
			}
		})
		.state(
			'concept.modify',
			{
				url : '/{editMode}/{targetId}',
				onEnter: [
				    '$state',
				    function($state) {
				    	
				    }
				]
			}
		);
	}
})();