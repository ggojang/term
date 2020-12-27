(function () {
	
	'use strict';
	
	angular
		.module('app.referenceset')
		.directive('referencesetLayout', referencesetLayout);
	
	referencesetLayout.$inject = ['$timeout', '$window'];
	function referencesetLayout($timeout, $window) {
		var directive = {
			restrict: 'EA',
			transclude: true,
			template: '<p ng-hide></p>',
			link: link
		};
		return directive;
		
		function link(scope, element, attributes, controller, transclude) {
			
			transclude(
				function injectLinkedClone(clone) {
					element.append(clone);
				}
			);
			
			element.layout({
				center__paneSelector: ".outer-center",
				west__paneSelector:   ".outer-west",
				west__size: "25%",
				fxName:	"none",		// none, slide, drop, scale
				west__childOptions:
				{
					center__paneSelector: ".outer-west-center",	// Referenceset List
					south__paneSelector:  ".outer-west-south", 	// Field List
					south__size: "55%"
				}
			});
			
			////
			
			var w = angular.element($window);
	        w.bind('resize', resize);
	        w.trigger('resize');
	        
	        function resize($event) {
	        	var target = $event.target,
	        	innerHeight = target.innerHeight,
	        	headerHeight = $('#header').height();
	        	// layout height 변경
	        	element.css('height', innerHeight - headerHeight + 'px');
	        };
	        
		};
	};
	
	
})();