/*
 * InfoClinic.Co.,Ltd.
 * refset-editor.js
 * IIFE scoping: To avoid polluting the global scope with our function declarations that get passed into Angular, ensure build tasks wrap the concatenated files inside an IIFE
 * All the code is wrapped in the IIFE
 * Reference: http://www.codestyle.co/Guidelines/angularjs
 * Reference: https://github.com/toddmotto/angular-styleguide
 */
(function () {

  'use strict';

  angular
    .module('ic.refset.editor', []);

  // Ctrl.js
  function RefsetEditorController ($scope, $rootScope, $q) {

    var self = this;

    //// ---------- PUBLIC ----------

    //// PUBLIC Fields
    $scope.list = {};

    //// PUBLIC Methods
    $scope.beforeAppend = _beforeAppend;


    //// ---------------- PRIVATE ----------------

    //// PRIVATE fields

    //// PRIVATE Functions - Public Methods Implementation

    function _beforeAppend() {
      var deferred = $q.defer();
      if (confirm('Are you sure??')) {
        deferred.resolve();
      } else {
        deferred.reject();
      }

      return deferred.promise;
    }

    function _onDrop() {

    }



  }

  angular
    .module('ic.refset.editor')
    .controller('RefsetEditorController', RefsetEditorController);



  // ic.common-modal.js
  function RefsetEditorDirective () {
    return {
      // 'A' - only matches attribute name
      // 'E' - only matches element name
      // 'C' - only matches clsss name
      restrict: 'EA',
      //transclude: true,
      template: '<div>' +
/*                '<table class="{{ tableClass }}" style="width:100%">' +
                ' <thead>' +
                '   <th ng-repeat="field in fields">{{ field | uppercase }}</th>' +
                ' </thead>' +
                ' <tbody>' +
                '   <tr></tr>' +
                ' </tbody>' +
                ' <tfoot>' +
                '   <tr>' +
                '     <td class="text-center" colspan="{{ fields.length }}" ng-model="list" data-drop="true" jqyoui-droppable="{multiple:false, beforeDrop:\'beforeAppend\'}" ng-bind="list.terminologyId ? \'drop success\':\'dddd\'"> ' +
                '       <span class="text-muted"> Drop a new member here</span>' +
                '     </td>' +
                '   </tr>' +
                ' </tfoot>' +
                '</table>' +
*/
                  '<div>' +
                    '<label>멤버 아이디</label>' +
                    '<input type="text"/>' +
                    '<label>용어</label>' +
                    '<input type="text"/>' +
                    '<button type="button" class="btn btn-default">가져오기</button>' +
                   '</div>' +
                  '<div class=""><button type="button" class="btn btn-primary">저장</button></div>' +
                '</div>'
      ,
      scope: {
        // & - method binding
        // @ - one way binding
        // = - two way binding
        componentType: '@',
        tableClass: '@'
      },
      controller: 'RefsetEditorController',
      controllerAs: 'reCtrl',
      link: function (scope, element, attrs) {
        scope.componentType = attrs.componentType;
        scope.tableClass = attrs.tableClass;
        scope.fields = [
          scope.componentType + 'id',
          'term',
          'active'
        ];
      }
    };
  }

  angular
    .module('ic.refset.editor')
    .directive('refsetEditor', RefsetEditorDirective);
})();
