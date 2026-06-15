import React, { useState, useEffect} from 'react';
import axios from 'axios';
import { withStyles, makeStyles } from '@material-ui/core/styles';
import Grid from "@material-ui/core/Grid";
import Divider from "@material-ui/core/Divider";
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import InputLabel from "@material-ui/core/InputLabel";
import Container from '@material-ui/core/Container';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Box from "@material-ui/core/Box";

const StyledTableCell = withStyles((theme) => ({
  head: {
    backgroundColor: "#f9f9f9", //"#e3f2fd",
    color: theme.palette.common.black,
  },
  body: {
    fontSize: 12,
  },
}))(TableCell);

const useStyles = makeStyles((theme) => ({
  container: {
    '-ms-overflow-style': 'none', /* IE and Edge */
    scrollbarWidth: 'none', /* Firefox */
    '&::-webkit-scrollbar': {
        display: 'none', /* Chrome, Safari, Opera*/
    },
  },
  typography: {
    padding: theme.spacing(1),
    marginTop: '1ch',
    marginBottom: '1ch',
  },
  label: {
    fontSize: '0.8em',
  },
  inputlabel: {
    minWidth: "10ch",
    fontSize: '0.8em',
    margin : "0 0 0 0",
    padding: "12px 0 0 12px",
  },
  textfield: {
    marginTop: theme.spacing(1),
    width: "60rem",
  },
  tf: {
    fontSize: "0.7em",
  },
  formControl: {
    margin: theme.spacing(1),
    alignItems : "center",
    margin : "0 0 0 0",
    padding: "12px 0 0 12px",
  },
}));

const qs1Parsing =
  "{" +
  "\"analyzer\" : \"standard\"," +
  "\"text\" : \"";

const qs2Parsing =
  "\"" +
"}";

const qs1 =
  "{" +
    "\"query\" : {" +
      "\"bool\": {" +
        "\"filter\": [" +
          "{\"script\":" +
            "{\"script\": \"doc[\'maxConceptDescriptionEffectiveTimeAndActive\'].value == (doc[\'conceptEffectiveTime\'].value + \'+\' + doc[\'descriptionEffectiveTime\'].value + \'+\' + \'1\');\"" +
            "}" +
          "}" +
          ",";


const qs2_semantic =
          "{\"terms\":" +
            "{\"semanticTag\": [";

const qs3_index =
            "]}" +
          "}" +
        "]," +
        "\"must\": {" +
          "\"match\" : {" +
            "\"term." ;

const qs4_term =
            "\" : {" +
              "\"query\" : \"";

const qs2_phrase =
            "]}" +
          "}" +
        "]," +
        "\"must\": {" +
          "\"match_phrase\" : {" +
            "\"term\" : {" +
                "\"query\" : \"";

const qs5 =
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

export default function Main(props) {

  const classes = useStyles();

  const [q, setQ] = useState('');
  const [qsCheckboxes, setQsCheckboxes] = useState('');
  const [tokenStop, setTokenStop] = useState([]);
  const [termStop, setTermStop] = useState("");
  const [stopResult, setStopResult] = useState([]);
  const [stopSynonymResult, setStopSynonymResult] = useState([]);
  const [stopEdge5Result, setStopEdge5Result] = useState([]);
  const [postexpr, setPostexpr] = useState([]);
  const [value2, setValue2] = useState([]);
  const [value, setValue] = useState([]);
  const [val, setVal] = useState([]);
  const [tmp, setTmp] = useState([]);

  let resp = "";

  useEffect(() => {
    if (q.length > 2) {

      setTmp([]);
      setVal([]);
      setValue([]);
      setValue2([]);
      setQsCheckboxes([]);

      let checkboxes = "";
      let check_count = 0;

      for(let seman in props.semanTag) {
        //console.log("props.semanTag : " + props.semanTag);
        if(props.semanTag[seman].state) {
          if (check_count == 0) {
            checkboxes = "\"" + props.semanTag[seman].name + "\"";
            check_count++;
          } else {
            checkboxes += "," + "\"" + props.semanTag[seman].name + "\"";
            check_count++;
          }
        }
      }

      setQsCheckboxes(checkboxes);

      let term = q.replace(/(^\s*)|(\s*$)/gi, "").replace(/\s+/g, ' ').replace(/\s*-\s*/gi,' ');

      axios
        .post(`http://115.68.120.16:19210/snomedct_test/_analyze?pretty=true`,
          qs1Parsing + term + qs2Parsing, {
            headers: {
              'Authorization': 'Basic aWNfYWRtaW46aW5mb2NsaW5pYzIh' // 인증 토큰 헤더 (예시)
            }
          }
        )
        .then(response => setTokenStop(response));

      //console.log("tokenStop : ", tokenStop);

    }
  },[q,  props.semanTag]);

  useEffect(() => {
    if (tokenStop.data) {
      let ts = '';

      for (let t in tokenStop.data.tokens) {
        if (t == 0) {
          ts = tokenStop.data.tokens[0].token;
        } else {
          ts += " " + tokenStop.data.tokens[t].token;
        }
      }

      setTermStop(ts);
      //console.log("termStop : ", termStop);
    }
  }, [tokenStop]);

  useEffect(() => {
    if ( termStop != "" && qsCheckboxes != "") {
      axios
        .post(`http://115.68.120.16:19210/snomedct_test/_search?pretty=true`,
          qs1 + qs2_semantic + qsCheckboxes + qs3_index + "stop"  + qs4_term + termStop + qs5,
          {
      headers: {
        'Authorization': 'Basic aWNfYWRtaW46aW5mb2NsaW5pYzIh' // 인증 토큰 헤더 (예시)
      }
    })
        .then(response => setStopResult(response));

      //console.log(qs1 + qs2_semantic + qsCheckboxes + qs3_index + "stop"  + qs4_term + termStop + qs5);
      //console.log("stopResult : ", stopResult);
    }
  }, [termStop, qsCheckboxes,])

  useEffect(() => {
    if ( termStop != "" && qsCheckboxes != "") {
      if (stopResult.data) {
        axios
          .post(`http://115.68.120.16:19210/snomedct_test/_search?pretty=true`,
            qs1 + qs2_semantic + qsCheckboxes + qs3_index + "stop_synonym"  + qs4_term + termStop + qs5,
          {
      headers: {
        'Authorization': 'Basic aWNfYWRtaW46aW5mb2NsaW5pYzIh' // 인증 토큰 헤더 (예시)
      }
    })
          .then(response => setStopSynonymResult(response));
        //console.log("stopSynonymResult : ", stopSynonymResult);
      }
    }
  },[stopResult])

  useEffect(() => {
    if ( termStop != "" && qsCheckboxes != "") {
      if (stopResult.data) {
        axios
          .post(`http://115.68.120.16:19210/snomedct_test/_search?pretty=true`,
            qs1 + qs2_semantic + qsCheckboxes + qs3_index + "stop_edge5" + qs4_term + termStop + qs5,
          {
      headers: {
        'Authorization': 'Basic aWNfYWRtaW46aW5mb2NsaW5pYzIh' // 인증 토큰 헤더 (예시)
      }
    })
          .then(response => setStopEdge5Result(response));
        //console.log("stopEdge5Result : ", stopEdge5Result);
      }
    }
  },[stopSynonymResult])

  useEffect(() => {
    if (stopEdge5Result.data) {
      let value3 = [];
      let i_count = 0;

      if ( stopResult.data) {
        for (let t in stopResult.data.hits.hits) {
          //console.log("stopResult => " + t + stopResult.data.hits.hits[t]._source.term);
          value3[i_count] = new Array();
          value3[i_count][0] = stopResult.data.hits.hits[t]._score;
          value3[i_count][1] = stopResult.data.hits.hits[t]._source.conceptId;
          value3[i_count][2] = stopResult.data.hits.hits[t]._source.fsn;
          value3[i_count][3] = stopResult.data.hits.hits[t]._source.term;
          value3[i_count][4] = null;
          value3[i_count][5] = 'ok';
          i_count++;
        }
      }

      if ( stopSynonymResult.data) {
        for (let t in stopSynonymResult.data.hits.hits) {
          //console.log("stopSynonymResult => " + t +stopSynonymResult.data.hits.hits[t]._source.term);
          value3[i_count] = new Array();
          value3[i_count][0] = stopSynonymResult.data.hits.hits[t]._score;
          value3[i_count][1] = stopSynonymResult.data.hits.hits[t]._source.conceptId;
          value3[i_count][2] = stopSynonymResult.data.hits.hits[t]._source.fsn;
          value3[i_count][3] = stopSynonymResult.data.hits.hits[t]._source.term;
          value3[i_count][4] = null;
          value3[i_count][5] = 'ok';
          i_count++;
        }
      }

      if ( stopEdge5Result.data) {
        for (let t in stopEdge5Result.data.hits.hits) {
          //console.log("stopEdge5Result => " + t + stopEdge5Result.data.hits.hits[t]._source.term);
          value3[i_count] = new Array();
          value3[i_count][0] = stopEdge5Result.data.hits.hits[t]._score;
          value3[i_count][1] = stopEdge5Result.data.hits.hits[t]._source.conceptId;
          value3[i_count][2] = stopEdge5Result.data.hits.hits[t]._source.fsn;
          value3[i_count][3] = stopEdge5Result.data.hits.hits[t]._source.term;
          value3[i_count][4] = null;
          value3[i_count][5] = 'ok';
          i_count++;
        }
      }

      for (let w=0; w < i_count; w++) {
        for (let y=w+1; y < i_count; y++) {
          if (value3[w][3] == value3[y][3]) {
            if (value3[w][0] >= value3[y][0]) {
              value3[y][5] = "dup";
            } else {
              value3[w][5] = "dup";
            }
          }
        }
      }
      
      setValue2(value3);
    }
  }, [stopEdge5Result]);

  useEffect(() => {
    if (value2) {
      let i_count2=0;
      let tmp2 = [];
      let promises = [];
      for (var j in value2) {
        let ch;
        if ((value2[j][2] != value2[j][3]) && (value2[j][5] != "dup")) {
          ch = value2[j][1];
          value[i_count2] = value2[j];
          i_count2++;
          promises.push(
           axios
           .get(`/postexpr/SNOMEDCT/${value2[j][1]}`)
           .then(response => {
             tmp2.push([ch, response.data]);
           })
          )
        }
      }

      value.sort(function(a,b) {
        return b[0]-a[0];
      });
      
      Promise.all(promises).then(() => setTmp(tmp2));
    }
  },[value2]);

  useEffect(() => {
    if (tmp) {
      //console.log("useEffect : tmp")
      for (var l in value) {
        for ( var m in tmp) {
          if (value[l][1] == tmp[m][0]) {
            value[l][4] = tmp[m][1];
            //continue;
          }
        }
      }
      setTimeout(function() {
        setVal(value);
      }, 1500);
      
    }
  },[tmp]);

  
  useEffect(() => {
    if (val) {
      //console.log("useEffect : val")
    }
  }, [val]);

  const handleQueryKeyUp = (event) => {
    if (window.event.keyCode === 13) {
      setQ(event.target.value);
    }
  };

  //console.log("qsCheckboxes : " + qsCheckboxes);
  //console.log("value2: ", value2);
  //console.log("tmp : ", tmp);
  //console.log("value: ", value);
  //console.log("val: ", val);
 

  return (
    <>
    <FormControl className={classes.formControl}>
      <InputLabel shrink className={classes.inputlabel} id="queryLabel">At least 2 more characters
      </InputLabel>
      <TextField labelid="queryLabel" className={classes.textfield} id="query" type="search" onKeyUp={handleQueryKeyUp}
        InputProps={{
          classes: {
            input: classes.tf,
          },
        }}
      />
    </FormControl>
    <Container
      className={classes.container} /*ref={setRef}*/
      style={{
        margin : "0 0 0 0",
        padding: "12px 0 0 0",
        height: "85vh",
        overflow: "scroll"}}>
    { val && val[0]
      ? (
        <Box p={1}>
        <TableContainer align="center">
          <Table stickyHeader aria-label="sticky table" size="small" aria-label="a small table">
            <colgroup>
              <col style={{width:'5%'}}/>
              <col style={{width:'20%'}}/>
              <col style={{width:'75%'}}/>
            </colgroup>
            <TableHead>
              <TableRow >
                <StyledTableCell className={classes.label}>
                  <strong>Scores</strong>
                </StyledTableCell>
                <StyledTableCell className={classes.label}>
                  <strong>Mapped term</strong>
                </StyledTableCell>
                <StyledTableCell className={classes.label}>
                  <strong>Fully Specified Name & Defining Relationship</strong>
                </StyledTableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              { val.map((v, index) => (
                <TableRow key={index}>
                  <StyledTableCell className={classes.label}>
                    {v[0]}
                  </StyledTableCell>
                  <StyledTableCell className={classes.label}>
                    {v[3]}
                  </StyledTableCell>
                  <StyledTableCell className={classes.label}>
                    {v[1]} | {v[2] }|
                    <br/>
                    {v[4]}
                  </StyledTableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
        </Box>
      ):(
        <>
        { !q &&
          <>
          <Typography style={{margin: "0 0 0 12px"}} variant="body2">[Usage] Check "Semantic Tag" -> Type "strings" which you want to map [and Enter] -> The Closest terms of SNOMED CT will be displayed (Up to 40 terms)</Typography>
          <br />
          <Typography style={{margin: "0 0 0 12px"}} variant="body2">[Note 1] Available browser : Chrome, Safari</Typography>
          <br />
          <Typography style={{margin: "0 0 0 12px"}} variant="body2">[Note 2 to Chrome User] if you got  a CORS error, you need to install Chrome extension ("Allow-Control-Allow-Origin:*") at <a href="https://chrome.google.com/webstore/category/extensions">https://chrome.google.com/webstore/category/extensions</a></Typography>
          </>
        }
        </>
      )
    }
    </Container>
    </>
  );
}


