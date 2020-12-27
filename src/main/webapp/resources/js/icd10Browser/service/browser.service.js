/**
 * ICD10 Browser: Browser Service
 */

(function () {
	
	'use strict';
	
	angular
		.module('app.icd10Browser')
		.service('icd10browserservice', BrowserService);
	
	BrowserService.$inject = ['$window', '$log'];
	function BrowserService($window, $log) {

		var service = this;
	
		// search history key for localstorage
		var srchHistKey = 'icd10SrchHistory';
		// iso language-country
		var langCtry = 'en-us';
		
		var localStorage;
		
		
		
		// ----------------------------------------
		// Public methods declare
		// ----------------------------------------
		
		this.addSearchHistory = addSearchHistory;
		this.getSearchHistoryList = getSearchHistoryList;
		this.removeSearchHistoryAll = removeSearchHistoryAll;
		
		this.setLanguage = setLanguage;
		this.getLanguage = getLanguage;
		
		
		
		
		activate();
		
		// ----------------------------------------
		// Public methods implements
		// ----------------------------------------
		
		function activate() {
			localStorage = getStorage();
		}
		
		/**
		 * @name addSearchHistory
		 * @desc
		 * @param {string} srch
		 */
		function addSearchHistory(srch) {
			var obj = {
				data: srch,
				time: new Date()
			}
			addItem(srchHistKey, obj);
		};
		
		
		/**
		 * @name getSearchHistoryList
		 * @desc
		 */
		function getSearchHistoryList() {
			//return JSON.parse(getItem(srchHistKey));
			return getItem(srchHistKey);
		};
		
		
		function removeSearchHistoryAll() {
			removeItem(srchHistKey);
		};
		
		
		/**
		 * @name setLanguage
		 * @desc
		 * @param {String} lang
		 */
		function setLanguage(lang) {
			langCtry = lang;
			$log.debug('<Method call> ICD10 SetLanguage: ' + lang);
		};
		
		
		/**
		 * @name getLanguage
		 * @desc
		 */
		function getLanguage() {
			return langCtry;
		};
		
		
		
		
		// ----------------------------------------
		// Private methods
		// ----------------------------------------
		
		/**
		 * @name getStorage
		 * @desc 사파리 private mode에서 localstorage setItem을 호출하면 QuotaExceededError가 발생된다.
			     우리는 이를 감지해서 페이지가 멈추지않도록 피해야한다. 
			     참고: http://stackoverflow.com/questions/14555347/html5-localstorage-error-with-safari-quota-exceeded-err-dom-exception-22-an
		 */
		function getStorage() {
		    var storageImpl;

		    if (typeof $window.localStorage === 'object') {
			     try { 
			    	 $window.localStorage.setItem("storage", ""); 
			    	 $window.localStorage.removeItem("storage");
			        storageImpl = $window.localStorage;
			     }
			     catch (err) { 
			         storageImpl = new LocalStorageAlternative();
			         $log.debug('Your web browser does not support storing settings llocally. In Safari, the most common cause of this is using "Private Browsing Mode". Some settings may not save or some features may not work properly for you.')
			     }
		    }

		    return storageImpl;
		}
	
		
		/**
		 * @name LocalStorageAlternative
		 */
		function LocalStorageAlternative() {

		    var structureLocalStorage = {};

		    this.setItem = function (key, value) {
		        structureLocalStorage[key] = value;
		    }

		    this.getItem = function (key) {
		        if(typeof structureLocalStorage[key] !== 'undefined') {
		            return structureLocalStorage[key];
		        }
		        else {
		            return null;
		        }
		    }

		    this.removeItem = function (key) {
		        structureLocalStorage[key] = undefined;
		    }
		};
		
		
		/**
		 * @name getItem
		 * @desc localStorage에서 key의 item 반환
		 * @param {string} key localStorage key 
		 */
		function getItem(key) {
			return localStorage.getItem(key);
		};
		
		
		/**
		 * @name addItem
		 * @desc
		 * @param {string} key
		 * @param {string} item
		 */
		function addItem(key, item) {
			var obj = JSON.parse(getItem(key));
			
			if (obj == null) { obj = [] };
			obj.push(item);
			
			localStorage.setItem(key, JSON.stringify(obj));
		};
		
		
		/**
		 * @name removeItem
		 * @desc
		 * @param {String} key
		 */
		function removeItem(key) {
			localStorage.removeItem(key);
		};
		
		// localStorage 내용 변경 시 storage 이벤트 발생됨
		// window.addEventListener('storage', fn)
		// event = key, oldValue, newValue, url
		// 서로 다른 윈도우(브라우저)에서 변경되면 이벤트가 발생되고, 같은 브라우저에서는 발생하지 않음.
		//
		// 브라우저가 읽어들이는 순서: 페이지가 로드되기 전, 물리적으로 저장되어 있는 데이터를 읽어 메모리에 올려놓음.
		//
		// 저장되는 곳(물리적으로 저장됨)
		// 크롬: SQLite
		//   MacOS
		//     위치: ~/Library/Application Support/Google/Chrome/Default/Local Storage
		//     파일: 프로토콜_도메인_인덱스.localstorage
	}
})();
