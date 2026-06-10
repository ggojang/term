(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.controller('BrowserDescriptionController', BrowserDescriptionController);
	
	BrowserDescriptionController.$inject = ['$scope', '$mdDialog', 'utilservice', 'apiservice', 'browserservice', 'browser.description', 'browser.langrefset', 'browser.semantictag'];
	function BrowserDescriptionController($scope, $mdDialog, utilservice, apiservice, browserservice, DESCRIPTION, LANGREFSET, SEMANTICTAG) {
		var vm = this;
		
		/* ---------------------------------------- */
		/* Variables */
		/* ---------------------------------------- */
		
		vm.ui = {
			editorEnabled: false,
			base: {
				idx: 0,
				dataIdxs: []
			},
			other: {
				idx: null,
				dataIdxs: []
			}	
		},
		vm.data = [],
		vm.newItem = {},
		vm.loading = false,
		vm.lang = {
		  base: ['us', 'gb'],
		  extra: []
		},
		vm.options = DESCRIPTION;
		
		
		/* ---------------------------------------- */
		/* Methods */
		/* ---------------------------------------- */
		
		vm.initFields = _initFields,
		
		vm.getConceptType = utilservice.getConceptType,
		vm.getConceptTypeSimple = utilservice.getConceptTypeSimple,
		vm.getDescriptionPriority = utilservice.getDescriptionPriority,
		
		vm.getAvailableLanguageList = _getAvailableLanguageList,
		vm.isExistDescription = _isExistDescription,
		vm.changeTypeIcon = _changeTypeIcon,
		vm.getTypeId = _getTypeId,
		
		vm.setEnabled = _setEnabled,
		//vm.save = _save,
		vm.getAcceptableIcon = _getAcceptableIcon,
		vm.getAcceptableName = _getAcceptableName,
		vm.setBaseLangIndex = _setBaseLangIndex,
		vm.setOtherLangIndex = _setOtherLangIndex,
		vm.getLangName = _getLangName,
		vm.toggleEditMode = _toggleEditMode,
		//vm.copyDescription = _copyDescription,
		vm.groupByLanguage = _groupByLanguage,
		
		
		vm.createItem = _createItem,
		vm.updateItem = _updateItem,
		vm.deleteItem = _deleteItem,
		
		vm.addLanguage = _addLanguage,
		vm.createLanguageReferenceset = _createLanguageReferenceset;
		
		vm.isObjUndef = isObjUndef;

		_activate();
		
		function _activate() {
			_initFields(vm.newItem, []);
		};
		
		/**
		 * @name isObjUndef
		 * @desc Object가 Undefined인지 확인
		 * @param {Object} obj
		 */
		function isObjUndef(obj) {
			return angular.isUndefined(obj); 
		};
		
		function _createItem(item, source) {
			
			var cnptId = browserservice.context.concept.conceptId;
			
			if (_isValid(item)) {
				// language refset을 추가하려는 경우
				if (typeof item.descriptionId !== 'undefined') {
					var valid = true;
					if (source.descriptions.length > 0) {
						angular.forEach(source.descriptions, function(description, didx) {
							if (item.typeId == vm.options.type.fsn) {
								if (description.active && (description.typeId == vm.options.type.fsn)) {
									valid = false;
									alert('The fully specified name is already defined. Please change the type of description.');
									return true;
								}
							}

							if (item.descriptionId == description.descriptionId) {
								valid = false;
								alert('Exist Same ID. Do Not Save a Description');
								return true;
							}
						});
					}

					if (valid) {
						var confirm = $mdDialog.confirm()
						.title('저장 전 확인')
						.textContent('Language Referenceset을 저장합니다. 동의하십니까?')
						.ariaLabel('label')
						.ok('ok')
						.cancel('cancel');

						$mdDialog.show(confirm).then(function() {
							var languageReferenceset = {
								active: true,
								moduleId: '247771000300108',
								refsetId: '247781000300103',
								referencedComponentId: item.descriptionId,
								acceptabilityId: item.languageReferencesetList[0].acceptabilityId
							};

							apiservice.createLanguageReferenceset(languageReferenceset)
							.then(function(response) {
								var d = angular.copy(item);
								d.languageReferencesetList = [
									angular.copy(response)
								];
								d.priority = vm.getDescriptionPriority(d.typeId, d.languageReferencesetList[0].acceptabilityId);
								d.editMode = false;
								source.descriptions.push(d);
							}, function(error) {
							})
							.finally(function() {
								vm.initFields(vm.newItem, source.descriptions);
							});
						}, function() {
							//alert('no');
						});
					}
				} else {
					// 새로운 description을 추가하려는 경우
					var confirm = $mdDialog.confirm()
					.title('저장 전 확인')
					.textContent('새로운 Description을 저장합니다. 동의하십니까?')
					.ariaLabel('label')
					.ok('ok')
					.cancel('cancel');

					$mdDialog.show(confirm).then(function() {

						// Check : TypeId is fsn

						// if typeId is fsn
						// then
						//    Check : exist fsn
						//    if exist fsn
						//    then
						//      ERROR : Exist FSN

						//    else
						//      find semanticTag & add term & save
						//      add Preferred & save
						//

						// else
						//  save

						// ==================================

						// if type is fsn
						if (item.typeId == vm.options.type.fsn) {
							var existFsn = false;
							if (source.descriptions.length > 0) {
								angular.forEach(source.descriptions, function(description, didx) {
									if (description.active && (description.typeId == vm.options.type.fsn)) {
										existFsn = true;
										alert('Exist FSN. Do Not Save a Description');
										return true;
									}
								});
							}

							if (!existFsn) {
								// find semantic tag
								var semTag = _getSemantictag(vm.data[0].descriptions, 'us', 'kr');

								// for preferred term
								var originTerm = angular.copy(item.term);

								// add to semantic tag
								item.term += ' (' + semTag + ')';

								// add ...
								item.moduleId = '247771000300108';
								item.languageCode = source.lang;
								item.conceptId = cnptId;
								item.caseSignificanceId = '900000000000017005';

								item.languageReferencesetList[0].active = item.active;
								item.languageReferencesetList[0].refsetId = source.id;
								item.languageReferencesetList[0].moduleId = '247771000300108';

								// save
								apiservice.createDescription(item)
								.then(function(response) {
									var d = angular.copy(response);

									d.priority = vm.getDescriptionPriority(d.typeId, d.languageReferencesetList[0].acceptabilityId);
									d.editMode = false;
									//vm.otherAddMode = false;
									source.descriptions.push(d);

									//source.addMode = false;

									// generate preferred description

									var prfTerm = {
										active: true,
										term: originTerm,
										typeId: vm.options.type.synonym,
										moduleId: '247771000300108',
										languageCode: source.lang,
										conceptId: cnptId,
										caseSignificanceId:'900000000000017005',
										languageReferencesetList: [
											{
												active: true,
												refsetId: source.id,
												moduleId: '247771000300108',
												acceptabilityId: vm.options.acceptability.preferred
											}
										]
									};

									// save
									return apiservice.createDescription(prfTerm);
								})
								.then(function(response) {
									var d = angular.copy(response);

									d.priority = vm.getDescriptionPriority(d.typeId, d.languageReferencesetList[0].acceptabilityId);
									d.editMode = false;
									//vm.otherAddMode = false;
									source.descriptions.push(d);
								})
								.finally(function() {
									vm.initFields(vm.newItem, source.descriptions);
								});
							}
						} else {
							// if type is not fsn

							var desc = angular.copy(item);

							if (desc.typeId == vm.options.type.preferred) desc.typeId = vm.options.type.synonym;

							desc.moduleId = '247771000300108';
							desc.languageCode = source.lang;
							desc.conceptId = cnptId;
							desc.caseSignificanceId = '900000000000017005';

							desc.languageReferencesetList[0].active = desc.active;
							desc.languageReferencesetList[0].refsetId = source.id;
							desc.languageReferencesetList[0].moduleId = '247771000300108';

							apiservice.createDescription(desc)
							.then(function(response) {
								var d = angular.copy(response);

								d.priority = vm.getDescriptionPriority(d.typeId, d.languageReferencesetList[0].acceptabilityId);
								d.editMode = false;
								//vm.otherAddMode = false;

								source.descriptions.push(d);

								//source.addMode = false;
							})
							.finally(function() {
								vm.initFields(vm.newItem, source.descriptions);
							});
						}
					}, function() {
						//alert('no');
					});
				}
			}
		};
		
		function _updateItem(item, source) {
			if (_isValid(item)) {
				apiservice.updateDescription(item)
				.then(function(response) {
					// success
					var d = angular.copy(response);
					d.priority = vm.getDescriptionPriority(d.typeId, d.languageReferencesetList[0].acceptabilityId);
					d.editMode = false;

					var index = source.indexOf(item);
					source[index] = d;
				}, function(error) {
					// error
				});
			}
		};
		
		function _deleteItem(item, source) {
			if (_isValid(item)) {
				var cloneItem = angular.copy(item);

				delete cloneItem.priority;
				delete cloneItem.editMode;

				if (cloneItem.moduleId === cloneItem.languageReferencesetList[0].moduleId) {
					var confirm = $mdDialog.confirm()
											.title('삭제 전 확인')
											.textContent('Description을 삭제합니다. 동의하십니까?')
											.ariaLabel('label')
											.ok('ok')
											.cancel('cancel');

					$mdDialog.show(confirm).then(function() {
						apiservice.deleteDescription(cloneItem.id, cloneItem)
						.then(function(response) {
							var index = source.indexOf(item);
							source.splice(index, 1);
						}, function(error) {
						});
					}, function(error) {

					});

				} else {
					var confirm = $mdDialog.confirm()
											.title('삭제 전 확인')
											.textContent('Language Referenceset을 삭제합니다. 동의하십니까?')
											.ariaLabel('label')
											.ok('ok')
											.cancel('cancel');

					$mdDialog.show(confirm).then(function() {
						apiservice.deleteLanguageReferenceset(cloneItem.languageReferencesetList[0].id, cloneItem.languageReferencesetList[0])
						.then(function(response) {
							var index = source.indexOf(item);
							source.splice(index, 1);
						}, function(error) {

						});
					}, function(error) {

					});
				}
			}
		};
		
		function _getAvailableLanguageList(item, source) {
			// 기본 언어 : vm.lang.default
			// 추가 언어 : vm.lang.extra

			// TODO 내 권한으로 추가할 수 있는 언어
			var languagesInAuthz = ['us', 'gb', 'kr'];
			var cloneLanguages = angular.copy(languagesInAuthz);

			angular.forEach(languagesInAuthz, function (country, key) {
				if (vm.lang.base.indexOf(country) > -1 ||
				vm.lang.extra.indexOf(country) > -1) {
					cloneLanguages.splice(country, 1);
				}
			});

			return cloneLanguages;
		};
		
		function _createLanguageReferenceset(source, target) {
			var confirm = $mdDialog.confirm()
				.title('확인')
				.textContent('Language Referenceset을 추가하시겠습니까?')
				.ariaLabel('label')
				.ok('ok')
				.cancel('cancel');

			$mdDialog.show(confirm).then(function() {
				_copyDescription(source, target)
			}, function() {
				//alert('no');
			});
		};
		
		function _getSemantictag(descriptions, sourceLang, targetLang) {
			var result = undefined;
			var originSemTag = '';

			angular.forEach(descriptions, function(description, index) {
				if (description.active && (description.typeId == vm.options.type.fsn)) {
					var term = description.term;

					originSemTag = term.substring(term.lastIndexOf('(') + 1, term.lastIndexOf(')'));

					return true;
				}
			});

			angular.forEach(SEMANTICTAG, function (semTag, index) {
				if (semTag[sourceLang] == originSemTag) {
					if (typeof semTag[targetLang] !== 'undefined') result = semTag[targetLang];

					return true;
				}
			});

			if (typeof result === 'undefined') result = originSemTag;

			return result;
		};
		
		function _addLanguage(lang) {
			vm.lang.extra.push(lang);
			
			var refsetId = '';
			// TODO get Language RefsetId
			if (lang === 'kr') refsetId = '247781000300103';

			var idx = vm.data.push({
				id: refsetId,
				lang: lang,
				descriptions: [],
				//addMode: false
			});

			vm.ui.other.dataIdxs.push(idx  -1);
		};
		
		function _isExistDescription(description) {
			var isExist = false;

			angular.forEach(vm.ui.other.dataIdxs, function (dataIdx, index) {
				angular.forEach(vm.data[dataIdx].descriptions, function (dc, idx) {
					if (dc.descriptionId === description.descriptionId) {
						isExist = true;
						return true;
					}
				});
			});

			return isExist;
		};
		
		function _initUiAndData() {
			vm.ui = {
					editorEnabled: false,
					base: {
						idx: 0,
						dataIdxs: []
					},
					other: {
						idx: null,
						dataIdxs: []
					}
				};
			vm.data = [];
		};
		
		function _groupByLanguage(descriptions) {
			// init languages, descriptions
			_initUiAndData();
			
			var group = [];

			angular.forEach(descriptions, function(description, index) {

				var tempDescription = angular.copy(description);
				var languageReferenceset = description.languageReferencesetList[0];
				var priority = vm.getDescriptionPriority(
					tempDescription.typeId,
					languageReferenceset.acceptabilityId
				);
				tempDescription.priority = priority;
				tempDescription.editMode = false;

				var refsetId = languageReferenceset.refsetId;
				var isContain = _arrayObjectIndexOf(group, refsetId, 'id');
				// Not Contain
				if (isContain == -1) {
					var d = [];
					d.push(tempDescription);

					var lang = _getLanguageReferencesetProperty(refsetId, 'countryCode');

					var idx = group.push({
						id: refsetId,
						lang: lang,
						descriptions: d,
						//addMode: false
					});

					// Base Language
					if (vm.lang.base.indexOf(lang) > -1) {
						// Default Languae ('US') is Select Active
						if (lang === vm.lang.base[0]) {
							vm.ui.base.idx = (idx - 1);
						}
						vm.ui.base.dataIdxs.push(idx-1);
					} else {
						// Other Language
						vm.ui.other.dataIdxs.push(idx-1);
						if (vm.ui.other.idx == null) {
							vm.ui.other.idx = (idx-1);
						}
						vm.lang.extra.push(lang);
					}
					// Contain
				} else {
					angular.forEach(group, function(tempValue, tempKey) {
						if (tempValue.id === refsetId) {
							tempValue.descriptions.push(tempDescription);
						}
					});
				}
			});

			
			vm.data = group;
		};
		
		/**
		 * @name _copyDescription
		 * @desc Description을 복사
		 * @param source
		 * @param target
		 * @returns
		 */
		function _copyDescription(source, target) {
			//target.id = source.id;
			target.active = source.active;
			target.conceptId = source.conceptId;
			//target.descriptionId = source.descriptionId;
			target.effectiveTime = source.effectiveTime;
			target.languageCode = source.languageCode;
			target.moduleId = source.moduleId;
			target.term = source.term;
			target.typeId = source.typeId;
			target.caseSignificanceId = source.caseSignificanceId;
			target.languageReferencesetList[0].acceptabilityId = source.languageReferencesetList[0].acceptabilityId;
			target.languageReferencesetList[0].referencedComponentId = source.languageReferencesetList[0].referencedComponentId;
		};
		
		function _initFields(object, source) {
			if (typeof object.id !== 'undefined') {
				delete object.id;
			}
			if (typeof object.descriptionId !== 'undefined') {
				delete object.descriptionId;
			}
			//console.log('initFields', object, source);
			// conceptId = 유지
			object.active = true;
			object.effectiveTime = '';
			object.moduleId = '';
			object.caseSignificanceId = '';
			object.languageCode = '';
			object.term = '';
			object.languageRefsetList = [];

			if (source.length == 0) {
				object.typeId = vm.options.type.fsn; // fsn
				object.languageReferencesetList = [
					{
						acceptabilityId: vm.options.acceptability.preferred
					}
				]; // preferred
			} else {
				object.typeId = vm.options.type.synonym;
				object.languageReferencesetList = [
					{
						acceptabilityId: vm.options.acceptability.acceptable
					}
				]; // acceptable
			}
		};
		
		function _changeTypeIcon() {
			if (typeId == vm.options.type.synonym) {
				if (acceptabilityId == vm.options.acceptability.preferred) item.typeId = vm.options.type.preferred;
			}

			if (typeId == vm.options.type.preferred) {
				if (acceptabilityId == vm.options.acceptability.acceptable) item.typeId = vm.options.type.synonym;
			}
		};
		
		/**
		 * SNOMEDCT에서 지원하는 Description Type은 FSN, Synonym, Defined 총 3가지가 있다.
		 * UI상에서 Synonym은 languageRefset의 acceptabilityId에 의해 Synonym 혹은 Preferred로 표시할 수 있다.
		 * 예1) Description Type : Synonym
		 *     LanguageRefset acceptability : Preferred
		 *     위의 경우일 경우, UI상에서 Preferred를 나타내는 아이콘으로 표현해야하고,
		 *     LanguageRefset acceptability : Acceptable
		 *     위의 경우일 경우, UI상에서 Synonym을 나타내는 아이콘으로 표현해야한다.
		 */
		function _getTypeId(typeId, acceptabilityId) {
			if (vm.options.type.synonym == typeId) {
				if (vm.options.acceptability.preferred == acceptabilityId) {
					return vm.options.type.preferred;
				}
			}
			return typeId;
		};
		
		/*
		vm.setEnabled = _setEnabled,
		vm.save = _save,
		vm.getAcceptableIcon = _getAcceptableIcon,
		vm.getAcceptableName = _getAcceptableName,
		vm.setBaseLangIndex = _setBaseLangIndex,
		vm.setOtherLangIndex = _setOtherLangIndex,
		vm.getLangName = _getLangName,
		 */
		function _setEnabled(isEnabled) {
			vm.ui.editorEnabled = isEnabled;
		};
		
		
		function _getAcceptableIcon(acceptabilityId, typeId) {
			var icon = '';
			
			if (typeId == vm.options.type.fsn) {
				icon = 'glyphicon-star-empty';
			} else if (typeId == vm.options.type.preferred) {
				icon = 'glyphicon-star';
			} else if (typeId == vm.options.type.definition) {
				icon = 'glyphicon-ok';
			}

			if (acceptabilityId == vm.options.acceptability.acceptable) {
				icon = "glyphicon-ok";
			} else if (acceptabilityId == vm.options.acceptability.preferred) {
				if (typeId == vm.options.type.fsn) {
					icon = "glyphicon-star-empty";
				} else {
					icon = "glyphicon-star"
				}
			} else {
				icon = "undefined";
			}
			
			return icon;
		};
		
		function _getAcceptableName(acceptabilityId) {
			var name = '';
			if (acceptabilityId == vm.options.acceptability.preferred) {
				name = "preferred";
			} else if (acceptabilityId == vm.options.acceptability.acceptable) {
				name = "acceptable";
			} else {
				name = "undefined";
			}
			
			return name;
		};
		
		function _setBaseLangIndex(index) {
			vm.ui.base.idx = index;
		};
		
		function _setOtherLangIndex(index) {
			vm.ui.other.idx = index;
		};
		
		function _getLangName(countryCode) {
			return _getArrayObjectValue(LANGREFSET, 'countryCode', countryCode, 'name');
		};
		
		
		function _toggleEditMode(object) {
			object.editMode = !object.editMode;
		};
		
		function _arrayObjectIndexOf(array, term, property) {
			var index = -1;
			if (property != "") {
				for(var i = 0, len = array.length; i < len; i++) {
					if (array[i][property] === term) {
						index = i;
						break;
					}
				}
			} else {
				for(var i = 0, len = array.length; i < len; i++) {
					if (array[i] === term) {
						index = i;
						break;
					}
				}
			}

			return index;
		};
		
		function _getArrayObjectValue(array, property, term, returnProperty) {
			var value = null;
			for(var i = 0, len = array.length; i < len; i++) {
				if (array[i][property] === term) {
					value = array[i][returnProperty];
					break;
				}
			}
			return value;
		};
		
		function _getLanguageReferencesetProperty(langrefsetId, property) {
			return _getArrayObjectValue(LANGREFSET, 'id', langrefsetId, property);
		};
		
		function _isValid(item) {
			var isValid = true;

			// required field
			// active              - ok
			// moduleId            - default
			// languageCode        - default
			// typeId              - ok
			// conceptId           - default
			// term                - ok
			// caseSignificanceId  - default
			// languageReferenceSetId - ok
			// acceptabilityId     - ok

			if (item.term == '' ||
			typeof item.term === 'undefined' ||
			typeof item.term == null) {
				alert('term is not empty!');
				isValid = false;
			}

			if (item.typeId == vm.options.type.preferred) {
				item.typeId = vm.options.type.synonym;
				item.languageReferencesetList[0].acceptabilityId = vm.options.acceptability.preferred;
			} else if (item.typeId == vm.options.type.synonym) {
				item.languageReferencesetList[0].acceptabilityId = vm.options.acceptability.acceptable;
			}
						/* TODO
						// validate all columns
						self.columnsDefinition.forEach(function (column) {
						if (isValid) {
						// required validation
						if (column.required = 'true') {
						isValid = item[column.binding] != undefined;
					}
				}
			});
			*/
			
			return isValid;
			};
			
	};
	
})();