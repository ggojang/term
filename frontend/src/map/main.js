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

// ES 상수 제거: 백엔드 /map/SNOMEDCT/analyze, /map/SNOMEDCT/search API로 대체

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

      // ── 토큰 분석 (ES _analyze → /map/SNOMEDCT/analyze) ──
      axios
        .get(`/map/SNOMEDCT/analyze?q=${encodeURIComponent(term)}`)
        .then(response => setTokenStop(response));
    }
  },[q, props.semanTag]);

  useEffect(() => {
    if (tokenStop.data) {
      let ts = '';
      const tokens = tokenStop.data.tokens || [];
      for (let t = 0; t < tokens.length; t++) {
        ts += (t === 0 ? '' : ' ') + tokens[t].token;
      }
      setTermStop(ts);
    }
  }, [tokenStop]);

  useEffect(() => {
    if (termStop !== "" && qsCheckboxes !== "") {
      // ── 통합 검색 (ES _search × 3 → /map/SNOMEDCT/search × 1) ──
      const semTags = qsCheckboxes
        ? qsCheckboxes.replace(/"/g, '').split(',').filter(s => s.length > 0)
        : [];

      axios
        .post(`/map/SNOMEDCT/search`, {
          q: termStop,
          semanticTags: semTags,
          state: 'active',
          size: 20,
          page: 1
        })
        .then(response => {
          // 백엔드 응답: { hits: [{conceptId, fsn, term, ...}] }
          // value3 배열로 변환 (기존 코드 호환)
          const hits = (response.data && response.data.hits) ? response.data.hits : [];
          let value3 = hits.map((h, idx) => [idx, h.conceptId, h.fsn, h.term, null, 'ok']);
          setValue2(value3);
        });

      setStopResult({ data: null });
      setStopSynonymResult({ data: null });
      setStopEdge5Result({ data: { dummy: true } });
    }
  }, [termStop, qsCheckboxes]);

  useEffect(() => {}, [stopResult]);
  useEffect(() => {}, [stopSynonymResult]);

  useEffect(() => {
    if (stopEdge5Result.data) {
      // value2는 위 통합 검색 useEffect에서 이미 setValue2로 설정됨
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


