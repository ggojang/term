(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.directive('description', description);
	
	description.$inject = [];
	function description() {
		var directive = {
			restrict: 'EA',
			bindToController: {
				save: '&',
				descriptions: '=',
				conceptId: '@'
			},
			controller: 'BrowserDescriptionController',
			controllerAs: 'de',
			templateUrl: 'resources/template/concept/description.html',
			link: link
		};
		return directive;

		function link(scope, element, attributes, controller) {
			
			scope.$watchCollection('de.descriptions', function (ds) {
				if (!angular.isDefined(ds)) {
					return;
				}
				
				controller.lang.extra = [];
				controller.groupByLanguage(ds);
				var targetDescs = controller.data.length === 3 ? controller.data[2].descriptions:[];

				//if (controller.ui.other.dataIdxs.length > 0) {
				controller.initFields(controller.newItem, targetDescs);
				//}
			});
		};
	};
	
})();