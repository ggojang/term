(function () {
	
	'use strict';
	
	angular
		.module('app.common')
		.service('userservice', UserService);
	
	UserService.$inject = [];
	function UserService() {
		
		var service = this;
	    
	    service.effectiveTime = '20200309';
	    service.lang = 'en';
	    
	    //service.oldConcept = '';
	    //service.oldLoincVer = '';
	    //service.oldLoincCode = '';
	    //service.oldIcd10Code = '';
	    
	    // korean extension
	    service.extension = {
	    	namespace: '1000300',
	    	moduleId: '247771000300108',
	    	langRefsetId: '247781000300103'
	    };
	};
	
})();
