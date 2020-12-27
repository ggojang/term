(function () {
	
	'use strict';
	
	angular
		.module('app.mapBrowser')
		.controller('MapController', EntityController);
	
	EntityController.$inject = ['$scope', '$stateParams', '$element', 'dataservice', 'entityservice', '$timeout', '$log', '$localstorage'];
	function EntityController($scope, $stateParams, $element, dataSvc, entitySvc, $timeout, $log, $localstorage) {
		
		// ----------------------------------------
		// Local variable
		// ----------------------------------------

		var qs1Parsing =
		  "{" +
		  "\"analyzer\" : \"standard\"," +
		  "\"text\" : \"";
		
		var qs2Parsing =
		  "\"" +
		"}";
		
		var qs1 =
		  "{" +
		    "\"query\" : {" +
		      "\"bool\": {" +
		        "\"filter\": [" +
		          "{\"script\":" +
		            "{\"script\": \"doc[\'maxConceptDescriptionEffectiveTimeAndActive\'].value == (doc[\'conceptEffectiveTime\'].value + \'+\' + doc[\'descriptionEffectiveTime\'].value + \'+\' + \'1\');\"" +
		            "}" +
		          "}" +
		          ",";


		var qs2_semantic =
		          "{\"terms\":" +
		            "{\"semanticTag\": [";
		
		var qs3_index =
		            "]}" +
		          "}" +
		        "]," +
		        "\"must\": {" +
		          "\"match\" : {" +
		            "\"term." ;
		
		var qs4_term =
		            "\" : {" +
		              "\"query\" : \"";
		
		var qs2_phrase =
		            "]}" +
		          "}" +
		        "]," +
		        "\"must\": {" +
		          "\"match_phrase\" : {" +
		            "\"term\" : {" +
		                "\"query\" : \"";

		  var qs5 =
		                "\"" +
		              "}" +
		            "}" +
		          "}" +
		        "}" +
		      "}," +
		      "\"sort\": [" +
		        "{ \"_score\": { \"order\": \"desc\" }}" +
		      "]," +
		      "\"size\" : 20" +
		    "}";
		
		  var semanticTags = [
		      'finding', 'disorder', 'procedure','regime/therapy', 'situation',
		      'assessment scale', 'attribute', 'basic dose form', 'body structure', 'cell',
		      'cell structure','clinical drug','core metadata concept', 'disposition', 'dose form',
		      'environment','ethnic group', 'event','foundation metadata concept','geographic location',
		      'inactive concept','intended site','life style','link assertion','linkage concept',
		      'medicinal product form','medicinal product','metadata','morphologic abnormality','namespace concept',
		      'navigational concept','observable entity','occupation','organism','OWL metadata concept',
		      'person','physical force','physical object','product name','product',
		      'qualifier value','racial group','record artifact','release characteristic','religion/philosophy',
		      'role','SNOMED RT+CTV3','social concept','special concept','specimen',
		      'staging scale','state of matter','substance','supplier','transformation',
		      'tumor staging','unit of presentation'
		  ];

		  var vm = this;
		  
		  vm.setCheckBox = setCheckBox;
		  vm.setRadioButtonGroup = setRadioButtonGroup;
		  vm.setRadioButton = setRadioButton;
		  vm.setRBtoAllTrue = setRBtoAllTrue;
		  vm.setRBtoAllFalse = setRBtoAllFalse;
		  vm.setRBtoSomeTrue = setRBtoSomeTrue;
		  vm.setRBtoSomeFalse = setRBtoSomeFalse;
		  
  function onKeyDown() {
    if (event.keyCode == 13) {
      //TODO : 실행시킬 코드
      $('p').hide('.note');
      term_search();
    }
  }

		  function setCheckBox(str_check) {
		    if (document.getElementById(str_check).checked == false) {
		      document.getElementById(str_check).removeAttribute('checked');
		      //console.log(document.getElementById(str_check));
		    } else if (document.getElementById(str_check).checked == true) {
		      document.getElementById(str_check).setAttribute('checked', 'checked');
		      //document.getElementById(str_check).checked=true;
		      //console.log(document.getElementById(str_check));
		    }
		    document.getElementById("all").checked=false;
		    document.getElementById("none").checked=false;
		  }

		  function setRadioButtonGroup(str_check) {
		    if ( str_check == "none") {
		      document.getElementById(str_check).setAttribute('checked', 'checked');
		      document.getElementById(str_check).checked=true;
		      setRBtoAllFalse();
		    } else if ( str_check == "all") {
		      document.getElementById(str_check).setAttribute('checked', 'checked');
		      document.getElementById(str_check).checked=true;
		      setRBtoAllTrue();
		    } else if ( str_check == "diagnosis" ) {
		      document.getElementById(str_check).setAttribute('checked', 'checked');
		      document.getElementById(str_check).checked=true;
		      //console.log(document.getElementById('sem_tag1'));
		      setRadioButton([1,2,5]);
		      setRBtoFalse([3,4]);
		      setRBtoSomeFalse([6,semanticTags.length]);
		    } else if ( str_check == "procedure") {
		      document.getElementById(str_check).setAttribute('checked', 'checked');
		      document.getElementById(str_check).checked=true;
		      setRadioButton([3,4,5]);
		      setRBtoFalse([1,2]);
		      setRBtoSomeFalse([6,semanticTags.length]);
		    }
		  }

		  function setRadioButton(numbers) {
		    for (var num of numbers) {
		      document.getElementById('sem_tag'+num).setAttribute('checked', 'checked');
		      document.getElementById('sem_tag'+num).checked=true;
		    }
		  }

		  function setRBtoAllTrue() {
		    for (var i=1; i< semanticTags.length+1; i++) {
		        document.getElementById('sem_tag' + i).setAttribute('checked','checked');
		        document.getElementById('sem_tag' + i).checked=true;
		    }
		  }

		  function setRBtoAllFalse() {
		    for (var i=1; i< semanticTags.length+1; i++) {
		        document.getElementById('sem_tag' + i).removeAttribute('checked');
		        document.getElementById('sem_tag' + i).checked=false;
		    }
		  }

		  function setRBtoSomeFalse(numbers) {
		    for (var i=numbers[0]; i<=numbers[1]; i++) {
		        document.getElementById('sem_tag' + i).removeAttribute('checked');
		        document.getElementById('sem_tag' + i).checked=false;
		    }
		  }
		
		  function setRBtoFalse(numbers) {
		    for (var num of numbers) {
		        document.getElementById('sem_tag' + num).removeAttribute('checked');
		        document.getElementById('sem_tag' + num).checked=false;
		    }
		  }

  function ajax_call(str_type, str_URL, str_qs) {
       	/* AJAX 통신 처리 */
    	$http({
    		method: str_type, //방식
    		url: str_URL, /* 통신할 URL */
    		data: str_qs, /* 파라메터로 보낼 데이터 */
    		headers: {'Content-Type': 'application/json; charset=utf-8'} //헤더
    	})
    	.success(function(data, status, headers, config) {
    	  	if( data ) {
    	  		/* 성공적으로 결과 데이터가 넘어 왔을 때 처리 */
    	  		return data;
    	  	}
    	  	else {
    	  		/* 통신한 URL에서 데이터가 넘어오지 않았을 때 처리 */
    	  	}
    	})
    	.error(function(data, status, headers, config) {
    	  	/* 서버와의 연결이 정상적이지 않을 때 처리 */
    	  	console.log("fail" + " " + status);
    	});
  }

  
  
  function term_search() {

    var term = $('#term').val();
    var term1 = "";
    var term2 = "";
    var tokens = new Array();
    var token_array = [];
    var token_array_count = 0;
    var token_count = 0;

    var token_matching_count=0;
    var value_total="";
    var token_phrase=[];

    var checkboxes = document.getElementsByName("sem_tag");
    var checkboxes1 = document.getElementsByName("max");
    var checkboxes2 = document.getElementsByName("index_type");
    var qs_checkboxes = "";
    var qs_checkboxes1 = 5;
    var qs_checkboxes2 = "";
    var check_count = 0;
    var check_count2 = 0;

    var token_matching_flag = "false";
    var partial_matching_flag = "false";

    for(var i in checkboxes) {
      if(checkboxes[i].checked) {
        if (check_count == 0) {
          qs_checkboxes = "\"" + checkboxes[i].value + "\"";
          check_count++;
        } else {
          qs_checkboxes += "," + "\"" + checkboxes[i].value + "\"";
          check_count++;
        }
      }
    }

    for(var j in checkboxes2) {
      if(checkboxes2[j].checked) {
        if (check_count2 == 0) {
          qs_checkboxes2 = checkboxes2[j].value ;
          check_count2++;
        } else {
          qs_checkboxes2 += "," + checkboxes2[j].value;
          check_count2++;
        }
      }
    }

    if (term !== "") {
      var qs = "";
      var token_matching_total= new Array();

      // token

      // delete two or more spaces
      term1 = term.replace(/(^\s*)|(\s*$)/gi, "");
      term2 = term1.replace(/\s+/g, ' ');
      term = term2.replace(/\s*-\s*/gi,' ');

    //
    // partial mapping
    //
    qs_stop = qs1Parsing + term + qs2Parsing;
    var term_stop="";
    var token_stop = ajax_call("POST", "http://115.68.120.16:19210/snomedct_20200309_test/_analyze?pretty=true", qs_stop );
    //console.log(token_stop);
    for (var z in token_stop.responseJSON.tokens) {
      if (z == 0) {
        term_stop = token_stop.responseJSON.tokens[0].token;
      } else {
        term_stop += " " + token_stop.responseJSON.tokens[z].token;
      }
    }

    var i_count = 0;
    var value3 = new Array();

    // if
      var qs_stop = qs1 + qs2_semantic + qs_checkboxes + qs3_index + "stop"  + qs4_term + term_stop + qs5;
      //console.log(qs_stop);
      var token_partial_result = ajax_call("POST", "http://115.68.120.16:19210/snomedct_20200309_test/_search?pretty=true", qs_stop);
      //console.log(token_partial_result);
      for (var g in token_partial_result.responseJSON.hits.hits) {
        //if (token_partial_result.responseJSON.hits.hits[h]._score > 7) {
          value3[i_count] = new Array();
          value3[i_count][0] = token_partial_result.responseJSON.hits.hits[g]._score;
          value3[i_count][1] = token_partial_result.responseJSON.hits.hits[g]._source.conceptId;
          value3[i_count][2] = token_partial_result.responseJSON.hits.hits[g]._source.fsn;
          value3[i_count][3] = token_partial_result.responseJSON.hits.hits[g]._source.term;
          value3[i_count][4] = token_partial_result.responseJSON.hits.hits[g]._source.descriptionId;
          i_count++;
        //}
        //console.log(i_count);
      }
    //}

    //if (qs_checkboxes2.search("stop_syn") != -1) {
      //console.log("stop_syn");
      var qs_stop_synonym = qs1 + qs2_semantic + qs_checkboxes + qs3_index + "stop_synonym"  + qs4_term + term_stop + qs5;
      //console.log(qs_synonym);
      var token_partial_result = ajax_call("POST", "http://115.68.120.16:19210/snomedct_20200309_test/_search?pretty=true", qs_stop_synonym);
      //console.log(token_partial_result);
      for (var g in token_partial_result.responseJSON.hits.hits) {
        //if (token_partial_result.responseJSON.hits.hits[g]._score > 7) {
          value3[i_count] = new Array();
          value3[i_count][0] = token_partial_result.responseJSON.hits.hits[g]._score;
          value3[i_count][1] = token_partial_result.responseJSON.hits.hits[g]._source.conceptId;
          value3[i_count][2] = token_partial_result.responseJSON.hits.hits[g]._source.fsn;
          value3[i_count][3] = token_partial_result.responseJSON.hits.hits[g]._source.term;
          value3[i_count][4] = token_partial_result.responseJSON.hits.hits[g]._source.descriptionId;
          i_count++;
        //}
        //console.log(i_count);
      }
    //}

    //if (qs_checkboxes2.search("stop_edg") != -1) {
      //console.log("stop_edg");
      var qs_stop_edge = qs1 + qs2_semantic + qs_checkboxes + qs3_index + "stop_edge5" + qs4_term + term_stop + qs5;
      //console.log(qs_stop_edge);
      var token_partial_result = ajax_call("POST", "http://115.68.120.16:19210/snomedct_20200309_test/_search?pretty=true", qs_stop_edge);
      //console.log(token_partial_result);
      for (var g in token_partial_result.responseJSON.hits.hits) {
        //if (token_partial_result.responseJSON.hits.hits[g]._score > 7) {
          value3[i_count] = new Array();
          value3[i_count][0] = token_partial_result.responseJSON.hits.hits[g]._score;
          value3[i_count][1] = token_partial_result.responseJSON.hits.hits[g]._source.conceptId;
          value3[i_count][2] = token_partial_result.responseJSON.hits.hits[g]._source.fsn;
          value3[i_count][3] = token_partial_result.responseJSON.hits.hits[g]._source.term;
          value3[i_count][4] = token_partial_result.responseJSON.hits.hits[g]._source.descriptionId;
          i_count++;
        //}
      }
    //}

    //console.log(qs_checkboxes2);
    //console.log(i_count);

    for (var w=0; w < i_count; w++) {
      for (var y=w+1; y < i_count; y++) {
        if (value3[w][3] == value3[y][3]) {
          if (value3[w][0] > value3[y][0]) {
            value3[y][5] = "dup";
          } else {
            value3[w][5] = "dup";
          }
        }
      }
    }

    value3.sort(function(a,b) {
      return b[0]-a[0];
    });

    var k=0;
    value_total =
      //"<caption>- Mapping results -</caption>" +
      "<tbody style=\"font-size:11pt\">" +
        "<tr>" +
          "<th>Scores</th>" +
          "<th>Mapping terms</th>" +
          "<th>Fully Specified Name & Defining Relationship</th>" +
        "</tr>" ;

    for (var j in value3) {
      if ((value3[j][2] != value3[j][3]) && (value3[j][5] != "dup")) {
        var tmp = ajax_call("GET", "http://api.infoclinic.co/postexpr/SNOMEDCT/" + value3[j][1], "");
        partial_matching_flag = "true";
        if (term.toLowerCase() == value3[j][3].toLowerCase()) {
          value_total +=
            "<tr>" +
              "<td>" + value3[j][0] + "</td>" +
              "<td>" + value3[j][3] + "</td>" +
              "<td>" + "<a style=\"color:red\" href=\"http://term.infoclinic.co/#/browser/snomedct/" + value3[j][1] + "\""+ " target=\"_blank\">" + value3[j][1] + "</a>" +
                    " |" + "<span style=\"color:red\">" + value3[j][2] + "</span>" + "|" +
                    "<br />" + tmp.responseJSON.replace(/[0-9]+/g, "<a href=\"http://term.infoclinic.co/#/browser/snomedct/" + "$&" + "\""+ " target=\"_blank\">" + "$&" + "</a>") + "</td>" +
            "</tr>";
        } else {
          value_total +=
            "<tr style=\"color:black\">" +
              "<td>" + value3[j][0] + "</td>" +
              "<td>" + value3[j][3] + "</td>" +
              "<td>" + "<a href=\"http://term.infoclinic.co/#/browser/snomedct/" + value3[j][1] + "\""+ " target=\"_blank\">" + value3[j][1] + "</a>" + " |" + value3[j][2] + "|" +
                    "<br />" + tmp.responseJSON.replace(/[0-9]+/g, "<a href=\"http://term.infoclinic.co/#/browser/snomedct/" + "$&" + "\""+ " target=\"_blank\">" + "$&" + "</a>") + "</td>" +
            "</tr>";
        }
      }
    }
    value_total += "</tbody>";

    if (partial_matching_flag == "true") {
      $("#results_partial").show();
      document.getElementById("results_partial").innerHTML=value_total;
      partial_matching_flag = "false";
    } else {
      $("#results_partial").hide();
    }

  }
}

}());
