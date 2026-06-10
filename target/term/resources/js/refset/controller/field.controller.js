(function () {
	
	'use strict';
	
	angular
		.module('app.referenceset')
		.controller('FieldController', FieldController);
	
	FieldController.$inject = [
	                           '$scope',
	                           '$element',
	                           '$compile',
	                           '$filter',
	                           '$window',
	                           '$timeout',
	                           'apiservice', 
	                           'utilservice', 
	                           'refsetservice',
	                           'userservice',
	];
	function FieldController(
			$scope, 
			$element,
			$compile,
			$filter,
			$window,
			$timeout,
			apiservice, 
			utilservice, 
			refsetservice,
			userservice) {
		
		var vm = this;
		
		//////////PUBLIC

		//// PUBLIC Methods
		vm.termBeautifier = utilservice.termBeautifier;
		vm.orderByAttributeOrder = _orderByAttributeOrder;

		vm.save = _save;
		vm.backwardField = _backwardFieldList;

		//
		vm.onMouseDown = _onMouseDown;
		//
		vm.onMouseUp = _onMouseUp;
		//
		vm.onMouseMove = _onMouseMove;
		//
		vm.cbComplete = _cbComplete;
		// 선택한 필드를 지우는 메소드
		vm.deleteField = _deleteField;

		//// PUBLIC Variables
		vm.fields = [];
		vm.histories = [];
		vm.context = {
			referencesetId: null,
			moduleId: null,
			draggable: {
				index: -1,
				data: null
			}
		};
		vm.newField = {
			description: {
				id: null,
				name: null
			},
			type: {
				id: null,
				name: null
			}
		};
		// 드래그하기위해 대상을 선택했는지에 대한 여부
		vm.isDragging = false;
		// 드래그 상태에서 이동했는지에 대한 여부
		vm.isMoving = false;
		// 드래그 상태에서 이동시 보여지는 핸들이 붙여져있는지에 대한 여부
		vm.isHandleAttach = false;
		// 선택한 행 수
		vm.selectedCount = 0;
		// 상수
		vm.constants = {
			attributeDescription: 'Attribute description',
			attributeOrder: 'Attribute order',
			attributeType: 'Attribute type'
		};

		_activate();

		////////// PRIVATE

		//// PRIAVTE Methods
		function _activate() {
			vm.fields = refsetservice.fields;
			vm.refsetId = refsetservice.refsetId;
			
			// 행 선택후 지우고자 할 떄, 키보드 이벤트를 받기위해 이벤트 등록
			angular.element('body').on('keyup', _onKeyUp);

			
			var html = '<!-- SAVE BUTTON -->' +
					'      <div id="refset-fieldlist-tool" style="height:34px;line-height:30px;text-align:right;" ng-if="fieldVm.fields[0].module.id == \'247771000300108\'">' +
					'        <button' +
					'          class="btn btn-default btn-xs glyphicon glyphicon-floppy-save"' +
					'          ng-disabled="fieldVm.histories.length <= 1 ? true:false"' +
					'          ng-style="fieldVm.histories.length <= 1 ? {\'opacity\':0.5}:{}"' +
					'          ng-click="fieldVm.save()">' +
					'        </button>' +
					'        <!-- BACKWARD BUTTON -->' +
					'        <button' +
					'          class="btn btn-default btn-xs glyphicon glyphicon-arrow-left"' +
					'          ng-disabled="fieldVm.histories.length <= 1 ? true:false"' +
					'          ng-style="fieldVm.histories.length <= 1 ? {\'opacity\':0.5}:{}"' +
					'          ng-click="fieldVm.backwardField()">' +
					'        </button>' +
					'        <!-- DELETE BUTTON -->' +
					'        <button' +
					'          class="btn btn-default btn-xs glyphicon glyphicon-trash"' +
					'          ng-disabled="fieldVm.selectedCount == 0 ? true:false"' +
					'          ng-style="fieldVm.selectedCount == 0 ? {\'opacity\':0.5}:{}""' +
					'          ng-click="fieldVm.deleteField()">' +
					'        </button>' +
					'      </div>';
			var el = $compile(html)($scope);
			$element.closest('md-tabs').find('md-tabs-wrapper > md-tabs-canvas').append(el);
		}


		/**
		 * 필드의 Attribute order를 10진수로 변환 후 반환하는 메소드
		 * @name _orderByAttributeOrder(field)
		 * @param {string} field
		 * @return {integer} order
		 */
		function _orderByAttributeOrder(field) {
			return parseInt(field.extra[vm.constants.attributeOrder].id, 10);
		};

		/**
		 * 변경사항 확인 후 서버에 반영하는 메소드
		 * @name _save()
		 */
		function _save() {

			// 서버에 보낼 배열 A

			// 첫 번째 필드리스트 : vm.context.field.history[0]
			// 마지막 필드리스트 : vm.fields

			// 마지막 필드리스트 loop
			// case1. 새로 추가된 필드인지 확인
			//        - 필드의 extra[Attribute order].id가 -1이면 새로 추가한 필드
			//        - 맞다면 배열 A에 추가
			//        - if
			// case2. 기존 필드 변경 내역 확인
			//        - else
			//   case2.1. 필드의 extra[Attribute order].id와 loop의 index가 서로 다름을 확인
			//           - if
			//   case2.2. 필드의 extra[Attribute order].id값을 인덱스로 잡고
			//            첫 번째 필드리스트의 인덱스에서 extra[Attribute description].id가 변경되었는지 확인
			//           - if
			//        - 2.1 또는 2.2에 변경사항이 있다면 배열 A에 추가


					// 변경된 필드 데이터
			var data = {
						c: [],
						u: [],
						d: []
		    },
			// 첫 번째 필드리스트
			originalFieldList = angular.copy(vm.histories[0].fieldList),
			// 마지막 필드리스트
			latestFieldList = angular.copy(vm.fields),
			// 지워진 필드 리스트
			deletedList = [],
			// loop에서 사용하는 latest field 변수
			field = {},
			// loop에서 사용하는 temp field 변수
			tempField = {},
			// loop에서 사용하는 field의 order 변수
			order = -1,
			// 히스토리 배열길이
			fieldHistoryListLength = vm.histories.length,
			// 마지막 필드리스트의 배열길이
			latestFieldListLength = latestFieldList.length;
			for (var i = 0; i < latestFieldListLength; i++) {
				// 마지막 필드리스트의 i번째 값
				field = latestFieldList[i];
				order = parseInt(field.extra[vm.constants.attributeOrder].id, 10);
				// case1. 새로 추가되었거나 기존 필드지만 순서가 변경되었을 경우
				if (order === -1 || order !== i) {
					tempField = angular.copy(field);
					tempField.effectiveTime = undefined;

					tempField.extra[vm.constants.attributeOrder].id = i;
					if (order === -1) {
						data.c.push(_convertFieldEntity(tempField));
					} else {
						data.u.push(_convertFieldEntity(tempField));
					}
				}
				// case2. 기존 필드이면서 순서가 변경되지않은 경우
				else {
					var isChanged = false;
					// case2.1. Attribute Description이 변경된 경우
					//          필드의 extra[Attribute order].id값을 인덱스로 잡고
					//          첫 번째 필드리스트의 인덱스에서 extra[Attribute description].id가 변경되었는지 확인
					var originalDescriptionId = originalFieldList[order].extra[vm.constants.attributeDescription].id,
					    latestDescriptionId = field.extra[vm.constants.attributeDescription].id;
					isChanged = originalDescriptionId !== latestDescriptionId ? true:false;

					// case2.2. Attribute Type이 변경된 경우
					//          필드의 extra[Attribute order].id값을 인덱스로 잡고
					//          첫 번째 필드리스트의 인덱스에서 extra[Attribute type].id가 변경되었는지 확인
					var originalTypeId = originalFieldList[order].extra[vm.constants.attributeType].id,
					    latestTypeId = field.extra[vm.constants.attributeType].id;
					isChanged = originalTypeId !== latestTypeId ? true:false;

					if (isChanged) {
						// description 또는 type이 변경된 경우
						tempField = angular.copy(field);
						tempField.effectiveTime = undefined;
						data.u.push(_convertFieldEntity(tempField));
					}
				}
			}

			for (var i = 0; i < fieldHistoryListLength; i++) {
				deletedList = vm.histories[i].deletedList;
				if (deletedList.length > 0) {
					console.log('pre',data.d, deletedList);
					angular.forEach(deletedList, function(item, index) {
						data.d.push(_convertFieldEntity(item));
					});
					console.log('post',data.d, deletedList);

				}
			}


			//console.log('changed', data);
			// 추가할 데이터 : data.add
			// 수정할 데이터 : data.update
			// 삭제할 데이터 : data.delete
			console.log(data);
			//*/
			apiservice.saveDescriptorList(refsetservice.refsetId, userservice.effectiveTime, data)
			.then(function(response) {
				
				vm.histories = [];

				var fieldList = response;
				if (fieldList && fieldList.length > 0 && typeof fieldList[0].extra[vm.constants.attributeOrder] != undefined) {
					fieldList = $filter('orderBy')(response, _orderByAttributeOrder);
				}
				// 필드리스트에서 필드변경, 수정 작업 중 되돌리기 기능 적용을 위하여 보관
				vm.histories.push({
					fieldList: angular.copy(fieldList),
					deletedList: []
				});
				// 필드리스트에 보여질 view model
				vm.fields = fieldList;
				
			});
		}

		function _convertFieldEntity(field) {
			console.log('_convert',field);
			var entity = {
				id: field.id,
				uuid: field.uuid,
				active: field.active,
				effectiveTime: field.effectiveTime,
				moduleId: field.module.id,
				refsetId: field.refset.id,
				referencedComponentId: field.referencedComponent.id,
				extra: null
			};

			var extra = field.extra;
			if (extra) {
				var tempExtra = {}, e;//, extraLength = extra.length;
				angular.forEach(extra, function (e, key) {
					tempExtra[key] = e.id;
				});

				entity.extra = tempExtra;
			}

			return entity;
		}

		/**
		 * 이전에 변경했던 내역으로 되돌리는 메소드
		 * @name _backwardFieldList()
		 */
		function _backwardFieldList() {
			var historySize = vm.histories.length;
			if (historySize > 1) {
				var backwardField = vm.histories.splice(historySize - 1, 1);
				vm.fields = backwardField[0].fieldList;
			}
		}
		/**
		 * 테이블 행내에서 마우스를 눌렀을 때 발생하는 이벤트를 처리하는 이벤트
		 * @name _onMouseDown($event)
		 * @param {object} $event
		 */
		function _onMouseDown($event) {
			if (vm.isDragging) return;

			// 선택된 DOM에서 위로 가장 가까운 tr을 찾음
			var tr = angular.element($event.target).closest('tr');
			// index 0~4까지는 기본 필드
			var index = angular.element('#field-table > tbody').children().index(tr);

			if (index < 5) return;

			// Drag 시작을 알림
			vm.isDragging = true;

			// .bind보다 .on을 사용하는 이유
			// : .bind에 비해 .on으로 여러 종류의 이벤트 설정이 가능
			angular.element('body')
			.on({
				'mouseup': _onMouseUp,
				'mousemove': _onMouseMove
			});

			var indexInFieldList = index - 4;
			var source = angular.copy(vm.fields[indexInFieldList]);
			vm.context.draggable.index = indexInFieldList
			vm.context.draggable.data = source;

			// 드래그할 경우 텍스트가 선택되는 이벤트가 방지되도록 preventDefault 수행
			// 참고: http://solidlystated.com/scripting/proper-way-to-disable-text-selection-and-highlighting/
			//$event.preventDefault();
			//return false;
		}


		/**
		* 드래그 대상을 선택 후 이동했을 때 발생하는 이벤트를 처리하는 메소드
		* @name _onMouseMove($event)
		* @param $event $event jQuery Event Object의 인스턴스(기본 jqLite)
		*/
		function _onMouseMove($event) {
			if (!vm.isDragging) return;

			vm.isMoving = true;

			// Handle을 위한 작업
			// 1. 선택한 DOM의 X, Y좌표 구하기
			//    page: document기준, offset: Object기준, screen: 모니터기준, client: 브라우저기준(스크롤무시)
			// 2. DOM 복제 (이후 작업은 _onMouseMove에서)
			var pageY = $event.pageY;

			var fieldDescription = angular.copy(angular.element('#field-table > tbody').children().eq(vm.context.draggable.index + 4).children().eq(1).text());
			var dragHandlerStyle = 'position:absolute;'
			//+ 'top:' + angular.element('#field-table > tbody').children().eq(index).offset().top +'px;'
			+ 'top:' + (pageY + 3) +'px;'
			+ 'left:0;'
			+ 'width:' + angular.element('#field-table').css('width') + ';'
			+ 'height:32px;'
			+ 'background-color:lemonchiffon;'
			+ 'opacity:0.8;'
			+ 'text-align:center;'
			+ 'line-height:32px'
			var dragHandlerElement = angular.element('<div id="field-drag-handle" class="noselect" style="' + dragHandlerStyle + '"></div>');
			dragHandlerElement.append(fieldDescription);

			if (!vm.isHandleAttach) {
				angular.element('body').append(dragHandlerElement);
				vm.isHandleAttach = true;
			}

			angular.element('#field-drag-handle').css('top', ($event.pageY + 3) + 'px');

			var tbodyTop = angular.element('#field-table > tbody').offset().top;
			var tbodyRight = angular.element('#field-table > tbody').offset().left + angular.element('#field-table > tbody').width();
			var availableTop = angular.element('#field-table > tbody').children().eq(5).offset().top;

			if ($event.pageY < tbodyTop || $event.pageX > tbodyRight) {
				_initializeDrag();
			} else {
				if ($event.pageY < availableTop - 20) {
					angular.element('#field-drag-handle')
					.css('cursor', 'not-allowed')
					.css('background-color', 'red');
				} else {
					angular.element('#field-drag-handle')
					.css('cursor', 'move')
					.css('background-color', 'lemonchiffon');
				}
			}
		}


		/**
		* 드래그 대상을 선택 후 놓았을 때 발생하는 이벤트를 처리하는 메소드
		* @name _onMouseUp($event)
		* @param $event jQuery Event Object의 인스턴스(기본 jqLite)
		* @return true 텍스트 선택 방지 해제
		*/
		function _onMouseUp($event) {
			if (!vm.isDragging) return;

			if (vm.isMoving) {
				var changedBackgroundColor = 'lemonchiffon',
				draggable = vm.context.draggable;

				// 선택된 DOM에서 위로 가장 가까운 tr을 찾음
				var tr = angular.element($event.target).closest('tr');
				// index 0~4까지는 기본 필드
				var index = angular.element('#field-table > tbody').children().index(tr);
				var indexInFieldList = index - 4;

				if (draggable.index != indexInFieldList) {
					vm.histories.push({
						fieldList: angular.copy(vm.fields),
						deletedList: []
					});
					draggable.data.changed = true;
					draggable.data.selected = false;

					vm.fields.splice(draggable.index, 1);
					vm.fields.splice(indexInFieldList, 0, draggable.data);
				}
			} else {
				var selected = false;
				selected = vm.fields[vm.context.draggable.index].selected;
				vm.fields[vm.context.draggable.index].selected = !selected;

				if (selected) vm.selectedCount++;
				else vm.selectedCount--;
			}
			// 초기화
			_initializeDrag();
			vm.isMoving = false;
			// preventDefault 해제
			//return true;
		}


		/**
		 * Drag&Drop로 인해 등록된 이벤트 해제, 값 초기화
		 * @name _initializeDrag()
		 */
		function _initializeDrag() {
			if (!vm.isDragging) return;

			if (vm.isMoving) angular.element('#field-drag-handle').remove();

			// .on()함수에 의해 바인인 된 이벤트 핸들러를 제거
			angular.element('body').off({
				'mouseup': _onMouseUp,
				'mousemove': _onMouseMove
			});

			utilservice.safeApply($scope, function() {
				vm.context.draggable.index = -1;
				vm.context.draggable.data = null;

				// 현재 드래그 상태를 나타내는 변수를 초기화
				vm.isDragging = false;
				vm.isHandleAttach = false;
			});
		}

		/**
		 * 필드의 Description 또는 Type을 새로 추가 및 변경하는 메소드
		 * @name _cbComplete
		 */
		function _cbComplete() {
			if (vm.newField.description.id != null && vm.newField.type.id != null) {
				var cloneField = angular.copy(vm.newField);
				vm.histories.push({
					fieldList: angular.copy(vm.fields),
					deletedList: []
				});
				vm.fields.push({
					active: true,
					referencedComponent: {
						id: refsetservice.refsetId
					},
					refset: {
						id: '900000000000456007'
					},
					type: {
						id: '900000000000456007'
					},
					module: {
						id: '247771000300108' // TODO moduleId
					},
					extra: {
						'Attribute description': {
							id: vm.newField.description.id,
							name: vm.newField.description.name
						},
						'Attribute type': {
							id: vm.newField.type.id,
							name: vm.newField.type.name
						},
						'Attribute order': {
							id: '-1'
						}
					},
					selected: false,
					changed: true
				});

				vm.newField.description.id = null;
				vm.newField.description.name = null;
				vm.newField.type.id = null;
				vm.newField.type.name = null;
			} else {
				$timeout(function() {
					var id = vm.newField.type.id == null ? 'ft-new-type':'ft-new-description';
					angular.element('#' + id).children().eq(0).dblclick();
				});
			}
		};


		/**
		 * 키를 올렸을 때 발생하는 이벤트를 처리하는 메소드
		 * - 8번키(delete)를 눌렀을 때, _deleteField() 호출
		 * @name _onKeyUp($event)
		 * @param {object} $event
		 */
		function _onKeyUp($event) {
			// keycode: http://keycode.info/
			var keyCode = $event.keyCode;
			// 8 : backspace / delete
			if (keyCode == 8) {
				_deleteField();
			}
		}


		/**
		 * 선택한 필드를 지우는 메소드
		 * @name _deleteField()
		 */
		function _deleteField() {
			if (vm.isDragging || vm.isMoving || vm.selectedCount == 0) return;

			var selected = false;
			var deletedFieldList = [];
			var deletedField = {};

			vm.histories.push({
				fieldList: angular.copy(vm.fields),
				deletedList: deletedFieldList
			});

			var fieldListLength = vm.fields.length;
			for (var i = fieldListLength - 1; i >= 0; i--) {
				selected = vm.fields[i].selected;
				if (selected) {
					utilservice.safeApply($scope, function() {
						deletedField = vm.fields.splice(i, 1);
						deletedFieldList.push(deletedField[0]);
					});

				}
			}

			vm.selectedCount = 0;
		}
		
		/**
		 * 변수들을 초기화하는 메소드
		 */
		function _initVariables() {
			vm.fields = [];
			vm.histories = [];
			vm.moduleId = '';
			vm.newField = {
				description: {
					id: null,
					name: null
				},
				type: {
					id: null,
					name: null
				}
			};
			// 드래그하기위해 대상을 선택했는지에 대한 여부
			vm.isDragging = false;
			// 드래그 상태에서 이동했는지에 대한 여부
			vm.isMoving = false;
			// 드래그 상태에서 이동시 보여지는 핸들이 붙여져있는지에 대한 여부
			vm.isHandleAttach = false;
			// 선택한 행 수
			vm.selectedCount = 0;
		};

		/**
		 * referencset(id)의 Descriptor(field) 목록을 가져와서 반영하는 메소드
		 * @param {stirng} referencesetId
		 * @trigger $watch, _selectNode
		 */
		$scope.$watch(function() {
			return refsetservice.fields;
		}, function(fields) {
			// 변수 초기화
			_initVariables();
			
			vm.fields = fields;
			
			if (vm.fields && vm.fields.length > 0) {
				vm.moduleId = vm.fields[0].module.id;

				var tmpFields = [];
				if (angular.isDefined(vm.fields[0].extra) && typeof vm.fields[0].extra[vm.constants.attributeOrder] != undefined) {
					tmpFields = $filter('orderBy')(vm.fields, _orderByAttributeOrder);
				}
				// 필드리스트에서 필드변경, 수정 작업 중 되돌리기 기능 적용을 위하여 보관
				vm.histories.push({
					fieldList: angular.copy(vm.fields),
					deletedList: []
				});
				// 필드리스트에 보여질 view model
				vm.fields.length = 0;
				angular.merge(vm.fields, tmpFields);
				console.log('f', vm.fields);
			}
		});
	};
	
})();