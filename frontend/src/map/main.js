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
import LinearProgress from '@material-ui/core/LinearProgress';

const StyledTableCell = withStyles((theme) => ({
  head: {
    backgroundColor: "#1e2d40",
    color: '#94a3b8',
    fontWeight: 700,
    fontSize: '0.75em',
  },
  body: {
    fontSize: 12,
  },
}))(TableCell);

const useStyles = makeStyles((theme) => ({
  container: {
    '-ms-overflow-style': 'none',
    scrollbarWidth: 'none',
    '&::-webkit-scrollbar': {
        display: 'none',
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

const BASE = '';

export default function Main(props) {

  const classes = useStyles();

  const [q, setQ] = useState('');
  const [value2, setValue2] = useState([]);
  const [value, setValue] = useState([]);
  const [val, setVal] = useState([]);
  const [tmp, setTmp] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (q.length > 2) {

      setTmp([]);
      setVal([]);
      setValue([]);
      setValue2([]);
      setLoading(true);

      const semanticTags = [];
      for (let seman in props.semanTag) {
        if (props.semanTag[seman].state) {
          semanticTags.push(props.semanTag[seman].name);
        }
      }

      axios
        .post(`${BASE}/map/SNOMEDCT/search`, {
          q: q.replace(/(^\s*)|(\s*$)/gi, "").replace(/\s+/g, ' ').replace(/\s*-\s*/gi,' '),
          semanticTags: semanticTags,
        })
        .then(response => {
          const hits = (response.data && response.data.hits) ? response.data.hits : [];
          const value3 = hits.map(h => [h.score, h.conceptId, h.fsn, h.term, null, 'ok']);
          setValue2(value3);
        });
    }
  }, [q, props.semanTag]);

  useEffect(() => {
    if (value2 && value2.length > 0) {
      let i_count2 = 0;
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
           .get(`${BASE}/postexpr/SNOMEDCT/${value2[j][1]}`)
           .then(response => {
             tmp2.push([ch, response.data]);
           })
          )
        }
      }

      // backend returns results in priority order (stop > synonym > edge)

      Promise.all(promises).then(() => setTmp(tmp2));
    }
  }, [value2]);

  useEffect(() => {
    if (tmp) {
      for (var l in value) {
        for (var m in tmp) {
          if (value[l][1] == tmp[m][0]) {
            value[l][4] = tmp[m][1];
          }
        }
      }
      setTimeout(function() {
        setVal(value);
        setLoading(false);
      }, 1500);
    }
  }, [tmp]);

  const handleQueryKeyUp = (event) => {
    if (window.event.keyCode === 13) {
      setQ(event.target.value);
    }
  };

  return (
    <>
    <FormControl className={classes.formControl}>
      <TextField className={classes.textfield} id="query" type="search" onKeyUp={handleQueryKeyUp}
        InputProps={{
          classes: { input: classes.tf },
          placeholder: 'At least 2 characters',
        }}
      />
    </FormControl>
    {loading && <LinearProgress style={{ margin: '0 14px 4px', borderRadius: 2 }} />}
    <Container
      className={classes.container}
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
              <col style={{width:'3%'}}/>
              <col style={{width:'22%'}}/>
              <col style={{width:'75%'}}/>
            </colgroup>
            <TableHead>
              <TableRow >
                <StyledTableCell className={classes.label}>
                  <strong>No.</strong>
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
                    {index + 1}
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
          <Typography style={{margin: "0 0 0 12px"}} variant="body2">[Usage] Check "Semantic Tag" -> Type "strings" which you want to map [and Enter] -> The matching terms of SNOMED CT will be displayed</Typography>
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
