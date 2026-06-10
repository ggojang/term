/*
 * InfoClinic.Co.,Ltd.
 * common-modal.js
 * IIFE scoping: To avoid polluting the global scope with our function declarations that get passed into Angular, ensure build tasks wrap the concatenated files inside an IIFE
 * All the code is wrapped in the IIFE
 * Reference: http://www.codestyle.co/Guidelines/angularjs
 * Reference: https://github.com/toddmotto/angular-styleguide
 */
(function () {

  'use strict';

  angular
    .module('ic.modal', []);

  // Ctrl.js
  function IcModalController ($scope, $rootScope, $uibModalInstance, modalSetting, modalScope) {

    var self = this;

    //// ---------- PUBLIC ----------

    //// PUBLIC Fields

    // modal setting
    self.setting = modalSetting;

    // modal scope
    self.scope = modalScope;

    //// PUBLIC Methods

    self.accept = _accept;

    self.cancel = _cancel;

    //// ---------------- PRIVATE ----------------

    //// PRIVATE fields

    //// PRIVATE Functions - Public Methods Implementation

    // callback trigger : _accept
    function _accept(e) {
      $uibModalInstance.close();
      $rootScope.$emit('CommonModalAccepted', e);
      if (e) e.stopPropagation();
    };

    // callback trigger : _cancel
    function _cancel(e) {
      $uibModalInstance.dismiss('cancel');
      $rootScope.$emit('CommonModalCanceled', e);
      if (e) e.stopPropagation();
    };


    //// ---------------- Event ----------------

    /*
    $rootScope.$on('CommonModalAccepted', function() {
      console.log('event accept');
      //_accept();
    });

    $rootScope.$on('CommonModalCanceled', function() {
      //_cancel();
      console.log('event cancel');
    });
    */
  }

  angular
    .module('ic.modal')
    .controller('IcModalController', IcModalController);



  // ic.common-modal.js
  function IcCommonModalDirective ($uibModal) {
    return {
      // 'A' - only matches attribute name
      // 'E' - only matches element name
      // 'C' - only matches clsss name
      restrict: 'EA',
      transclude: true,
      template: '<a ng-click="open()" ng-transclude></a>',
      scope: {
        // & - method binding
        // @ - one way binding
        // = - two way binding
        name: '@',
        size: '@',
        scope: '=scope',
        bodyTemplate: '@',
        bodyClass: '@'
      },
      link: function (scope, element, attrs) {
        scope.open = function() {
          var modalInstance = $uibModal.open({
            templateUrl: attrs.template ? attrs.template : false,
            template: !attrs.template ? function() {
              var html = '';
              html += '<div class="modal-header">';
              html += '<h3 class="modal-title">' + attrs.name && attrs.name != '' ? attrs.name : 'Angular My Modal' + '</h3>';
              html += '</div>';
              html += '<div class="modal-body ' + attrs.bodyClass + '" ng-include="' + attrs.bodyTemplate + '">';
              //html += attrs.body;
              html += '</div>';
              html += '<div class="modal-footer">';
              html += '<button class="btn btn-primary" ng-click="IcModalCtrl.accept($event)">OK</button>';
              html += '<button class="btn btn-warning" ng-click="IcModalCtrl.cancel($event)">Cancel</button>';
              html += '</div>';
              return html;
            } : false,
            controller: 'IcModalController',
            controllerAs: 'IcModalCtrl',
            size: attrs.size ? attrs.size : 'sm', // lg-sm-md
            windowClass: attrs.windowClass ? attrs.windowClass : 'angular-common-modal-window',
            backdrop: attrs.backdrop ? attrs.backdrop : true,
            resolve: {
              modalSetting: function() {
                return {
                  name: scope.name ? scope.name : 'Angular my modal'
                };
              },
              modalScope: function() {
                return scope.scope ? scope.scope : {};
              }
            }
          });

          modalInstance.result.then(function() {
             //console.debug('success');
          }, function() {
             //console.debug('error');
          });
        }
      }
    };
  }

  angular
    .module('ic.modal')
    .directive('icCommonModal', IcCommonModalDirective);
})();
