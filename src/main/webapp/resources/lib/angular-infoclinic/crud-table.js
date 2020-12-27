/*
 * InfoClinic.Co.,Ltd.
 * Crud-Table.js
 * IIFE scoping: To avoid polluting the global scope with our function declarations that get passed into Angular, ensure build tasks wrap the concatenated files inside an IIFE
 * All the code is wrapped in the IIFE
 * Reference: http://www.codestyle.co/Guidelines/angularjs
 * Reference: https://github.com/toddmotto/angular-styleguide
 */
(function () {

  'use strict';

  angular
    .module('ic-crudtable', []);

  // Ctrl.js
  function MainCtrl () {

    var self = this;

    //// ---------- PUBLIC ----------

    //// PUBLIC Fields

    // Columns - grid should display
    self.columnsDefinition = [];

    // All items
    self.allItems = [];

    // The item being added
    self.newItem = {};

    // Indicates if the view is being loaded
    self.loading = false;

    // Indicates if the view is in add mode
    self.addMode = false;

/*
    //// PUBLIC Methods

    // Initialize module
    self.initialize = _initialize;

    // Toggle the grid between add and normal mode
    self.toggleAddMode = _toggleAddMode;

    // Toggle an item between normal and edit mode
    self.toggleEditMode = _toggleEditMode;

    // Creates the 'newItem' on the server
    self.createItem = _createItem;

    // Gets an item from the server using the id
    self.readItem = _readItem;

    // Updates an item
    self.updateItem = _updateItem;

    // Get all items from the server
    self.getAllItems = _getAllItems;

    // In edit mode, if user press ENTER, update item
    self.updateModeKeyUp = _updateModeKeyUp;

    // In add mode, if user press ENTER, add item
    self.createModeKeyUp = _createModeKeyUp;

    // Adds the item to the collection (no server communication)
    self.itemDeleted = _itemDeleted;

    // Removes an item from the collection (no server communication)
    self.itemCreated = _itemCreated;

    //// ---------------- CODE TO RUN ------------

    self.initialize();

    //// ---------------- PRIVATE ----------------

    //// PRIVATE fields

    //// PRIVATE Functions - Public Methods Implementation

    function _initialize() {
        // create a service to do the communication with the server
        _itemsService = ajaxServiceFactory.getService($scope.serverUrl);

        // configured columns
        self.columnsDefinition = angular.fromJson($attrs.columnsDefinition);

        // Initialize
        self.getAllItems();
    }
    */
  }

  angular
    .module('ic-crudtable')
    .controller('CrudTableController', MainCtrl);

  // crud.table.service.js
  function CrudService () {

    //// PUBLIC METHODS - Definition

    this.getService = _getService;

    //// PUBLIC METHODS - Implementation

    function _getService(endPoint) {
      if (endPoint === '') {
        throw 'Invalid end point';
      }

      // TODO
      return '';
    }
  }

  angular
    .module('ic-crudtable')
    .service('crudService', CrudService);


  // crud.table.directive.js
  function CrudTableDirective () {
    return {
      // 'A' - only matches attribute name
      // 'E' - only matches element name
      // 'C' - only matches clsss name
      restrict: 'EA',
      // Don't replace the element that contains the attribute
      replace: false,
      bindToController: {
        // & - method binding
        // @ - one way binding
        // = - two way binding
        serverUrl: "@serverUrl" // one way binding
      },
      controller: 'CrudTableController',
      controllerAs: 'ic',
      template:
        '<div>' +
        '  <div style="padding-bottom:10px" class="text-right">' +
        '    TEST' +
        '  </div>' +
        '</div>',
      link: function (scope, element, attrs) {
        //element.on('dragend', function () {});
      }
    };
  }

  angular
    .module('ic-crudtable')
    .directive('icCrudTable', CrudTableDirective);
})();
