(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.controller('BrowserSearchController', BrowserSearchController);
	
	BrowserSearchController.$inject = ['$scope', '$state', '$mdSidenav', 'utilservice', 'apiservice', 'browserservice'];
	function BrowserSearchController($scope, $state, $mdSidenav, utilservice, apiservice, browserservice) {
		var vm = this;
		
		vm.getDefinitionStatusFlagValue = utilservice.getDefinitionStatusFlagValue;
		vm.getDefinitionStatusName = utilservice.getDefinitionStatusName;
		vm.getDescriptionType = utilservice.getConceptType;
		
		vm.search = _search;
		vm.check = _check;
		vm.more = _more;
		vm.go = _go;
		vm.isOpen = _isOpen;
		vm.isCheckedTag = _isCheckedTag;
		vm.selectTag = _selectTag;
		vm.toggle = _buildToggle('left');
		
		vm.list = [],
		vm.tree = [],
		vm.tagList = [],
		vm.loadedSize = 0,
		vm.totalSize = 0,
		vm.options = {
			word: '',
			tagList: [],
			match: 'partial',
			state: 'active',
			page: 1,
			size: 100
			//order: 'length',
		},
		vm.loading = false;
		vm.moreLoading = false;
		
		function _initValue() {
			vm.loadedSize = 0;
			vm.totalSize = 0;
			vm.options.tagList = [];
			vm.options.page = 1;
			vm.options.size = 100;
		}
		
		function _check($event) {
			if (event.keyCode === 13) {
				_initValue();
				_search(vm.options);
			}
		}
		
		function _more() {
			vm.options.page++;
			_search(vm.options);
		}
		
		
		function _go(state, params) {
			$state.go(state, params);
		}
		
		function _isOpen() {
			return $mdSidenav('left').isOpen();
		}
		
		function _isCheckedTag(item, list) {
			return list.indexOf(item) > -1;
		};
		
		function _selectTag(item, list) {
			var idx = list.indexOf(item);
			if (idx > -1) {
				list.splice(idx, 1);
			} else {
				list.push(item);
			}
		};
		
		function _search(opt) {
			if (opt.word.length < 1) {
				return;
			}
			
			// 로딩 시작
			if (opt.page === 1) {
				vm.loading = true;
			} else {
				vm.moreLoading = true;
			}
			
			var options = {
				q: opt.word,
				match: opt.match,
				state: opt.state
			};
			
			// 검색 결과내에서 시맨틱태그 필터를 추가한 내역이 있다면 옵션에 시맨틱태그 설정
			if (angular.isDefined(opt.tagList) && opt.tagList.length > 0) {
				options.semanticfilter = opt.tagList;
			}
			
			// Api Service
			apiservice.searchByWord(options, opt.page, opt.size)
			.then(function (response) {
				// Success handler
				var page = response.page,
					contentList = page.content,
					contentListSize = page.numberOfElements,
					totalSize = page.totalElements,
					isFirst = page.first,
					tagList = response.semanticTags;
				
				if (isFirst) {
					vm.list = contentList;
					vm.loadedSize = contentListSize;
				} else {
					angular.forEach(contentList, function(content, index) {
						vm.list.push(content);
					});
					
					vm.loadedSize += contentListSize;
				}
				
				vm.totalSize = totalSize;
				
				if (opt.tagList.length === 0) {
					vm.tagList = tagList;
				}
				
				vm.loading = false;
			}, function () {
				// Error handler
			})
			.finally(function () {
				if (opt.page === 1) {
					vm.loading = false;
				} else {
					vm.moreLoading = false;
				}
			});
		};
		
		function _buildToggle(id) {
			return function () {
				$mdSidenav(id).toggle()
				.then(function () {
					//console.log('buildToggle', id);
				});
			}
		};
		
		$scope.$watch('vm.options.tagList', function (tagList, oldTagList) {
			if (tagList !== oldTagList) {
				vm.loadedSize = 0;
				vm.totalSize = 0;
				vm.options.page = 1;
				vm.list = [];
				_search(vm.options);
			}
		}, true); // deep watch
		
		$scope.$watch('vm.options.match', function (match, oldMatch) {
			if (match !== oldMatch) {
				vm.loadedSize = 0;
				vm.totalSize = 0;
				vm.options.page = 1;
				vm.options.tagList = [];
				_search(vm.options);
			}
		});
		
		$scope.$watch('vm.options.state', function (state, oldState) {
			if (state !== oldState) {
				vm.loadedSize = 0;
				vm.totalSize = 0;
				vm.options.page = 1;
				vm.options.tagList = [];
				_search(vm.options);
			}
		});
	};
	
})();