(function () {
	
	'use strict';
	
	angular
		.module('app.referenceset')
		.controller('RefsetController', RefsetController);
	
	RefsetController.$inject = ['$scope', '$window','$document', '$element', '$timeout' , 'refsetservice', 'apiservice', 'userservice'];
	function RefsetController($scope, $window, $document, $element, $timeout, refsetservice, apiservice, userservice) {
		
		var vm = this;
		
		vm.collapseSb = _collapseSb;
		vm.expandSb = _expandSb;
		
		vm.isCollapsed = false;
		vm.viewSrch = false;
		vm.loading = false;
		
		var splrWidth = 5, panelX = 0, panelWidth = 0,
			expandWidth = 0,
			elemSb = {}, elemM = {};
		
		_activate();
		
		function _activate() {
			// element 참조
			elemSb = $element.find('.iui-sidebar');
			elemM = $element.find('#re-main');
			
			angular.element($window)
	          .bind('resize', _windowResize)
	          .trigger('resize');
			
			
			// 사이드바 핸들 처리
			$element.find('.iui-sb-handle').on('mousedown', function (e) {
				e.preventDefault();
				
				$document.on('mousemove', _sbHandleMove);
				$document.on('mouseup', _sbHandleUp);
			});
			

			// splitter 이벤트 처리
			$element.find('#re-panel > .p_splitter').on('mousedown', function (e) {
				e.preventDefault();
				
				panelX = $element.find('#re-panel').offset().left;
		    	panelWidth = $element.find('#re-panel').outerWidth();

				$document.on('mousemove', function (e) {
					_splrResize(panelWidth, splrWidth, panelX, e.pageX);
					angular.element(this).css('cursor', 'ew-resize');
				});
				
				$document.on('mouseup', function (e) {
					angular.element(this).css('cursor', 'initial');
					$document.off('mousemove mouseup');
				});
			});
	
			var elemW = parseInt($element.css('width'), 10);
			panelWidth = parseInt($element.find('#re-panel').css('width'), 10);
			panelX = elemW - panelWidth;
			// .sd_panel width
			// .splitter width
			// .sd_panel x
			// .mouse x
			_splrResize(panelWidth, splrWidth, panelX, -1);
		};
		
		// 사이드바 사이즈 조절 반영
		function _changeSb(w) {
			elemSb.css('width', w + 'px');
			elemM.css('margin-left', w + 'px');
			
			panelWidth = parseInt($element.find('#re-panel').css('width'), 10);
			panelX = w;
			_splrResize(panelWidth, splrWidth, panelX, -1);
		};
		
		// Collapse sidebar
		function _collapseSb() {
			// Expand width 저장
			//_saveSb();
			
			// Collapse 상태로 변경
			//_changeSb(colW);
			
			vm.isCollapsed = true;
			
			expandWidth = parseInt($element.find('.iui-sidebar').css('width'), 10);
			panelWidth = expandWidth + panelWidth - 40;
			panelX = 40;
			_splrResize(panelWidth, splrWidth, panelX, -1);
		};
		
		// Expand sidebar
		function _expandSb() {
			// 최근 Expand width로 복원
			//_restoreSb();
			
			vm.isCollapsed = false;
			
			panelWidth = panelWidth - expandWidth + 40;
			panelX = expandWidth;
			_splrResize(panelWidth, splrWidth, panelX, -1);
		};
		
		function _sbHandleMove(e) {
			var x = e.pageX;
			
			if (x >= 300) {
				_changeSb(x);
				$element.css('cursor', 'col-resize');
			}
		};
		
		function _sbHandleUp() {
			$document.off('mousemove', _sbHandleMove);
			$document.off('mouseup', _sbHandleUp);
			$element.css('cursor', 'initial');
		};
		
		
		/**
		 * panelW: .sd_panel 가로길이
		 * splrW: .p_splitter 가로길이
		 * panelX: 전체 페이지로부터 .sd_panel의 X좌표 값
		 * moveX: 마우스다운+무브 상태에서의 X좌표 값
		 * 
		 * mW: Master width
		 * sW: Slave width
		 */
		function _splrResize(panelW, splrW, panelX, moveX) {
			var mW = 0, sW = 0, sL = 0;
			
			moveX = moveX === -1 ? panelX+300: moveX;
			
			mW = moveX - panelX;
			sW = panelW - mW - splrW;
			sL = mW + splrW;
			
			if (mW >= 300) {
				$element.find('#re-panel > .p_master').css('width', mW + 'px');
				$element.find('#re-panel > .p_splitter').css('left', mW + 'px');
				$element.find('#re-panel > .p_slave').css('left', sL + 'px').css('width', sW + 'px');
			}
		};
		
		function _windowResize($event) {
			// bodyH:body높이, headerH:header높이, baseH:body높이에서 header높이를 뺀 컨텐츠 영역 높이
			var bodyH = 0, headerH = 0, baseH = 0, elemPanel = $element.find('#re-panel');
			bodyH = angular.element('body').css('height').replace('px', '');
			headerH = angular.element('#header').css('height').replace('px', '');
			
			baseH = bodyH - headerH;
			
			elemPanel.css('height', baseH + 'px');
        	
        	panelX = elemPanel.offset().left;
	    	panelWidth = elemPanel.outerWidth();
        	//_splrResize(panelWidth, splrWidth, panelX, d)

	    	// 왼쪽 사이드바 컨테이너(.iui-sb-container) 높이
	    	// baseH - 사이드바 툴 높이(30px)
	    	var sbcH = baseH - 30;
	    	$element.find('.iui-sb-container').css('height', sbcH + 'px');
	    	
	    	
	    	// 메인 패널 서치쿼리 높이
	    	$element.find('div[ng-controller="RefsetSearchController as rss"]').css('height', baseH + 'px');
	    	
	    	// 멤버 리스트 높이
	    	$element.find('div[ng-controller="MemberController as m"]').css('height', baseH + 'px');
	    	
	    	_splrResize(panelWidth, splrWidth, panelX, -1);
        };
        
        $scope.$watch('rs.viewSrch', function (isEdit) {
              isEdit ? _collapseSb():_expandSb();
              vm.loading = true;
              $timeout(function () {
            	  angular.element($window).trigger('resize');
            	  vm.loading = false;
              }, 500);
              
        });

	};
	
})();