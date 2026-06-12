import React, { useState, useEffect, useRef} from 'react';
import axios from 'axios';
import { withStyles, makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';

import Grid from "@material-ui/core/Grid";
import Box from "@material-ui/core/Box";
import Divider from "@material-ui/core/Divider";
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import Table from '@material-ui/core/Table';
import TablePagination from '@material-ui/core/TablePagination';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import {Star, StarBorder} from '@material-ui/icons';
import Container from '@material-ui/core/Container';
import { BrowserRouter as Link} from "react-router-dom";
import FormControl from '@material-ui/core/FormControl';
import TextField from '@material-ui/core/TextField';
import Select from "@material-ui/core/Select";
import InputLabel from "@material-ui/core/InputLabel";

const StyledTableCell = withStyles((theme) => ({
  head: {
    backgroundColor: "#f9f9f9", //"#e3f2fd",
    color: theme.palette.common.black,
  },
  body: {
    fontSize: 11,
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
  lineheight: {
    lineHeight: 2,
  },
  alertWarning: {
    backgroundImage: 'linear-gradient(to bottom,#f7edb5 0,#f5e79e 100%)',
    backgroundRepeat: 'repeat-x',
    color: '#8a6d3b',
    backgroundColor: '#fcf8e3',
    borderColor: '#f5e79e',

  },
  badge: {
    display: 'inline-block',
    minWidth: '10px',
    padding: '3px 7px',
    fontSize: '12px',
    fontWeight: 'bold',
    lineHeight: '1',
    textAlign: 'center',
    whiteSpace: 'nowrap',
    verticalAlign: 'baseLine',
    borderRadius: '10px',
  },
  label: {
    fontSize: '0.8em',
  },
  inputlabel: {
    minWidth: "10ch",
    fontSize: '0.8em',
    padding: "12px 0 0 12px",
  },
  textfield: {
    marginTop: theme.spacing(1),
    fontSize: '0.8em',
    width: "20rem",
  },
  form: {
    padding: "0 0 0 12px",
  },
  gridcontainer: {
    height: '100vh',
  },
  divider: {
    borderBottom: "solid 2px #2196F3",
  },
  tooltip:{
    color: '#fffff',
  },
  flagIcon : {
    position: 'relative',
    display: 'inline-block',
    backgroundRepeat: 'no-repeat',
    backgroundPosition: '50%',
  },
  link: {
    textDecoration: "none",
    color: '#000',
    /* '&:hover': {
      color: '#3a87ad',
    }, */
  },
  boxHover: {
    /* 마우스 오버(마우스 올렸을때) */
    '&:hover' : {
        backgroundColor: "#dce6f0",

    },
  },
  boxActive: {
    /* 마우스 클릭하고있을때 */
    '&:active' : {
        backgroundColor: "#777",
    },
  },
  boxVisited: {
    /* 마우스 한번클릭후 */
    '&:visited' : {
        color: "white",
    },
  },
}));

export default function Main(props) {

  const classes = useStyles();

  const [q, setQ] = useState('*');
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(15);
  const [refsetTitle, setRefsetTitle] = useState(props.refset.name);
  const [descript, setDescript] = useState([]);
  const [member, setMember] = useState([]);
  const [refset, setRefset] = useState([]);
  const [refset2,setRefset2] = useState([]);
  const flag = useRef("start");

  const refsetDescriptor = '900000000000456007';

  useEffect(() => {
    setQ('*');
    setPage(1);
    setSize(15);
    axios
      .get(`/members/SNOMEDCT?refcpntid=${props.refset.id}`)
      .then(response => setDescript(response))
      .catch((ex) => { console.log('No descriptor', ex); });
  }, [props.refset.id])

  useEffect(() => {
    //setQ('*');
    axios
      .get(`/members/SNOMEDCT/${props.refset.id}?q=${q}&page=${page}&size=${size}`)
      .then( (response) => { flag.current = "hasMemebr"; setMember(response) })
      .catch((ex) => { flag.current = "noMember";  setMember([]); console.log('No members', ex); });
  }, [descript])

  useEffect(() => {
    setRefset2([]);

    let order;
    let ref = [];
    ref[refsetDescriptor] = [];

    // if (!descript.data) return null;
    // setTimeout(() => { }, 3000);

    if (descript.data) {
      console.log('descript.data', descript.data);
      descript.data.forEach((item, index, desc) => { 
	if (desc[index].extra !== undefined) { // extra가 없는 경우가 있음
          if (desc[index].extra["Attribute order"]) {
              order = desc[index].extra["Attribute order"].id;
              ref[refsetDescriptor][order] = [];

            for (let des in desc[index].extra) {
              ref[refsetDescriptor][order].push({
                "title": des,
                "id" : desc[index].extra[des].id,
                "name" : desc[index].extra[des].name
              })
            }
          }
        }
      })
      console.log(ref);
      setRefset2(ref);
    }
  },[descript])

  useEffect(() => {
    //setQ('*');
    setPage(1);
    setSize(15);
    axios
      .get(`/members/SNOMEDCT/${props.refset.id}?q=${q}&page=${page}&size=${size}`)
      .then( (response) => { flag.current = "hasMemebr"; setMember(response) })
      .catch((ex) => { flag.current = "noMember"; setMember([]); console.log('No members', ex); });
  }, [q])

  useEffect(() => {
    //setQ('*');
    axios
      .get(`/members/SNOMEDCT/${props.refset.id}?q=${q}&page=${page}&size=${size}`)
      .then( (response) => { flag.current = "hasMemebr"; setMember(response) })
      .catch((ex) => { flag.current = "noMember"; setMember([]); console.log('No members', ex); });
  }, [page, size])

  useEffect(() => {
    setRefset([]);
    if ( member.data ) {
      if (member.data.totalElements !== 0) {
        let fs;
        let ref=[];
	props.refset.desc = 0; // 계층구조에서 leaf 가 아니어도 멤버를 가지면 화면출력이 가능하도록 함 
        ref[props.refset.id] = [];
        member.data.content.forEach((item, index, mem) => {
          ref[props.refset.id][index] = [];
          if (refset2[refsetDescriptor]) {
            if (refset2[refsetDescriptor]["0"]) {
              ref[props.refset.id][index].push({
                "title": (refset2[refsetDescriptor]["0"]["1"].name.split('('))[0],
                "id": member.data.content[index].referencedComponent.id,
                "name": member.data.content[index].referencedComponent.name,
              });
              let c=1;
              for (let me in mem[index].fields) {
                if (refset2[refsetDescriptor][c]) {
                  ref[props.refset.id][index].push({
                    "title": (refset2[refsetDescriptor][c]["1"].name.split('('))[0],
                    "id" : mem[index].fields[me].id,
                    "name" : mem[index].fields[me].name
                  });
                } else {
                  ref[props.refset.id][index].push({
                    "title": '',
                    "id" : mem[index].fields[me].id,
                    "name" : mem[index].fields[me].name
                  });
                }
                c++;
              }
            }
          }
        })
        // console.log(ref);
        setRefset(ref);
      } else {
        flag.current = "noMember"
      }
    } else {
      flag.current = "noMember"
    }
  }, [member])

  const handleChangePage = (event, newPage) => {
    setPage(newPage+1);
  };

  const handleChangeRowsPerPage = (event) => {
    //console.log(event.target.value);
    setSize(event.target.value);
    setPage(1);
  };

  const handleQueryKeyUp = (event) => {
    if (window.event.keyCode === 13) {
      setQ(event.target.value);
    }
  };

  //console.log("props.refset.desc : " + props.refset.desc);
  //console.log("refset[props.refset.id] : " + refset[props.refset.id]);
  //console.log("member.data : " + member.data);

  return (
    <div>
      { props.refset &&
          <Grid item md={12} >
            <Box p={1}>
              <Typography variant="body2">
                <b>{props.refset.name}</b>
              </Typography>
            </Box>
            <Divider className={classes.divider}/>
            { props.refset.desc === 0 &&
            <>
              { refset[props.refset.id] && member.data && member.data.totalElements
                ? (
                <>
                <FormControl classes={{root: classes.form}}>
                  <InputLabel shrink
                    className={classes.inputlabel}
                    id="queryLabel">Word OR match (* for all)
                  </InputLabel>
                  <TextField
                    labelid="queryLabel"
                    className={classes.textfield}
                    InputProps={{
                      classes: {
                        input: classes.textfield,
                      },
                    }}
                    id="query"
                    type="search"
                    onKeyUp={handleQueryKeyUp}
                  />
                </FormControl>
                <br/>
                <Box p={1}>
                  <TableContainer p={1} align="center">
                    <Table size="small" aria-label="a small table">
                      <TableHead>
                        <TableRow >
                          { refset[props.refset.id][0].map((rs0, index) => (
                          <StyledTableCell key={index} className={classes.label}>
                            {rs0.title}
                          </StyledTableCell>
                          ))}
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        { refset[props.refset.id].map((rs,index2) => (
                        <TableRow key={index2}>
                          { rs.map((r,index3) => (
                            <StyledTableCell key={index3}  >
                            { r.id && r.name && 
			      <div>
                                {r.id} | {r.name} |
                              </div>
                            }
			    { r.id && !r.name &&
                              <div>
                                {r.id}
                              </div>
                            }
                            { !r.id  && r.name && 
			      <div>
                                {r.name}
                              </div>
                            }
                            </StyledTableCell>
                          ))}
                        </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                  <TablePagination
                    rowsPerPageOptions={[5, 10, 15]}
                    component="div"
                    count={member.data.totalElements}
                    page={page-1}
                    rowsPerPage={size}
                    onChangePage={handleChangePage}
                    onChangeRowsPerPage={handleChangeRowsPerPage}
                  />
                </Box>
                </>
                ):(
                  <>
                  { q !== '*'
                    ? (
                      <>
                      <Box p={1}>
                        <FormControl classes={{root: classes.form}}>
                          <InputLabel shrink
                            className={classes.inputlabel}
                            id="queryLabel">Word OR match (* for all)
                          </InputLabel>
                          <TextField
                            labelid="queryLabel"
                            className={classes.textfield}
                            InputProps={{
                              classes: {
                                input: classes.textfield,
                              },
                            }}
                            id="query"
                            type="search"
                            onKeyUp={handleQueryKeyUp}
                            />
                        </FormControl>
                      </Box>
                      <p style={{padding:'0 0 0 12px' }}>
                        No members !
                      </p>
                      </>
                    ):(
                      <>
                      { flag.current === "noMember" &&
                        <p style={{padding:'0 0 0 12px' }}>
                              No members !
                        </p>
                      }
                      </>
                    )
                  }
                  </>
                )
              }
            </>
            }
          </Grid>

      }
    </div>
  );
}

