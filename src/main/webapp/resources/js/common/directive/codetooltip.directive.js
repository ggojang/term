(function () {
	
	'use strict';
	/*
	directive (이름, 함수명) 
	1. http://araikuma.tistory.com/471
	2. http://blog.naver.com/PostView.nhn?blogId=scw0531&logNo=221031346326
	*/
	angular
		.module('app.common')
		.directive('codeTooltip', codeTooltip);

        /*
	Dependency Injection(DI)
	1. https://www.tutorialspoint.com/angularjs/angularjs_dependency_injection.htm

	$compile
	1. https://odetocode.com/blogs/scott/archive/2014/05/07/using-compile-in-angular.aspx
	2. http://devdoo.tistory.com/134
	*/
	codeTooltip.$inject = ['$compile', 'apiservice'];
	function codeTooltip($compile ,apiservice) {
		var directive = {
			restrict: 'EA',
			bindToController: {
				codeSystem: '=',
				code: '=',
				placement: '='
			},
			controller: CodetooltipController,
			controllerAs: 'codetVm',
			link: link 
		};
		return directive;
		
		function link(scope, element, attribute,  controller) {
			controller.isExists = false;

			if (controller.code) {
				apiservice.getCode(controller.codeSystem, controller.code)
				.then(function (code) {
					if (code) {
						$(element).popover({
							trigger:'hover',
							html:true,
							container: 'body',
							content: function() {
								var content = '<div>';
								if (controller.codeSystem === 'LOINC') {
									content += '<div class="text-bold">' + code.partTypeName + '</div>';
									content += '<span class="text-bold">' + code.partName + '</span>';
								} else if (controller.codeSystem === 'ICD10') {
									if (code.kinds[0].kind === 'preferred') {
										content += '<div class="text-bold">' + code.kinds[0].label;
										if (code.kinds[0].ref != null) {
											content +=  ' ' + code.kinds[0].ref + '</div>';
										} else {
											content += '</div>';
										}
										
									}

                                                                        var totalLength=0;
                                                                        var inclusionsLength=0;
                                                                        var exclusionsLength=0;

                                                                        var content1 = '';
                                                                        var content2 = '';
                                                                        for (var i=0; i < code.kinds.length; i++) {
                                                                                if (code.kinds[i].kind === 'inclusion') {
  											inclusionsLength++;
											if (code.kinds[i].label != null) {
          											content1 += '<div>';
          											content1 += code.kinds[i].label;
                                                                                                if (code.kinds[i].ref != null) {
                                                                                                        content1 += ' ' + code.kinds[i].ref + '</div>';
                                                                                                } else {
                                                                                                        content1 += '</div>';
                                                                                                }
                                                                                        }
                                                                                }
	              								else if (code.kinds[i].kind === 'exclusion') {
  											exclusionsLength++;
                                                                                        if (code.kinds[i].label != null) {
                                                                                                content2 += '<div>';
                                                                                                content2 += code.kinds[i].label;
                                                                                                if (code.kinds[i].ref != null) {
                                                                                                        content2 += ' ' + code.kinds[i].ref + '</div>';
                                                                                                } else {
                                                                                                        content2 += '</div>';
                                                                                                }
                                                                                        }
                                                                                }
                                                                        }

                                                                        if (inclusionsLength > 0)
                                                                                content += '<div>[Inclusion]</div>' + content1;

                                                                        if (exclusionsLength > 0)
                                                                                content += '<div>[Exclusion]</div>' + content2;

/*									if (code.inclusions != null) {
										content += '<div>[Inclusion]</div>';
										var inclusionsLength = code.inclusions.length;
										if (inclusionsLength > 0) {
											for (var i=0; i < inclusionsLength; i++) {
												var ftypesLength = code.inclusions[i].ftypes.length;
												for (var j=0; j < ftypesLength; j++) {
													if (code.inclusions[i].ftypes[j].label != null) {
														content += '<div>';
														content += code.inclusions[i].ftypes[j].label;
														if (code.inclusions[i].ftypes[j].ref != null) {
															content += ' ' + code.inclusions[i].ftypes[j].ref + '</div>';
														} else {
															content += '</div>';
														}
													}
												}
											}
										}

									}

									if (code.exclusions != null) {
										content += '<div>[Exclusion]</div>';
										var exclusionsLength = code.exclusions.length;			
										if (exclusionsLength > 0) {
                                                                                	for (var i=0;i < exclusionsLength; i++) {
												var ftypesLength = code.exclusions[i].ftypes.length;
                                                                                                for (var j=0; j < ftypesLength; j++) {
                                                                                                        if (code.exclusions[i].ftypes[j].label != null) {
                                                                                                                content += '<div>';
                                                                                                                content += code.exclusions[i].ftypes[j].label;
                                                                                                                if (code.exclusions[i].ftypes[j].ref != null) {
                                                                                                                        content += ' ' + code.exclusions[i].ftypes[j].ref + '</div>';
                                                                                                                } else {
                                                                                                                        content += '</div>';
                                                                                                                }
                                                                                                        }
                                                                                                }	
											}

                                                                                }
                                                                        }
*/
									if (code.mobidities != null) {
										var mobiditiesLength = code.mobidities.length;
										if (mobiditiesLength > 0) {
											content += '<div>Mobidity : ';
											for (var i = 0; i < mobiditiesLength; i++) {
												content += code.mobidities[i];
												if (i < mobiditiesLength - 1) {
													content += ', ';
												}
											}
											content += '</div>';
										}
									}
									if (code.motalities != null) {
										var motalitiesLength = code.motalities.length;
										if (motalitiesLength > 0) {
											content += '<div>Motality : ';
											for (var i = 0; i < motalitiesLength; i++) {
												content += code.motalities[i];
												if (i < motalitiesLength - 1) {
													content += ', ';
												}
											}
											content += '</div>';
										}
									}

								}
								content += '</div>';

								return content;
							},
							title: function () {
								var title = '';
								if (controller.codeSystem === 'ICD10') {
									title = 'ICD-10 : ' + controller.code;
									if (code.usageKind != null) {
										if (code.usageKind === 'dagger') {
											title += ' &#8224;';
										} else if (code.usageKind === 'aster') {
											title += ' *';
										}
									}
								} else {
									title = controller.codeSystem + ' : ' + controller.code;;
								}
								return title;
							},
							placement: 'bottom'
						});
						controller.isExists = true;
					}

					var template = '<span ng-style="codetVm.isExists ? {\'font-weight\':\'bold\'}:{}">{{codetVm.code}}</span>';
					var linkFn = $compile(template);
					var templateContent = linkFn(scope);
					element.append(templateContent);
				});

			}
		};
	};
	
	CodetooltipController.$inject = ['$scope', 'apiservice'];
	function CodetooltipController($scope, apiservice) {
		
		function _getTemplateUrl() {
			var url = 'resources/popover',
				codeSystem = vm.codeSystem.toLowerCase();
			
			return url + '/' + codeSystem + '.html';
		};
	};
	

})();
