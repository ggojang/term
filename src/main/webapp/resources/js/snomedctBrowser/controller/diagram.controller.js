(function () {
	
	'use strict';
	
	angular
		.module('app.snomedctBrowser')
		.controller('BrowserDiagramController', BrowserDiagramController);
	
	BrowserDiagramController.$inject = ['$scope', '$filter', '$window', '$mdDialog'];
	function BrowserDiagramController($scope, $filter, $window, $mdDialog) {
		
		var vm = this;
		
		vm.hide = _hide;
		vm.saveAsPng = _saveAsPng;
		vm.loadDefs = _loadDefs;
		vm.d3Rect = _d3Rect;
		vm.d3RectR = _d3RectR;
		vm.d3Text = _d3Text;
		vm.d3Polyline = _d3Polyline;
		vm.d3Circle = _d3Circle;
		vm.d3Line = _d3Line;
		vm.drawSctBox = _drawSctBox;
		vm.connectElements = _connectElements;
		vm.drawAttributeGroupNode = _drawAttributeGroupNode;
		vm.drawConjunctionNode = _drawConjunctionNode;
		vm.drawEquivalentNode = _drawEquivalentNode;
		vm.drawSubsumedByNode = _drawSubsumedByNode;
		vm.drawSubsumesNode = _drawSubsumesNode;
		
		var svgDefsValue = [
			{
				id: 'BlackTriangle', refX: 0, refY: 10, markerWidth: 8, markerHeight: 6, markerUnits: 'strokeWidth', orient: 'auto', viewBox: '0 0 22 20', fill: 'black', stroke: 'black', strokeWidth: 2, path: 'M 0 0 L 20 10 L 0 20 z'
			},
			{
				id: 'ClearTriangle', refX: 0, refY: 10, markerWidth: 8, markerHeight: 8, markerUnits: 'strokeWidth', orient: 'auto', viewBox: '0 0 22 20', fill: 'white', stroke: 'black', strokeWidth: 2, path: 'M 0 0 L 20 10 L 0 20 z'
			},
			{
				id: 'LineMarker', refX: 0, refY: 10, markerWidth: 8, markerHeight: 8, markerUnits: 'strokeWidth', orient: 'auto', viewBox: '0 0 22 20', fill: 'white', stroke: 'black', strokeWidth: 2, path: 'M 0 10 L 20 10'
			}
		];
		
		var idSequence = 0;

		function _hide() {
			$mdDialog.hide();
		};
		
		function _saveAsPng() {
			var svg = d3.select("svg");
			var html = svg
				.attr("version", 1.1)
		        .attr("xmlns", "http://www.w3.org/2000/svg")
		        .node().parentNode.innerHTML;
	    	var imgsrc = 'data:image/svg+xml;base64,'+ btoa(html);
			var img = '<img src="'+imgsrc+'">';
			d3.select("#svgdataurl").html(img);

			var canvas = document.createElement("canvas"),
			context = canvas.getContext("2d");
			canvas.width = svg.node().getBBox().width + 25;
			canvas.height = svg.node().getBBox().height + 25;

			var image = new Image;
			image.src = imgsrc;
			image.onload = function() {
				context.drawImage(image, 0, 0);

				var canvasdata = canvas.toDataURL("image/png");

				//var pngimg = '<img src="'+canvasdata+'" width="1000" height="' + svg.node().getBBox().height + '" >';
				//d3.select("#pngdataurl").html(pngimg);

				var a = document.createElement("a");
				
				// Polyfill 
				if (Modernizr.adownload) {
				  // supported
					a.download = "diagram-" + vm.source.concept.conceptId + ".png";
					a.target = '_blank';
					a.href = canvasdata;
					a.click();
				} else {
				  // not-supported
					var b = document.createElement('button');
					b.type = 'submit';
					b.onclick = function() {
						window.open(canvasdata);
					};
					b.click();
				}
				
			};
		}

		function _loadDefs(svg) {
			var defs = svg
						.append('defs')
						.attr('id', 'SctDiagramsDefs');
			var marker = defs.selectAll('marker')
				.data(svgDefsValue)
				.enter()
				.append('marker')
					.attr('id', function(d) { return d.id })
					.attr('refX', function(d) { return d.refX })
					.attr('refY', function(d) { return d.refY })
					.attr('markerWidth', function(d) { return d.markerWidth })
					.attr('markerHeight', function(d) { return d.markerHeight })
					.attr('markerUnits', function(d) { return d.markerUnits })
					.attr('orient', function(d) { return d.orient })
					.attr('viewBox', function(d) { return d.viewBox })
					.attr('fill', function(d) { return d.fill })
					.attr('stroke', function(d) { return d.stroke })
					.attr('stroke-width', function(d) { return d.strokeWidth })
					.append('path')
						.attr('d', function(d) { return d.path });

		};

		function _d3Rect(svg, x, y, width, height, style) {
			var rectangles = svg.append('rect');
				rectangles
					.attr('x', x)
					.attr('y', y)
					.attr('width', width)
					.attr('height', height);
			if (style.id)
				rectangles.attr('id', style.id);
			rectangles
				.attr('fill', style.fill)
				.attr('stroke', style.stroke)
				.attr('stroke-width', style.strokeWidth);

			return rectangles;
		};

		function _d3RectR(svg, x, y, width, height, rx, ry, style) {
			var rectangles = vm.d3Rect(svg, x, y, width, height, style);

			rectangles
				.attr('rx', rx)
				.attr('ry', ry);

			return rectangles;
		};

		function _d3Text(svg, x, y, label, style) {
			var text = svg.append('text');

			text.attr('x', x)
				.attr('y', y)
				.attr('font-family', style.fontFamily)
				.attr('font-size', style.fontSize)
				.attr('fill', style.fill)
				.text(label);
			return text;
		};

		function _d3Polyline(svg, points, style) {
			var polyline = svg.append('polyline');

			var refinePoints = "";
			angular.forEach(points, function(point, i) {
				refinePoints += point[0] + ',' + point[1] + ' ';
			});

			polyline.attr('points', refinePoints)
					.attr('id', style.id)
					.attr('fill', style.fill)
					.attr('stroke', style.stroke)
					.attr('stroke-width', style.strokeWidth)
					.attr('marker-end', style.markerEnd);

			return polyline;
		};

		function _d3Circle(svg, cx, cy, r, style) {
			return svg.append('circle')
						.attr('cx', cx)
						.attr('cy', cy)
						.attr('r', r)
						.attr('fill', style.fill)
						.attr('stroke', style.stroke)
						.attr('stroke-width', style.strokeWidth);
		};

		function _d3Line(svg, x1, y1, x2, y2, style) {
			return svg.append('line')
						.attr('x1', x1)
						.attr('y1', y1)
						.attr('x2', x2)
						.attr('y2', y2)
						.attr('stroke', style.stroke)
						.attr('strokeWidth', style.strokeWidth);
		};

		function _drawSctBox(svg, x, y, label, sctid, cssClass) {
			var testText = "Test";
			if (label && sctid) {
				if (label.length > sctid.length)
					testText = label;
				else
					testText = sctid;
			} else if (label) {
				testText = label;
			} else if (sctid) {
				testText = sctid;
			}

			var fontFamily = '"Helvetica Neue",Helvetica,Arial,sans-serif';

			var tempText =  vm.d3Text(svg, x, y, testText, {fontFamily: fontFamily, fontSize: '12', fill: 'black'});
			var textHeight = tempText.node().getBBox().height;
			var textWidth = tempText.node().getBBox().width;
			textWidth = $window.Math.round(textWidth * 1.2);

			tempText.remove();

			var rect = null;
			var widthPadding = 20;
			var heightPadding = 25;

			if (!sctid || !label) {
				heightPadding = 15;
			}

			if (cssClass == "sct-primitive-concept") {
				rect = vm.d3Rect(svg, x, y, textWidth + widthPadding, textHeight + heightPadding, {id: 'rect'+idSequence, fill: '#99ccff', stroke: '#333', strokeWidth: 2});
			} else if (cssClass == "sct-defined-concept") {
				rect = vm.d3Rect(svg, x-2, y-2, textWidth + widthPadding + 4, textHeight + heightPadding + 4, {fill: 'white', stroke: '#333', strokeWidth: 1});
    			var innerRect = vm.d3Rect(svg, x, y, textWidth + widthPadding, textHeight + heightPadding, {id: 'rect'+idSequence, fill: '#ccccff', stroke: '#333', strokeWidth: 1});
			} else if (cssClass == "sct-attribute") {
				rect = vm.d3RectR(svg, x-2, y-2, textWidth + widthPadding + 4, textHeight + heightPadding + 4, 18, 18, {fill: 'white', stroke: '#333', strokeWidth: 1});
    			var innerRect = vm.d3RectR(svg, x, y, textWidth + widthPadding, textHeight + heightPadding, 18, 18, {id: 'rect'+idSequence, fill: '#ffffcc', stroke: '#333', strokeWidth: 1});
			} else if (cssClass == "sct-slot") {
				rect = vm.d3Rect(svg, x, y, textWidth + widthPadding, textHeight + heightPadding, {id: 'rect'+idSequence, fill: '#99ccff', stroke: '#333', strokeWidth: 2});
			} else {
				rect = vm.d3Rect(svg, x, y, textWidth + widthPadding, textHeight + heightPadding, {id: 'rect'+idSequence, fill: 'white', stroke: 'black', strokeWidth: 1});
			}

			if (sctid && label) {
				vm.d3Text(svg, x + 10, y + 16, sctid.toString(), {fontFamily: fontFamily, fontSize: '10', fill: 'black'});
    			vm.d3Text(svg, x + 10, y + 31, label, {fontFamily: fontFamily, fontSize: '12', fill: 'black'});
			} else if (label) {
				vm.d3Text(svg, x + 10, y + 18, label, {fontFamily: fontFamily, fontSize: '12', fill: 'black'});
			} else if (sctid) {
				vm.d3Text(svg, x + 10, y + 18, sctid.toString(), {fontFamily: fontFamily, fontSize: '12', fill: 'black'});
			}

			idSequence++;

			return rect;
		};

		function _connectElements(svg, fig1, fig2, side1, side2, endMarker) {
			var rect1cx = fig1.node().getBBox().x;
		    var rect1cy = fig1.node().getBBox().y;
		    var rect1cw = fig1.node().getBBox().width;
		    var rect1ch = fig1.node().getBBox().height;

		    var rect2cx = fig2.node().getBBox().x;
		    var rect2cy = fig2.node().getBBox().y ;
		    var rect2cw = fig2.node().getBBox().width;
		    var rect2ch = fig2.node().getBBox().height;

		    var markerCompensantion1 = 15;
		    var markerCompensantion2 = 15;

		    var originX = 0,
		    	originY = 0,
		    	destinationX = 0,
		    	destinationY = 0;

		    switch(side1) {
		        case 'top':
		            originY = rect1cy;
		            originX = rect1cx + (rect1cw/2);
		            break;
		        case 'bottom':
		            originY = rect1cy + rect1ch;
		            originX = rect1cx + (rect1cw/2);
		            break;
		        case 'left':
		            originX = rect1cx - markerCompensantion1;
		            originY = rect1cy + (rect1ch/2);
		            break;
		        case 'right':
		            originX = rect1cx + rect1cw;
		            originY = rect1cy + (rect1ch/2);
		            break;
		        case 'bottom-50':
		            originY = rect1cy + rect1ch;
		            originX = rect1cx + 40;
		            break;
		        default:
		            originX = rect1cx + (rect1cw/2);
		            originY = rect1cy + (rect1ch/2);
		            break;
		    }

		    switch(side2) {
		        case 'top':
		            destinationY = rect2cy;
		            destinationX = rect2cx + (rect2cw/2);
		            break;
		        case 'bottom':
		            destinationY = rect2cy + rect2ch;
		            destinationX = rect2cx + (rect2cw/2);
		            break;
		        case 'left':
		            destinationX = rect2cx - markerCompensantion2;
		            destinationY = rect2cy + (rect2ch/2);
		            break;
		        case 'right':
		            destinationX = rect2cx + rect2cw;
		            destinationY = rect2cy + (rect2ch/2);
		            break;
		        case 'bottom-50':
		            destinationY = rect2cy + rect2ch;
		            destinationX = rect2cx + 50;
		            break;
		        default:
		            destinationX = rect2cx + (rect2cw/2);
		            destinationY = rect2cy + (rect2ch/2);
		            break;
		    }

		    if (endMarker == null) endMarker = "BlackTriangle";

		    var polyline1 = vm.d3Polyline(svg, [[originX, originY],
        			[originX, destinationY], [destinationX, destinationY]]
				, {id: 'poly1', fill: 'none', stroke: 'black', strokeWidth: 2, markerEnd: 'url(#' + endMarker + ')'});
		}

		function _drawAttributeGroupNode(svg, x, y) {
			var circle = vm.d3Circle(svg, x, y, 20, {fill: 'white',stroke: 'black', strokeWidth: 2});
			return circle;
		};

		function _drawConjunctionNode(svg, x, y) {
			var circle = vm.d3Circle(svg, x, y, 10, {fill: 'black',stroke: 'black', strokeWidth: 2});
			return circle;
		};

		function _drawEquivalentNode(svg, x, y) {
			var g = svg.append('g');
		    vm.d3Circle(g, x, y, 20, {fill: 'white',stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-7, y-5, x+7, y-5, {stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-7, y, x+7, y, {stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-7, y+5, x+7, y+5, {stroke: 'black', strokeWidth: 2});
		    return g;
		};

		function _drawSubsumedByNode(svg, x, y) {
			var g = svg.append('g');
		    vm.d3Circle(g, x, y, 20, {fill: 'white',stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-7, y-8, x+7, y-8, {stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-7, y+3, x+7, y+3, {stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-6, y-8, x-6, y+3, {stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-7, y+7, x+7, y+7, {stroke: 'black', strokeWidth: 2});
		    return g;
		};

		function _drawSubsumesNode(svg, x, y) {
			var g = svg.append('g');
		    vm.d3Circle(g, x, y, 20, {fill: 'white',stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-7, y-8, x+7, y-8, {stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-7, y+3, x+7, y+3, {stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x+6, y-8, x+6, y+3, {stroke: 'black', strokeWidth: 2});
		    vm.d3Line(g, x-7, y+7, x+7, y+7, {stroke: 'black', strokeWidth: 2});
		    return g;
		};
	};
	
})();