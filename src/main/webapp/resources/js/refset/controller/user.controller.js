(function () {
	
	'use strict';
	
	angular
		.module('app.referenceset')
		.controller('UserRefsetController', UserRefsetController);
	
	UserRefsetController.$inject = ['$scope', '$state', '$element', 'refsetservice', 'utilservice', 'rsdataservice'];
	function UserRefsetController($scope, $state, $element, refsetservice, utilservice, rsdataservice) {
		
		var vm = this;
		
		//vm.referencseMemberObjectMaker = _referencseMemberObjectMaker;
		//vm.descriptorExtraObjectMaker = _descriptorExtraObjectMaker;
		//vm.simpleTypeDescriptorMaker = _simpleTypeDescriptorMaker;
		//vm.orderedTypeDescriptorMaker = _orderedTypeDescriptorMaker;
		//vm.addReferencesetMemberList = addRefset;
		$scope.selectNode = _selectNode;
		$scope.changeNode = changeNode;
		
		$scope.types = {
			'file': {
	        'icon': 'glyphicon glyphicon-file'
			}
		};
		
		$scope.contextMenu = {
			'createfolder': {
				'label': 'Create Folder',
				'_disabled': function (data) {
					var isDisabled = false;
					var inst = $.jstree.reference(data.reference),
							obj = inst.get_node(data.reference);

					if (obj.type == 'file') isDisabled = true;

					return isDisabled;
				},
				'action': function(data) {
					var inst = $.jstree.reference(data.reference),
					obj = inst.get_node(data.reference);
					inst.create_node(obj, {}, 'last', function (new_node) {
						setTimeout(function () { inst.edit(new_node); },0);
						});
					}
				},
			'createreferenceset': {
				'label': 'Create Referenceset',
				'_disabled': function (data) {
					var isDisabled = false;
					var inst = $.jstree.reference(data.reference),
							obj = inst.get_node(data.reference);

					if (obj.type == 'file') isDisabled = true;

					return isDisabled;
				},
				'action': false,
				'submenu': {
					'simple': {
						'label': 'Simple Type',
						'_disabled': function (data) {
							var isDisabled = false;
							var inst = $.jstree.reference(data.reference),
									obj = inst.get_node(data.reference);

							if (obj.type == 'file') isDisabled = true;

							return isDisabled;
						},
						'action': function(data) {
							// call descriptor
							var inst = $.jstree.reference(data.reference),
                  obj = inst.get_node(data.reference);
		          inst.create_node(
								obj,
								{
									'type': 'file'
								},
								'last',
								function (new_node) {
		              setTimeout(function () {
										new_node.text = 'untitled simple type reference set';
										inst.edit(new_node);
										var id = (Math.floor(Math.random() * (99999999999 - 99000000000 + 1)) + 99000000000) + '004';
										new_node.data = {
											'refsetId': id
										}
										var memberList = _simpleTypeDescriptorMaker(id,'247771000300108', true);
										// create descriptor to 900000000000456007
										addRefset(memberList);
										// TODO Create Descriptor By Id Like SimpleType
										// createDescriptorByIdLikeSimpleType
									},0);
		          	}
							);
						}
					},
					'ordered': {
						'label': 'Ordred Type',
						'_disabled': function (data) {
							var isDisabled = false;
							var inst = $.jstree.reference(data.reference),
									obj = inst.get_node(data.reference);

							if (obj.type == 'file') isDisabled = true;

							return isDisabled;
						},
						'action': function(data) {
							var inst = $.jstree.reference(data.reference),
                  obj = inst.get_node(data.reference);
		          inst.create_node(
								obj,
								{
									'type': 'file'
								},
								'last',
								function (new_node) {
		              setTimeout(function () {
										new_node.text = 'untitled ordered type reference set';
										inst.edit(new_node);
										var id = (Math.floor(Math.random() * (99999999999 - 99000000000 + 1)) + 99000000000) + '004';
										new_node.data = {
											'refsetId': id
										}
										var memberList = _orderedTypeDescriptorMaker(id,'247771000300108', true);
										// create descriptor to 900000000000456007
										addRefset(memberList);
										// TODO Create Descriptor By Id Like SimpleType
										// createDescriptorByIdLikeSimpleType
									},0);
		          	}
							);
						}
					},
					'map': {
						'label': 'Map Type',
						'_disabled': function (data) {
							var isDisabled = false;
							var inst = $.jstree.reference(data.reference),
									obj = inst.get_node(data.reference);

							if (obj.type == 'file') isDisabled = true;

							return isDisabled;
						},
						'action': false,
						'submenu': {
							'simpleMap': {
								'label': 'Simple Map Type',
								'_disabled': function (data) {
									var isDisabled = false;
									var inst = $.jstree.reference(data.reference),
											obj = inst.get_node(data.reference);

									if (obj.type == 'file') isDisabled = true;

									return isDisabled;
								},
								'action': function(data) {
									var inst = $.jstree.reference(data.reference),
		                  obj = inst.get_node(data.reference);
				          inst.create_node(
										obj,
										{
											'data': {
												'refsetId': (Math.floor(Math.random() * (99999999999 - 99000000000 + 1)) + 99000000000) + '004'
											},
											'type': 'file'
										},
										'last',
										function (new_node) {
				              setTimeout(function () {
												new_node.text = 'untitled simple map type reference set';
												inst.edit(new_node);
												// TODO Get Simple Map Type Descriptor
												// TODO Create Descriptor
											},0);
				          	}
									);
								}
							},
							'complexMap': {
								'label': 'Complex Map Type',
								'_disabled': function (data) {
									var isDisabled = false;
									var inst = $.jstree.reference(data.reference),
											obj = inst.get_node(data.reference);

									if (obj.type == 'file') isDisabled = true;

									return isDisabled;
								},
								'action': function(data) {
									var inst = $.jstree.reference(data.reference),
		                  obj = inst.get_node(data.reference);
				          inst.create_node(
										obj,
										{
											'data': {
												'refsetId': (Math.floor(Math.random() * (99999999999 - 99000000000 + 1)) + 99000000000) + '004'
											},
											'type': 'file'
										},
										'last',
										function (new_node) {
				              setTimeout(function () {
												new_node.text = 'untitled complex map type reference set';
												inst.edit(new_node);
												// TODO Get Complex Map Type Descriptor
												// TODO Create Descriptor
											},0);
				          	}
									);
								}
							},
							'extendedMap': {
								'label': 'Extended Map Type',
								'_disabled': function (data) {
									var isDisabled = false;
									var inst = $.jstree.reference(data.reference),
											obj = inst.get_node(data.reference);

									if (obj.type == 'file') isDisabled = true;

									return isDisabled;
								},
								'action': function(data) {
									var inst = $.jstree.reference(data.reference),
		                  obj = inst.get_node(data.reference);
				          inst.create_node(
										obj,
										{
											'data': {
												'refsetId': (Math.floor(Math.random() * (99999999999 - 99000000000 + 1)) + 99000000000) + '004'
											},
											'type': 'file'
										},
										'last',
										function (new_node) {
				              setTimeout(function () {
												new_node.text = 'untitled extended map type reference set';
												inst.edit(new_node);
												// TODO Get Extended Map Type Descriptor
												// TODO Create Descriptor
											},0);
				          	}
									);
								}
							}
						}
					},
					'userdefined': {
						'separator_before': true,
						'label': 'User Defined Type',
						'_disabled': function (data) {
							var isDisabled = false;
							var inst = $.jstree.reference(data.reference),
									obj = inst.get_node(data.reference);

							if (obj.type == 'file') isDisabled = true;

							return isDisabled;
						},
						'action': function(data) {
							var inst = $.jstree.reference(data.reference),
		                  obj = inst.get_node(data.reference);
		          inst.create_node(obj, {'data':{'refsetId': (Math.floor(Math.random() * (99999999999 - 99000000000 + 1)) + 99000000000) + '004'},'type':'file'}, 'last', function (new_node) {
		                  setTimeout(function () { inst.edit(new_node); },0);
		          });
						}
					}
				}
		      },
		      'rename': {
						'separator_before': true,
		        'label': 'Rename',
		        'action': function(data) {
							var inst = $.jstree.reference(data.reference),
		            	obj = inst.get_node(data.reference);
		          inst.edit(obj);
		        }
		      },
		      'delete': {
		        'label': 'Delete',
		        'action': function(data) {
							var inst = $.jstree.reference(data.reference),
									obj = inst.get_node(data.reference);
							if(inst.is_selected(obj)) {
								inst.delete_node(inst.get_selected());
							}
							else {
								inst.delete_node(obj);
							}
		        }
		      }
		    };

			$scope.stuff = [];
		
			activate();
			
			function activate() {
				
				return getUserTree().then(function () {});
			};
			
			function getUserTree() {
				return rsdataservice.getUserTree()
					.then(function (usrs) {
						$scope.stuff = usrs;
						return $scope.stuff;
					});
			};
			
	/////////// Object Maker 관련 메소드

			function _referencseMemberObjectMaker(referencesetId, referencedComponentId, moduleId, active) {
				return {
					refsetId: referencesetId,
					referencedComponentId: referencedComponentId,
					moduleId: moduleId,
					active: active
				}
			};

			function _descriptorExtraObjectMaker(attributeDescriptionId, attributeTypeId, attributeOrder) {
				return {
					'Attribute description': attributeDescriptionId,
					'Attribute type': attributeTypeId,
					'Attribute order': attributeOrder
				}
			};

			/**
			 * Simple type referenceset의 Descriptor(필드)정보 리스트를 만들어 반환하는 메소드
			 * @name _simpleTypeDescriptorMaker(referencedComponentId, moduleId, active)
			 * @param {String} referencedComponentId
			 * @param {String} moduleId
			 * @param {Boolean} active
			 * @return {Array} ReferencesetMemberDTO
			 */
			function _simpleTypeDescriptorMaker(referencedComponentId, moduleId, active) {
				var descriptorList = [];
				// TODO field$에 필요한 값들은 parameter혹은 서비스로부터 받아오도록 변경할 것
				var descriptorId = '900000000000456007';
				var attrConceptType = '900000000000461009'; // Concept type component

				var descriptor = _referencseMemberObjectMaker(descriptorId, referencedComponentId, moduleId, active);
				var extra = _descriptorExtraObjectMaker(attrConceptType, attrConceptType, '0');
				/*
				{
					field1: attrConceptType,	// 900000000000461009|Concept type component
					field2: attrConceptType,	// 900000000000461009|Concept type component
					field3: '0'	// order 0
				};
				*/
				// descriptor에 extra 부여
				descriptor.extra = extra;
				descriptorList.push(descriptor);

				return descriptorList;
			};

			/**
			 * Ordered type referenceset의 Descriptor(필드)정보 리스트를 만들어 반환하는 메소드
			 * @name _orderedTypeDescriptorMaker(referencedComponentId, moduleId, active)
			 * @param {String} referencedComponentId
			 * @param {String} moduleId
			 * @param {Boolean} active
			 * @return {Array} ReferencesetMemberDTO
			 */
			function _orderedTypeDescriptorMaker(referencedComponentId, moduleId, active) {
				var descriptorList = [];
				// TODO field$에 필요한 값들은 parameter혹은 서비스로부터 받아오도록 변경할 것
				var descriptorId = '900000000000456007';
				var attrReferencedComponent = '449608002'; // Referenced component
				var attrConceptType = '900000000000461009'; // Concept type component
				var attrAttributeOrder = '900000000000479008'; // Attribute order
				var attrUnsignedInteger = '900000000000478000'; // Unsigned integer
				var attrLinkedTo = '447257003'; // "Linked to" reference set attribute

				//// Field 0 {Concept} Referenced Component
				var base = _referencseMemberObjectMaker(descriptorId, referencedComponentId, moduleId, active);
				var extraForReferencedComponent = _descriptorExtraObjectMaker(attrReferencedComponent, attrConceptType, '0');
				/*
				extra = {
					field1: attrReferencedComponent,	// 449608002|Referenced component
					field2: attrConceptType,	// 900000000000461009|Concept type component
					field3: '0'	// order 0
				};
				*/
				// descriptor에 extra 부여
				var descriptorReferencedComponent = angular.copy(base);
				descriptorReferencedComponent.extra = extraForReferencedComponent;
				descriptorList.push(descriptorReferencedComponent);

				//// Field 1 {Unsigned Integer} Attribute order
				var descriptorOrder = angular.copy(base);
				var extraForOrder = _descriptorExtraObjectMaker(attrAttributeOrder, attrUnsignedInteger, '1');
				/*
				extra = {
					field1: attrAttributeOrder,	// 447257003|Attribute order
					field2: attrUnsignedInteger,	// 900000000000478000|Unsigned integer
					field3: '1'	// order 1
				};
				*/
				// descriptor에 extra 부여
				var descriptorAttributeOrder = angular.copy(base);
				descriptorAttributeOrder.extra = extraForOrder;
				descriptorList.push(descriptorAttributeOrder);

				// Field 2 {Concept} LinkedToId
				var descriptorLinkedTo = angular.copy(base);
				var extraForLinkedTo = _descriptorExtraObjectMaker(attrLinkedTo, attrConceptType, '2');
				/*
				extra = {
					field1: attrLinkedTo,	// 447257003|"Linked to" reference set attribute
					field2: attrConceptType,	// 900000000000461009|Concept type component
					field3: '2' // order 2
				};
				*/
				// descriptor에 extra 부여
				var descriptorLinkedTo = angular.copy(base);
				descriptorLinkedTo.extra = extraForLinkedTo;
				descriptorList.push(descriptorLinkedTo);

				return descriptorList;
			};
			
			/**
			 * @name saveUserTree
			 * @desc 사용자 레퍼런스세트 구조를 저장
			 */
			function saveUserTree(tree) {
				rsdataservice.saveUserTree(tree)
				.then(function (response) {
				});
			};
			
			/**
			 * @name addRefset
			 * @desc 새로운 레퍼런스세트를 추가
			 */
			function addRefset(r) {
				/*
				 [{"refsetId":"900000000000456007","referencedComponentId":"99385700114004","moduleId":"247771000300108","active":true,"extra":{"Attribute description":"900000000000461009","Attribute type":"900000000000461009","Attribute order":"0"}}]
				 */
				if (isValidRefset(r)) {
					rsdataservice.addRefset(r)
					.then(function () {});
				 }
				// FIXME: 새로운 레퍼런스세트가 추가되었으므로 트리정보 업데이트
			};
			
			/**
			 * @name removeRefset
			 * @desc 레퍼런스세트 제거(비활성화)
			 */
			function removeRefset(refsetId) {
				// FIXME: Descriptor와 트리정보만 제거하면되므로 우선 보류 - 0. Refset Member 제거(비활성화)
				// 1. Refset Descriptor 비활성화
				// 2. Refset으로 등록된 Concept 제거(비활성화)
				
				// FIXME: 기존 레퍼런스세트가 제거(비활성화)되었으므로 트리정보 업데이트
			};
			
			/**
			 * @name isValidRefset
			 * @desc 새로 추가하려는 레퍼런스세트 필드정보가 유효한지 확인
			 */
			function isValidRefset(r) {
				// FIXME: impl Required Property Check
				var valid = false;
				if (r.length > 0) valid = true;
				return valid;
			};
			
			/*
			 * 레퍼런스세트 에디터 > 인포클리닉 탭에서 트리 선택 이벤트를 처리하는 메소드
			 */
			function _selectNode(e, data) {
				var node = data.node,
					nodeData = node.data,
			 		refsetId = '',
			 		refsetNm = '';

				// nodeData = { refsetId: '레퍼런스세트 아이디' }
				if (node.type === 'file' && nodeData) {
					refsetId = nodeData.hasOwnProperty('refsetId') ? nodeData.refsetId:'';
					refsetNm = node.text;
				}
				
				//$scope.$emit('REFSET: CHNG', {refsetId: refsetId, refsetNm: refsetNm});
				//refsetservice.getUserMemberList(refsetId, refsetNm);
				$state.go('refset.view', {release:'user', code: refsetId, 'name': refsetNm});
			};
			
			/**
			 * @name changeNode
			 * @desc 사용자 레퍼런스세트 트리 변경사항 이벤트 제어
			 */
			function changeNode(e, data) {
				var tree = angular.element('#usrtree').jstree(true).get_json();
				saveUserTree(tree);
				
			};
	};
	
})();
