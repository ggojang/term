/**
 * Right click show contextMenu
 */
(function() {
	'use strict';
	
	angular.module('app.concept').directive('ngCombo', ngCombo);
	ngCombo.$inject = ['$parse', '$window', '$timeout', '$document'];
	function ngCombo($parse, $window, $timeout, $document) {
		var directive = {
				restrict: 'AEC',
				link: link
			};
		return directive;
		function link(scope, element, attributes, controller, transclude) {
			var fn = $parse(attributes.ngCombo);
			
			element.on('click', function(event) {
				//console.log(event);
				//angular.element('ul.ce-combo').remove();
				var pa = angular.element(this);
				if(angular.element('ul.ce-combo').length == 1) {
					var drop = angular.element('ul.ce-combo');
					if(drop.attr("id") === pa.attr("key")) {
						drop.remove();
						scope.$apply();
						return false;
					} else {
						drop.remove();
					}
				}
				
				var target = attributes.target == undefined || attributes.target === 'self' ? element : angular.element(attributes.target);
				scope.$apply(function() {
					event.preventDefault();
					var offset = target.offset();
					var width = target.outerWidth();
					var top = offset.top + target.outerHeight();
					var left = offset.left;
					var dropheight = attributes.dropheight != undefined ? 'max-height:'+attributes.dropheight+'px;' : '';
					
					var ul = angular.element('<ul id="'+attributes.key+'" class="ce-combo" style="top:'+top+'px;left:'+left+'px;min-width:'+width+'px;'+dropheight+'"></ul>');
					var opts = attributes.option == undefined || attributes.option == '' ? [] : angular.fromJson(attributes.option);
					if(opts.length > 0) {
						
						for(var i=0; i<opts.length; i++){
							var opt = opts[i];
							var name = attributes.namekey == undefined ? opt.name : opt[attributes.namekey]+' <span class="text-primary">(FSN : '+opt.fsn+')</span>';
							var li = angular.element('<li><a rel="'+i+'">'+name+'</a></li>');
							li.on('click', 'a', function(e){
								
								var ts = angular.element(this);
				    			var val = ts.attr('rel');
			    			   var k = angular.isNumber(val) == true ? parseInt(val) : val;
			    			   event.selComboIndex = k;
			    			   fn(scope, {$event:event});
			    			   ts.parents('ul.ce-combo').remove();
							});
							ul.append(li);
						}
						
						angular.element('body').append(ul);
						var h = angular.element('#'+attributes.key).outerHeight();
						if((h+top) > angular.element('body').height()) {
							var _top = offset.top - h - 3;
							angular.element('#'+attributes.key).css('top',_top+'px');
						}
						angular.element('md-tab-content').one('scroll', function(){
			    			angular.element('ul.ce-combo').remove();
						});
						//angular.element('body').on('click', dropReset);
						//angular.element('body').not("[key='"+attributes.key+"']").on('click', function(){
						//	console.log(angular.element(this));
						//});
						
						angular.element('body').on('click', function(e){
							e.stopImmediatePropagation();
							var obj = e.target.nodeName.toLowerCase() === 'span' ? angular.element(e.target).parent() : angular.element(e.target);
							if(obj.attr('ng-combo') === undefined) {
								angular.element('ul.ce-combo').remove();
							}
						});
						
					} //opts.length > 0
					else {
						event.selComboIndex = -1;
		    			fn(scope, {$event:event});
					}
				});
				
			});
			
		}
	}
	
	angular.module('app.concept').directive('ngContextMenu', ngContextMenu);
	ngContextMenu.$inject = ['$parse', '$window'];
	function ngContextMenu($parse, $window) {
		var directive = {
				restrict: 'AEC',
				link: link
			};
		return directive;
		function link(scope, element, attributes, controller, transclude) {
			//console.log(scope.ngContextMenuOption);26
			//console.log($window.innerWidth+'-----------'+$window.innerHeight);
	    	//console.log(event.pageX+'------'+event.pageY);
			var fn = $parse(attributes.ngContextMenu);
			angular.element('body').on('click', function(){
				var ngcm = angular.element('.ngcm-context');
				if(ngcm.length > 0) {
					ngcm.remove();
				}
			});
			
	       element.on('contextmenu', function(event) {
	    	   //createChildCtx(scope.ngContextMenuOption, ul);
	    	   scope.$apply(function() {
	    		   event.preventDefault();
	    		   if(angular.element('.ngcm-context').length > 0) {
	    			   angular.element('.ngcm-context').remove();
	    		   }
	    		   var opts = angular.fromJson(attributes.option);
	    		   var y = event.pageY + (opts.length * 26) > angular.element('body').height() ? event.pageY - (opts.length * 26) : event.pageY;
	    		   var ul = angular.element('<ul class="ngcm-context" style="left:'+event.pageX+'px;top:'+y+'px;"></ul>');
	    		   
	    		   for(var i=0; i<opts.length; i++) {
		    		   var opt = opts[i];
		    		   var disabled = opt.disabled === undefined || opt.disabled == false ? '' : 'ngcm-contextmenu-disabled';
		    		   var icon = opt.icon === undefined ? '' : opt.icon;
		    		   var child = angular.element('<li class="'+disabled+'"><a rel="'+opt.key+'"><i class="glyphicon '+icon+'"></i><span class="ngcm-contextmenu-sep">&nbsp;</span>'+opt.name+'</a></li>');
		    		   if(disabled === '') {
			    		   child.on('click', 'a', function(e){
			    			   var ts = angular.element(this);
			    			   var val = ts.attr('rel');
			    			   var k = angular.isNumber(val) == true ? parseInt(val) : val;
			    			   event.ngcmKey = k;
			    			   fn(scope, {$event:event});
			    			   ts.parents('ul.ngcm-context').remove();
			    		   });
		    		   }   
		    		   ul.append(child);
		    	   }
	    		   angular.element('body').append(ul);
	    		   
	    	   });
	        });
	        
	        /**
	       function createChildCtx(opts, parent) {
	    	   for(var i=0; i<opts.length; i++) {
	    		   var opt = opts[i];
	    		   //if(opt.disabled === undefined) opt.disabled = false;
	    		   //if(opt.icon === undefined) opt.icon = '';
	    		   var disabled = opt.disabled === undefined || opt.disabled == false ? '' : 'ngcm-contextmenu-disabled';
	    		   var icon = opt.icon === undefined ? '' : opt.icon;
	    		   var child = angular.element('<li class="'+disabled+'"><a id="ngcm-'+opt.key+'"><i class="glyphicon '+icon+'"></i><span class="ngcm-contextmenu-sep">&nbsp;</span>'+opt.name+'</a></li>');
	    		   parent.append(child);
	    	   }
	       	}
	       	*/
		}
	}
	
	angular.module('app.concept').directive('ngEnter', ngEnter);
	//ngEnter.$inject = ['$eval'];
	function ngEnter() {
		return function(scope, element, attributes) {
			element.bind('keydown keypress', function(event){
				if(event.which === 13) {
					if (scope.$$phase == '$apply' || scope.$$phase == '$digest' ) {
						scope.$eval(attributes.ngEnter);
					} else {
						scope.$apply(function (){
		                    scope.$eval(attributes.ngEnter);
		                });
					}
					event.preventDefault();
				}
			});
		};
	}
	
	angular.module('app.concept').directive('ceVisualization', ceVisualization);
	ceVisualization.$inject = ['$parse', '$window', '$timeout'];
	function ceVisualization($parse, $window, $timeout) {
		return {
			restrict: 'EA',
			link: function(scope, element, attributes) {
				scope.$watch('visualization', function handleChange(newValue, oldValue) {
					if (!newValue) {
	                    return;
	                }
					//console.log(newValue);
					var width = parseInt(attributes.width), height = parseInt(attributes.height);
					var root = newValue;
					
					var force = d3.layout.force()
                    .linkDistance(250)
                    .charge(-120)
                    .gravity(.05)
                    .size([width, height])
                    .on("tick", tick);
					
					var svg = d3.select(element[0]);
					var link = svg.selectAll(".ce-cv-vs-link");
					var node = svg.selectAll(".ce-cv-vs-node");
					update();
					function update() {
						var nodes = flatten(root);
						var links = d3.layout.tree().links(nodes);
						
						force.nodes(nodes).links(links).start();
						link = link.data(links, function (d) {
	                        return d.target.id;
	                    });
						link.exit().remove();
						link.enter().insert("line", ".ce-cv-vs-node").attr("class", "ce-cv-vs-link");
						
						node = node.data(nodes, function (d) {
	                        return d.id;
	                    });
						node.exit().remove();
						
						var nodeEnter = node.enter().append("g")
                        .attr("class", "ce-cv-vs-node")
                        .on("click", click)
                        .call(force.drag);
						
						nodeEnter.append("circle")
						.attr("r", function (d) {return d.size;})
						.attr("class", function (d) {
							return "ce-cv-vs-" + d.type;
						});
						
						nodeEnter.append("text").attr("dy", ".35em").text(function (d) {return d.name;});
					}
					
					function tick() {
						link.attr("x1", function (d) {
	                        return d.source.x;
	                    })
                     .attr("y1", function (d) {
                    		return d.source.y;
		                })
		              .attr("x2", function (d) {
                    		return d.target.x;
		                })
		              .attr("y2", function (d) {
                    		return d.target.y;
		                });
		                
		              node.attr("transform", function (d) {
	                        return "translate(" + d.x + "," + d.y + ")";
	                    });
					}
					
					function click(d) {
						if (d3.event.defaultPrevented) return;
						if (d.children) {
							d._children = d.children;
							d.children = null;
						} else {
							d.children = d._children;
							d._children = null;
						}
						update();
					}
					
					function flatten(root) {
						var nodes = [];
						var i = 0;
						function recurse(node) {
							if (node.children) node.children.forEach(recurse);
							if (!node.id) node.id = ++i;
							nodes.push(node);
						}
						recurse(root);
						return nodes;
					}
					
				});
			}
		};
	}
	
})();