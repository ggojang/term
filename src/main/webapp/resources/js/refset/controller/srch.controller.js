(function () {
	
	'use strict';
	
	angular
		.module('app.referenceset')
		.controller('RefsetSearchController', RefsetSearchController);
	
	RefsetSearchController.$inject = ['$scope', '$window', '$timeout', '$compile', '$element', '$sce', 'apiservice', 'userservice', 'refsetservice', 'utilservice', 'srchservice', 'rsdataservice'];
	function RefsetSearchController($scope, $window, $timeout, $compile, $element, $sce, apiservice, userservice, refsetservice, utilservice, srchservice, rsdataservice) {
		
		var vm = this;
		
		vm.onKeyup = _onKeyup;
		vm.onItemSelected = _onItemSelected;
		vm.getChildren = _getChildren;
		vm.highlight = _highlight;
		vm.addMember = _addMember;
		
		vm.rootPath = '';
		// {string} 검색어
		vm.srch = '';
		// {boolean} 자동완성 검색 결과 목록에서의 선택 여부
		vm.isSelected = false;
		// {boolean} 자동완성에서 선택한 컨셉의 트리에서의 체크 여부
		vm.treeSelected = false;
		// {boolean} 트리 갱신중 여부
		vm.treeLoading = false; 
		// {Object} 
		vm.model;
		// {Array} 자동완성에서 선택한 컨셉의 트리
		vm.tree;
		// {Array} 자동완성 검색 결과 목록
		vm.matches;
		
		
		
		
		// {string} 자동완성 템플릿
		var sgstTpl = ['<div class="re-autocomplete-suggestions" ng-show="rss.srch.length > 0 && rss.matches.length > 0">',
		             	'<div class="treeTable" ng-hide="rss.model === \'\' || rss.isSelected" >',
		             		'<div class="tr" ng-repeat="match in rss.matches" ng-click="rss.onItemSelected($event, match)">',
		             			'<div class="td tcol0-ac">',
		             				'<button type="button" class="btn btn-default btn-link btn-xs glyphicon glyphicon-option-horizontal"></button>' +
		             			'</div>',
		             			'<div class="td tcol1-ac">',
		             				'<div class="celldiv">',
		             					'<div class="grad"></div>',
		             					'<span ng-bind-html="rss.highlight(match.term, rss.srch)"></span>' +
		             				'</div>',
		             			'</div>',
		             			'<div class="td tcol2-ac">',
		             				'<div class="celldiv">',
		             					'<div class="grad"></div>',
		             					'<span ng-bind="match.fsn" title="{{ match.fsn }}"></span>' +
		             				'</div>',
		             			'</div>',
		             		'</div>',
		             	'</div>',
		            '</div>'].join(''),
        linkFn = $compile(sgstTpl), // {Object} 자동완성 템플릿 컴파일
		sgstHtml = linkFn($scope),
		sgstEl = null; // {Object} 자동완성 엘리먼트 객체
		
		_activate()
		
		function _activate() {
			vm.model = srchservice.model;
			vm.tree = srchservice.tree;
			vm.matches = srchservice.matches;
			
			// 계층 구조 트리의 컨테이너 아이디는 #rss_tree
			// 트리는 사용자가 검색 진행 중 자동완성 목록에서 '계층' 버튼을 선택하면 데이터를 받아서 보여지게 됨
			// 이 트리의 각 노드들은 동적으로 생성이 되는데 체크박스 이벤트 또한 동적으로 주입하기위해 아래와 같이 이벤트를 주입시킨다.
			// .live      - jQuery 1.3+
			// .deletgate - jQuery 1.4.3+
			// .on        - jQuery 1.7+
			$element.on('change', '#re-srch-list input[type="checkbox"]', _change);
			
			angular.element($window)
				.bind('resize', _resize);
			
			$timeout(function() {
				_resize();
			});
		};
		
		function _onKeyup($event) {
			if (vm.isSelected) {
				vm.isSelected = false;
			}
			
			// input의 값이 비어있을 때 트리모델 초기화
			if (vm.srch.length === 0) {
				//vm.tree.length = 0;
				vm.matches.length = 0;
				return;
			}
			
			if (vm.srch.length > 0) {
				var el = angular.element('#re_srch');
				sgstHtml
					.css('top', el.offset().top + el.outerHeight() + 'px')
					.css('left', el.offset().left);
				
				sgstEl = angular.element('body').append(sgstHtml);
				// if 숫자형식 && 8~18자
				apiservice.getSuggestResultList(vm.srch, 15)
				.then(function (rslts) {
					vm.matches = rslts;
				});
			}
		};
		
		/**
		 * @name _onItemSelected
		 * @desc 자동완성 목록에서 발생한 클릭 이벤트를 처리하는 메소드
		 * @param {Object} $event
		 * @param {Object} cpt 컨셉 객체 
		 * @param {boolean} cpt.conceptActive
		 * @param {boolean} cpt.descriptionActive
		 * @param {string} cpt.conceptId
		 * @param {string} cpt.fsn
		 * @param {number} cpt.length // TODO 무조건 0으로 들어옴
		 * @param {string} cpt.term
		 */
		function _onItemSelected($event, cpt) {
			
			// 선택상태 활성화
			//   true: 자동완성 목록 숨김
			vm.isSelected = true;
			// 자동완성 목록 초기화
			vm.matches.length = 0;
			// 검색어 초기화
			vm.srch = '';
			
			// Document로부터 자동완성 목록 DOM 분리
			sgstHtml.detach();
			
			apiservice.getPathList(cpt.conceptId, userservice.effectiveTime)
			.then(function (paths) {
				vm.rootPath = paths[0];
				// 어떤 Object가 선택했는지
				var tagName = $event.target.tagName.toLowerCase();
				if (tagName === 'button') {
					// 자동완성 목록에서 선택한 컨셉의 트리 초기화
					vm.tree.length = 0;
					
					// 선택한 모델 데이터 결합
					vm.model = cpt;
					
					vm.treeLoading = true;
					
					apiservice.getConceptAndChildren(cpt.conceptId, userservice.effectiveTime)
					.then(function (rslts) {
						var concept = rslts[0],
							children = rslts[1],
							node = {};
						
						node = angular.copy(concept);
						node.children = children;
						node.path = vm.rootPath;
						node.open = true;
						node.checked = false;
						
						// FIXME: 예비멤버로 추가되었거나 이미멤버일 경우 disabled=true
						/*
						node.disabled = _hasMember(node.conceptId) ? true:false;
						angular.forEach(node.children, function(c, i) {
							c.disabled = _hasMember(c.conceptId) ? true:false;
						});
						*/
						
						_spreadCheckState(node);
						
						vm.tree.push(node);
					})
					.finally(function () {
						vm.treeLoading = false;
					});
				} else {
					cpt.path = vm.rootPath;
					var mbr = refsetservice.convertToMember(cpt, true);
					
					refsetservice.addMember(mbr);
				}
			});
		};
		
		/**
		 * @name _getChildren
		 * @desc 트리 컨셉에서 자식을 호출하는 메소드
		 * @param {Object} cnpt 컨셉
		 * @param {string} cnpt.active
		 * @param {string} cnpt.conceptId
		 * @param {string} cnpt.term
		 * @param {string} cnpt.semanticTag
		 * @param {Object} cnpt.definitionStatus
		 * @param {string} cnpt.definitionStatus.id
		 * @param {string} cnpt.definitionStatus.name
		 * @param {string} cnpt.chidlrenCount
		 * @param {string} cnpt.descendantCount
		 * @param {string} cnpt.disabled
		 * @param {string} cnpt.checked
		 * @param {string} cnpt.path
		 */
		function _getChildren(cnpt) {
			var cnptId = cnpt.conceptId;
			
			// 자식 수가 0일 경우 반환
			if (cnpt.childrenCount === 0) {
				return;
			}
			
			// 오픈 상태 반전
			cnpt.open = !cnpt.open;
			
			// 이미 불러온 자식이 있다면 반환
			if (angular.isDefined(cnpt.children)) {
				return;
			}
			
			// 로딩 시작
			cnpt.loading = true;
			
			// 자식 호출
			rsdataservice.getChildren(cnpt.conceptId)
			.then(function(chdrn) {
				cnpt.children = chdrn;
				
				// FIXME: 예비멤버로 추가되었거나 이미멤버일 경우 disabled=true
				/*
				angular.forEach(cnpt.children, function(c, i) {
					c.disabled = _hasMember(c.conceptId) ? true:false;
				});
				*/
				
				_spreadCheckState(cnpt);
			}, function (err) {
				//
			})
			.finally(function() {
				cnpt.loading = false;
			});
		};
		
		
		// 트리 체크에 관한 처리를 하는 메소드 구현체 시작
		
		function _change(e) {
			var elem = angular.element(e.target), // input element
				node = elem.scope().$parent.n, // tree node data
				checked = this.checked,
				wrapper = elem.closest('.tr');     //input element 위로 가장 가까운 li
			
			
			// 나의 체크 상태를 변경 - ngModel에 바인딩 되어있으므로 불필요
			// elem.prop('checked', checked);
			//wrapper.find('input[type="checkbox"]').prop('checked', checked);

			if (node.children) {
				utilservice.safeApply($scope, function () {_selectAllChildren(node);});
				wrapper.find('input[type="checkbox"]').prop('indeterminate', false);
			}
			
			// 부모와 형제들 확인 및 상태 변경
			_checkParentAndSibling(wrapper, checked);
			
			
			var rootCb = $element.find('#re-srch-list > .treeTable > .tr:first-child').find('input[type="checkbox"]');
			utilservice.safeApply($scope, function () {
				var selected = rootCb.prop('checked') ? true:(rootCb.prop('indeterminate') ? true:false);
				vm.treeSelected = selected;
				_resize();
			});
			
		};
		
		function _selectAllChildren(node) {
			var children = node.children,
				leng = children.length,
				cnode = {};
			for (var i = 0; i < leng; i++) {
				cnode = children[i];
				cnode.checked = node.checked;
				
				if (cnode.children) {
					_selectAllChildren(cnode);
				}
			}
		};
		
		function _checkParentAndSibling(wrapper, checked) {
			// 대상 범위에서 벗어나면 진행하지않고 반환
			if (angular.element('#re-srch-list').find(wrapper).length === 0) {
				return;
			}
			
			var siblings = wrapper.siblings(),
				all = true;
			
			// siblings: .tr list
			siblings.each(function () {
				return all = (angular.element(this).children('.td:first-child').find('input[type="checkbox"]').prop('checked') === checked);
				// return true: 다음 sibling 탐색
				// return false: 탐색 종료
			});
			
			// all === true: 자신의 체크값과 형제들의 체크값이 같음
			// all === false: 자신의 체크값과 형제들의 체크값이 다른경우가 하나이상 존재
			
			// 경우1. 형제들의 체크상태가 같지않은 경우, 부모 체크상태를 '-'로 변경
			// sibling 체크상태가 모두 같으면, 부모의 상태를 변경
			// 경우2-1. sibling 체크상태가 모두 같으면서, 자신이 체크상태인 경우
			// 경우2-2. sibling 체크상태가 모두 같으면서, 자신이 체크상태가 아닌 경우
			
			if (!all) {
				_changeParent(wrapper, false, true, true);
			} else {
				_changeParent(wrapper, checked, false, false);
				_checkParentAndSibling(wrapper.parent().closest('.tr'), checked);
			}
		};
		
		function _changeParent(wrapper, checked, indeterminate, recursive) {
			
			var pWrapper = wrapper.parent().closest('.tr');
			
			// 대상 범위에서 벗어나면 진행하지않고 반환
			if (angular.element('#re-srch-list').find(pWrapper).length === 0) {
				console.log('end parent');
				return;
			}
			var pCheckbox = pWrapper.children('.td:first-child').find('input[type="checkbox"]'),
				pNode = pWrapper.scope().$parent.n;
			
			//console.log('...', pNode);
			utilservice.safeApply($scope, function () {
				pNode.checked = checked;
			});
			pCheckbox.prop('indeterminate', indeterminate);
			
			if (recursive) {
				_changeParent(pWrapper, checked, indeterminate, recursive);
			}
		};
		
		
		
		/**
		 * @name _addMember
		 * @desc 레퍼런스세트 예비멤버로 추가하는 메소드
		 */
		function _addMember() {
			if (vm.treeSelected) {	
				var rootNode = vm.tree[0], mbrs = [], mbr = {};
				
				if (rootNode.checked) {
					// FIXME: 예비멤버로 추가되었거나 이미멤버일 경우 disabled=true
					/*
					rootNode.disabled = true;
					*/
					mbr = refsetservice.convertToMember(rootNode, false);
					mbrs.push(mbr);
				}
				
				if (rootNode.children) {
					var chdMbrs = _recursive(rootNode.children);
					Array.prototype.push.apply(mbrs, chdMbrs);
				}
				
				if (mbrs.length > 0) {
					refsetservice.addMember(mbrs);
				}
				
				// 추가 된 후 선택되었던 노드들은 선택해제를 위해 전체 트리 해제
				vm.tree[0].checked = false;
				vm.treeSelected = false;
				
				_selectAllChildren(vm.tree[0]);
				$element.find('#re-srch-list input[type="checkbox"]').prop('indeterminate', false);
				
				_resize();
			}
		};
		
		/**
		 * @name _recursive
		 * @desc node의 자식,자손 탐색을 통하여 예비멤버 객체 반환하는 메소드
		 * @param {Array} nodes
		 */
		function _recursive(nodes) {
			var len = nodes.length,
				node, conceptId,
				addedIds = [], mbrs = [], mbr = {};
			
			for (var i = 0; i < len; i++) {
				node = nodes[i];
				conceptId = node.conceptId;
				// 체크상태 + 예비멤버목록에 존재하지않는다면
				if (node.checked && addedIds.indexOf(conceptId) === -1) {
					// FIXME: 예비멤버로 추가되었거나 이미멤버일 경우 disabled=true
					/*
					node.disabled = true;
					*/
					mbr = refsetservice.convertToMember(node, false);
					mbrs.push(mbr);
					addedIds.push(conceptId);
				}
				if (node.children) {
					var chdMbrs = _recursive(node.children);
					Array.prototype.push.apply(mbrs, chdMbrs);
				}
			}
			
			return mbrs;
		};

		function _highlight(text,srch) {
			if (!srch) {
				return $sce.trustAsHtml(text);
			}
			return $sce.trustAsHtml(text.replace(new RegExp(srch.match(/\S+/g).join('|'), 'gi'), '<span class="highlightedText">$&</span>'));
		};

		function _spreadCheckState(node) {
			var checked = node.checked ? node.checked:false,
				hasChildren = typeof node.children !== 'undefined';
			
			if (hasChildren) {
				var children = node.children;
				for (var i = 0; i < children.length; i++) {
					children[i].checked = checked;
				}
			}
		};
		
		function _resize() {
			var bodyH = 0, headerH = 0, h = 0;
			
			bodyH = angular.element('body').css('height').replace('px', '');
			headerH = angular.element('#header').css('height').replace('px', '');
			h = bodyH - headerH - 35 - 40 - (vm.treeSelected ? 40:0);
			
			
			angular.element('#re-srch-list').css('height', h + 'px');
		};
		
		
		
		///*
		// * @deprecated 지속적으로 digest 루프돌면서 newMembers 체크하므로 성능저하예상되어 member가 추가될때, 지워질때 체크하도록 변경
		function _hasMember(id) {
			//console.log('hasId:'+id, refsetservice.newMembers.find(function(o) {
			//	return o.conceptId === id;
			//}));
			
			return refsetservice.newMembers.find(function(o) {
				return o.conceptId === id;
			}) ? true:false;
		};
		//*/
	};
	
})();