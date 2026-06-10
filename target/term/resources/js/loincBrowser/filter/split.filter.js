(function () {
	
	'use strict';
	
	angular
		.module('app.loincBrowser')
		.filter('split', SplitFilter);
	
	SplitFilter.$inject = [];
	function SplitFilter() {
		return function(input) {//, delimiter) {
			if (typeof input === 'undefined') {
				return false;
			}
			
		    var delimiter = ';'; //delimiter || ';'
		    return input.split(delimiter);
		}
	}
})();