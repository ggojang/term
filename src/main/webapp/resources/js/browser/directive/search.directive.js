(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.directive('search', search);
	
	search.$inject = ['$window'];
	function search($window) {
		var directive = {
			restrict : 'EA',
			templateUrl : 'resources/template/snomedctBrowser/search.html',
			controller : 'BrowserSearchController as vm',
			link : link
		};
		return directive;

		function link(scope, element, attributes) {

			var w = angular.element($window);
			w.bind('resize', resize);
			w.trigger('resize');

			function resize($event) {
				var target = $event.target, innerHeight = target.innerHeight, height = innerHeight
						- 50 - 34 - 52 - 35;
				// 브라우저 안쪽 높이 - 전체메뉴 높이 - 탭 높이 - 검색창 높이 - 메시지 높이
				element.find('#search-result').css('height', height + 'px');
			}
		};
	};
	
})();