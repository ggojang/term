/**
 * Common Loading Indicator Directive
 */
(function() {
	'use strict';
	
	
	angular
		.module('app.common')
		.directive('icLoading', icLoading);
	
	icLoading.$inject = ['$timeout'];
	
	function icLoading($timeout) {
		var directive = {
				restrict: 'EA',
				template: [
				    '<div class="ic-loading" ng-show="loading"></div>',
					'<img ng-show="loading" src="resources/images/loading.gif" style="position:absolute;z-index:9999"/>'
				].join(''),
				scope: {
					loading : '=loading'
				},
				// replace : true,
				link: link
			};
		return directive;
		function link(scope, element, attributes, controller, transclude) {
			scope.$watch('loading', function(nv, ov) {
				var parent = angular.element(attributes.target);
				var offset = attributes.offset === 'false' ? {top:0,left:parent.position().left} : parent.position();
				var width = parent.outerWidth(), height = parent.height();
				element.children('div').css({
					top: 0,
					bottom: 0,
					left: 0,
					right: 0,
					margin:'auto'
					//'left' : '0px',
					//'top' : offset.top+'px',
					//'top' : '0px',
					//'width' : width+'px',
					//'height' : height+'px'
				});
				var imgTop = offset.top + ((height / 2) >= 200 ? 150 : (height / 2)) - 15, imgLeft = offset.left + (width/2) - 15;
				element.children('img').css({
					//top : imgTop + 'px',
					top: '150px',
					left: 0,
					right: 0,
					margin: 'auto'
					//left : ((width/2) - 15)+'px'
				});
				
				
			});
		}
	}
})();