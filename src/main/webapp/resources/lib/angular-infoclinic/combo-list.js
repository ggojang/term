/**
 * InfoClinic.Co.,Ltd.
 * combo-list.js
 * IIFE scoping: To avoid polluting the global scope with our function declarations that get passed into Angular, ensure build tasks wrap the concatenated files inside an IIFE
 * All the code is wrapped in the IIFE
 * Reference: http://www.codestyle.co/Guidelines/angularjs
 * Reference: https://github.com/toddmotto/angular-styleguide
 */
(function () {

  'use strict';

  angular
    .module('combolist', []);

  angular
    .module('combolist')
    .constant('config', {
      BASE_URL: 'http://api.infoclinic.co/snomedct'
      //BASE_URL: 'http://was.infoclinic.co/snomedct',
      //BASE_URL: 'http://localhost:9000'
    });


  // Ctrl.js
  function ComboListController ($scope, $q, $http, config) {

    var vm = this;

    //// ---------- PUBLIC ----------

    //// PUBLIC Fields
    vm.treeNodeList = [];
    //vm.value = {
    //  id: null,
    //  name: null
    //};

    //// PUBLIC Methods
    vm.callChildren = _callChildren;
    vm.termBeautifier = _termBeautifier;

    //// ---------------- PRIVATE ----------------

    _activate();

    //// PRIVATE fields

    //// PRIVATE Functions - Public Methods Implementation

    function _activate() {
      // conceptId의 자식목록 가져오기
      _getChildren(vm.conceptId)
      .then(function(response) {
        vm.treeNodeList = response;
      });
    }


    function _getChildren(conceptId) {
      var url = config.BASE_URL + '/terminology/concept/' + conceptId + '/children';

      var deferred = $q.defer();
      $http({
        method: 'get',
        url: url
      }).then(
        function (response) {
          deferred.resolve(response.data);
        },
        function (response) {
          deferred.reject(response.data);
        }
      );
      return deferred.promise;
    }


    function _callChildren(node) {
      if (node.descendantCount == 0) return;

      node.open = !node.open;

      if (angular.isDefined(node.children)) return;

      node.loading = true;

      _getChildren(node.conceptId)
      .then(function(response) {
        node.children = response;
      })
      .finally(function() {
        node.loading = false;
      });
    }


    function _termBeautifier(term) {
      if (typeof term === 'undefined') return '';

      if (term.indexOf('(') == -1) return term;

      return term.substring(0, term.lastIndexOf('('));
    }
  }

  angular
    .module('combolist')
    .controller('ComboListController', ComboListController);

  // combo-list.directive.js
  function ComboListDirective ($compile, $timeout, $document) {
    var iconArrowTop = 'glyphicon-triangle-top',
        iconArrowBottom = 'glyphicon-triangle-bottom',
        emptyMessage = 'selected...',
        template = '<div '
                + '  ng-keyup="vm.onKeyup($event)" '
                + '  ng-blur="vm.onBlur($event)" '
                + '  ng-dblclick="vm.onDblclick($event, true)" '
                + '  ng-style="vm.isEditMode() ? '
                + '              { padding: \'6px\' } : '
                + '              { padding: \'8px\' }   ">'
                + '  <span '
                + '    ng-if="vm.isEditMode()" '
                + '    class="combolist-arrow glyphicon" '
                + '    ng-class="vm.isLayerMode() ? '
                + '                \'' + iconArrowTop + '\' : '
                + '                \'' + iconArrowBottom + '\'"></span>'
                + '  <span '
                + '    class="combo-value" '
                + '    ng-class="vm.isEditMode() ? \'ellipsis-span\':\'\'" '
                + '    ng-bind-html="vm.isEditMode() && vm.model.name == null ? '
                + '                    \'' + emptyMessage + '\' : '
                + '                    (!vm.isEditMode() && vm.model.name == null ? \'&nbsp;\':vm.termBeautifier(vm.model.name))" '
                + '    ng-attr-conceptId={{vm.model.id}}></span>'
                + '</div>'

    var directive = {
      // 'A' - only matches attribute name
      // 'E' - only matches element name
      // 'C' - only matches clsss name
      // Default: 'EA'
      restrict: 'EA',
      //transclude: true,
      replace: true,
      template: template,
      bindToController: {
        // & - method binding
        // @ - one way binding
        // = - two way binding
        conceptId: '@',
        model: '=model',
        cbComplete: '&'
      },
      scope: {},
      controller: 'ComboListController',
      controllerAs: 'vm',
      link: link
    };
    return directive;

    function link(scope, element, attrs, vm) {
      // 편집모드 관련 메소드
      vm.isEditMode = _isEditMode;
      //vm.setEditMode = _setEditMode;

      // 트리레이어모드 관련 메소드
      vm.isLayerMode = _isLayerMode;
      //vm.setLayerMode = _setLayerMode;

      vm.onDblclick = _onDblclick;
      vm.onBlur = _onBlur;
      vm.setValue = _setValue;
      vm.onKeyup = _onKeyup;

      var editMode = false, layerMode = false;
      var linkFn,
          templateContent = '',
          rootStyle = 'padding:10px;'
                            + 'overflow-y:auto;'
                            + 'overflow-x:hidden;'
                            + 'position:absolute;'
                            + 'background-color:#ffffff;'
                            + 'border:1px solid #979797;'
                            + 'box-shadow:2px 2px 2px #999999;'
                            + 'z-index:100;',
          uLStyle = 'padding-left:5px;',
          liStyle = 'white-space:nowrap;'
                  + 'margin:3px 0;',
          include = '\'/combolist.tpl.html\'',
          repeat = 'node in vm.treeNodeList | orderBy:\'term\'',
          template = '<div style="' + rootStyle + '">'
                   + '  <ul style="' + uLStyle + '">'
                   + '    <li '
                   + '      style="' + liStyle + '"'
                   + '      ng-repeat="' + repeat + '"'
                   + '      ng-include="' + include + '">'
                   + '    </li>'
                   + '  </ul>'
                   + '</div>';

       // 지시자의 부모 스타일 변경
       element.parent().css('padding', 0);

       //if(element[0].addEventListener) {
      //   element[0].addEventListener('blur', _onBlur, true);
       //} else {
      //   element[0].attachEvent('onfocusout', _onBlur);
       //}


       /**
        * 편집 모드를 반환하는 메소드
        * @name _isEditMode()
        * @return {boolean} editMode
        */
       function _isEditMode() {
         return editMode;
       }


       /**
        * 편집 모드를 설정하는 메소드
        * @name _setEditMode(editMode)
        * @param {boolean} editMode
        */
       function _setEditMode(m) {
         editMode = m;

         if (editMode) {
           // 편집 모드일 경우 클릭이벤트 등록
           element.on('click', _onClick);

         } else {
           // 편집 모드가 아닐경우 클릭이벤트 해제
           element.off('click', _onClick);
         }
       }


      /**
       * 트리레이어 모드를 반환하는 메소드
       * @name _isLayerMode()
       * @return {boolean} layerMode
       */
      function _isLayerMode() {
        return layerMode;
      }


      /**
       * 트리레이어 모드를 설정하는 메소드
       * @name _setLayerMode(layerMode)
       * @param {boolean} layerMode
       */
      function _setLayerMode(m) {
        layerMode = m;

        if (layerMode) {
          _appendTreeLayoutToBody();
        } else {
          _removeTreeLayoutFromBody();
        }
      }


      /**
       * 현재 지시자(Directive) 영역 내에서 발생하는 더블클릭 이벤트를 처리하는 메소드
       * @name _onDblclick($event)
       * @param {object} $event
       */
      function _onDblclick($event) {
        // 편집 모드일 경우 반환
        if (_isEditMode()) return;

        // 편집 모드 활성화
        _setEditMode(true);
        // 선택한 값의 가로길이 변경
        element.find('.combo-value').css('width', (element.parent().width() - 35) + 'px');
      }


      /**
       * 클릭 이벤트를 처리하는 메소드
       * @name _onClick($event)
       * @param {object} $event
       * @trigger 지시자 영역에서 클릭(처음 더블클릭이벤트 후 클릭이벤트가 등록됨)
       */
      function _onClick($event) {
        // 편집 모드가 아니라면 반환
        if (!_isEditMode()) return;

        // 버블링 방지
        $event.stopPropagation();

        var bodyHeight = angular.element('body').height(),
            elementOffset = element.offset(),//내자신이나 부모나 약간의 차이뿐 element.parent().offset(),
            layoutHeight = 0,
            elementHeight = element.parent().outerHeight(),
            layoutTop = 0,
            availableBottomSize = 0;

        // 트리레이아웃 상태 토글
        scope.$apply(function() {
          _setLayerMode(_isLayerMode() ? false:true);
        });

        // 편집 모드일 때, 왼쪽에 화살표가 나오고 화살표 오른쪽에 선택한 값이 나오게 된다.
        // 길이가 길어지면 ...으로 표시해주어야 하므로 width를 계산하여 반영한다.
        element.find('.combo-value').css('width', (element.parent().width() - 35) + 'px');

        // 트리레이아웃의 높이 (현재 레이아웃에 있는 컨텐츠 포함)
        layoutHeight = templateContent.outerHeight();
        // 현재 지시자 엘리먼트의 아래쪽 끝지점부터 브라우저의 아래쪽 끝 사이의 높이
        // 아래쪽으로 가용가능한 높이
        availableBottomSize = (bodyHeight - elementOffset.top) - elementHeight;

        // 트리레이아웃의 높이보다 아래쪽으로 가용가능한 높이가 크다면
        if (availableBottomSize > (layoutHeight + 10)) {
          layoutTop = elementOffset.top + elementHeight
          templateContent
            .css('min-height', layoutHeight + 'px')
            .css('max-height', (availableBottomSize - 10) + 'px');
        } else {
          // 트리레이아웃의 높이가 엘리먼트오프셋 높이보다 작다면
          //console.log('LayoutHeight: ' + layoutHeight, element.parent().offset(), element.offset());
          //console.log('ElementHeight: '+ $event.clientY, elementOffset.top, $event);
          if (layoutHeight < elementOffset.top - 10) {

            layoutTop = elementOffset.top - layoutHeight;
          }
          // 트리레이아웃의 높이가 엘리먼트 오프셋 높이보다 크다면
          else {
            layoutTop = 10;
          }

          templateContent
            .css('max-height', (elementOffset.top - 10) + 'px');
        }


        templateContent
          .css('top', layoutTop + 'px')
          .css('left', elementOffset.left + 'px')
          .css('min-width', (templateContent.outerWidth() + 50) + 'px');
      }

      /**
       * 현재 Directive를 벗어났을 때의 이벤트를 처리하는 메소드
       * blur는 focus와는 반대되는 기능
       * @name _onBlur($event)
       * @param {object} $event
       * @trigger 현재 지시자로부터 포커스를 벗어났을 때
       */
      function _onBlur($event) {
        // 편집 모드가 아닐경우 반환
        if (!_isEditMode()) return;

        // 트리레이어 모드일 경우
        if (_isLayerMode()) {
          // 트리레이어 모드 비활성화
          _setLayerMode(false);
        }

        // 편집 모드 비활성화
        _setEditMode(false);
      }


      /**
       *
       */
      function _appendTreeLayoutToBody() {
        // 편집 모드가 아니거나, 편집 모드이면서 레이어 모드가 아니면 반환
        //if (!_isEditMode() || (_isEditMode() && !_isLayerMode())) return;
        linkFn = $compile(template);
        templateContent = linkFn(scope);
        templateContent
          .addClass('combolist-layout');

        angular.element('body').append(templateContent);

        templateContent
          .on('mousedown mouseup', function(e) {
              e.preventDefault();
              return false;
            });
      }


      /**
       *
       */
      function _removeTreeLayoutFromBody() {
        // 편집 모드가 아니거나, 편집 모드이면서 레이어 모드이면 반환
        //if (!_isEditMode() || (_isEditMode() && _isLayerMode())) return;
        angular.element('body > .combolist-layout')
          .off('mousedown mouseup')
          .remove();
      }


      /**
       * 트리레이어로부터 선택한 노드를 모델에 반영하는 메소드
       * @name _setValue(node)
       * @param {object} node
       * @trigger 트리레이어에서 컨셉(노드)을 선택했을 때
       */
      function _setValue(node) {
        // 모델에 (대표)용어 반영
        vm.model.name = node.term;// + ' (' + node.semanticTag + ')';
        // 모델에 컨셉아이디 반영
        vm.model.id = node.conceptId;
        // 트리레이어 모드 비활성화
        _setLayerMode(false);
      }


      /**
       * 키보드 키를 놓았을 때 발생하는 이벤트를 처리하는 메소드
       * @name _onKeyup($event)
       * @param {object} $event
       */
      function _onKeyup($event) {
        // 편집 모드 비활성화상태이거나 트리레이어가 활성화상태라면 반환
        if (!_isEditMode() || _isLayerMode()) return;
        // 이벤트로부터 키코드 가져오기
        var keycode = $event.keyCode;
        // 13: Enter
        if (keycode == 13) {
          // 콜백 실행
          vm.cbComplete();
          // 모든 이벤트 해제
          _onBlur();
        }
      }
    }
  }

  angular
    .module('combolist')
    .directive('combolist', ComboListDirective);
})();
