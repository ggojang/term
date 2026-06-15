import React, {useState, useEffect} from 'react';
import axios from 'axios';
import useAsync from "../useAsync.js";
import img from "./img/loinc-logo-tmp.png"
import { withStyles, makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';

import Grid from "@material-ui/core/Grid";
import Box from "@material-ui/core/Box";
import Divider from "@material-ui/core/Divider";
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import Table from '@material-ui/core/Table';
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
import Breadcrumbs from '@material-ui/core/Breadcrumbs';

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
  label: {
    fontSize: '0.8em',
  },
  labelPath: {
    fontSize: '0.8em',
  },
  gridcontainer: {
    //height: '100vh',
  },
  divider: {
    borderBottom: "solid 2px #2196F3",
  },
  gridBorder: {
    borderRight: "dotted 1px lightGray",
    borderLeft: "dotted 1px lightGray",
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

  const [id, setId] = useState('');
  const [codeType, setCodeType] = useState('');
  const [classType, setClassType] = useState('');
  //const [panelType, setPanelType] = useState('');
  const [FSN, setFSN] = useState('');
  const [names, setNames] = useState([]);
  const [tmp, setTmp] = useState('');

  const [entity, setEntity] = useState([]);
  const [path, setPath] = useState([]);
  const [LP, setLP] = useState([]);
  const [LPLink, setLPLink] = useState([]);
  const [LPMap, setLPMap] = useState([]);
  const [LA, setLA] = useState([]);
  const [LGTerm, setLGTerm] = useState([]);
  const [LV, setLV] = useState([]);
  const [panel, setPanel] = useState([]);

  useEffect(() => {
    if (props.loincId) {
        setEntity([]);
        setPath([]);
        setLP([]);
        setLPLink([]);
        setLPMap([]);
        setLA([]);
        setLGTerm([]);
        setLV([]);
        setPanel([]);

        axios
          .get(`/paths/LOINC/${props.loincId}`)
          .then(response => setPath(response))

    }
  },[props.loincId]);

  useEffect(() => {
    if (path.data && path.data[0]) {
      setFSN(path.data[0].prefName);
      if (props.loincId.substring(0,2) === 'LP') {
        setCodeType('LP');
        axios
          .get(`/LP/LOINC/${props.loincId}`)
          .then(response => setLP(response));
        axios
          .get(`/LPLINK/LOINC/${props.loincId}`)
          .then(response => setLPLink(response));
        axios
          .get(`/LPMAP/LOINC/${props.loincId}`)
          .then(response => setLPMap(response));
      } else {
        setCodeType('');
        axios
          .get(`/entity/LOINC/${props.loincId}`)
          .then(response => setEntity(response));
        axios
          .get(`/LPLINK/LOINC/${props.loincId}`)
          .then(response => setLPLink(response));
        axios
          .get(`/LA/LOINC/${props.loincId}`)
          .then(response => setLA(response));
        axios
          .get(`/LGTERM/LOINC/${props.loincId}`)
          .then(response => setLGTerm(response));
        axios
          .get(`/panel/LOINC/${props.loincId}`)
          .then(response => setPanel(response));
        axios
          .get(`/LV/LOINC/${props.loincId}`)
          .then(response => setLV(response));
      }
    }
  }, [path]);

  useEffect(() => {
    if (entity.data) {
      if (entity.data.clsType === 'Lab')
        setClassType('Laboratory');

      if (entity.data.panelType === '')
        props.setPanelType('');
      else
        props.setPanelType(entity.data.panelType);

      if (entity.data.relatedNames)
        setNames(entity.data.relatedNames.split(';'));
    }
  }, [entity]);

  /* 
  console.log("codeType => " + codeType);
  console.log("entity.data => " + JSON.stringify(entity.data));
  console.log("LPLink.data => " + JSON.stringify(LPLink.data));
  console.log("LV.data => " + JSON.stringify(LV.data));
  */

  return (
    <>
      { !props.loincId &&
        <Grid
          container
          spacing={0}
          direction="column"
          alignItems="center"
          justify="center"
        >
          <br />
          <Grid item md={12}>
            <img src={img} />
          </Grid>
          <Grid item md={12}>
            <h1>LOINC v2.82</h1>
          </Grid>
          <Grid item md={12}>
            <h4>Released: 2026-02-24</h4>
          </Grid>
          <Grid item md={12}>
            <h4>License from Regenstrief Institute, Inc. </h4>
          </Grid>
        </Grid>
      }
      <Grid container className={classes.gridcontainer} spacing={0} >
        <Grid item className={classes.gridBorder} p={0} m={0} md={12}>
          { path.data && path.data[0] &&
            <>
              <Box p={1}>
                <Typography component="span" variant="body1"><b>{path.data[0].code}&nbsp;{path.data[0].prefName}&nbsp;</b></Typography>
                { entity.data && entity.data.status &&
                  <Typography component="span" style={{backgroundColor:"lightgreen"}} variant="body2">{entity.data.status}</Typography>
                }
              </Box>

              <Box p={1} style={{backgroundColor:"#f5f5f5"}}>
              { path.data.map((p,index) => (
                  <Breadcrumbs key={index}>
                    <Typography className={classes.labelPath}>{p.path}</Typography>
                    <Typography className={classes.labelPath}>{p.prefName}</Typography>
                  </Breadcrumbs>
              ))}
              </Box>
            </>
          }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  codeType && LPMap.data && LPMap.data[0] &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>Loinc Part Related Code Mapping</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                    <StyledTableCell className={classes.label} >Part Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Part Type Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Ext Code Id</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Ext Code Display Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Ext Code System</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Equivalence</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Content Origin</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Ext Code System Version</StyledTableCell>
                  </TableRow>
                </TableHead>

                <TableBody>
                  { LPMap.data.map((map,index) => (
                  <TableRow key={index}>
                    <StyledTableCell className={classes.label} width="15%" component="th" scope="row">
                      {map.partName}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="15%" align="center">
                      {map.partTypeName}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="10%" align="center">
                      { map.extCodeId }
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="10%" align="left">
                      { map.extCodeDisplayName }
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="15%" align="left">
                      { map.extCodeSystem }
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="10%" align="left">
                      { map.equivalence }
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="5%" align="left">
                      { map.contentOrigin }
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="20%" align="left">
                      { map.extCodeSystemVersion }
                    </StyledTableCell>
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  codeType && LPLink.data && LPLink.data[0] &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>Loinc Part Link</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                    <StyledTableCell className={classes.label} >Part Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Part Type Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Link Type Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Loinc Number</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Long Common Name</StyledTableCell>
                  </TableRow>
                </TableHead>

                <TableBody>
                  { LPLink.data.map((link,index) => (
                  <TableRow key={index}>
                    <StyledTableCell className={classes.label} width="15%" component="th" scope="row">
                      {link.partName}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="10%" align="center">
                      {link.partTypeName}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="15%" align="center">
                      { link.linkTypeName }
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="10%" align="left">
                      { link.loincNumber }
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} width="50%" align="left">
                      { link.longCommonName }
                    </StyledTableCell>
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  !codeType && entity.data && LPLink.data && LPLink.data[0] &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>Loinc Part</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                    <StyledTableCell className={classes.label} >Part Number</StyledTableCell>
                    <StyledTableCell className={classes.label} >Part Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Part Type Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Link Type Name</StyledTableCell>
                  </TableRow>
                </TableHead>

                <TableBody>
                  { LPLink.data.map((link, index) => (
                  <TableRow key={index}>
                    <StyledTableCell className={classes.label} width="20%" component="th" scope="row">
                      {link.partNumber}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} >
                      {link.partName}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      {link.partTypeName}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      { link.linkTypeName }
                    </StyledTableCell>
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  !codeType && entity.data && entity.data.relatedNames &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>General Information</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableRow>
                    <StyledTableCell rowSpan={2} className={classes.label} component="th" scope="row" align="center">
                      <strong>Synonyms</strong>
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} >
                      <strong>ShortName</strong>:{entity.data.shortName}
                    </StyledTableCell>
                  </TableRow>
                  <TableRow>
                    <StyledTableCell className={classes.label} >
                      <strong>LongCommonName</strong>: { entity.data.longName }
                    </StyledTableCell>
                  </TableRow>
                  <TableRow>
                    <StyledTableCell className={classes.label} align="center">
                      <strong>Parts</strong>
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      <TableContainer align="center">
                        <Table size="small" aria-label="a small table">
                          <TableHead>
                            <TableRow >
                              <StyledTableCell className={classes.label} >Component</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Property</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Time</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">System</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Scale</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Method</StyledTableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            <TableRow>
                              <StyledTableCell className={classes.label} >{entity.data.component}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.property}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.time}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.system}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.scale}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.method}</StyledTableCell>
                            </TableRow>
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </StyledTableCell>
                  </TableRow>
                  <TableRow>
                    <StyledTableCell className={classes.label} align="center">
                      <strong>Class</strong>
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      <TableContainer align="center">
                        <Table size="small" aria-label="a small table">
                          <TableHead>
                            <TableRow >
                              <StyledTableCell className={classes.label} >Type</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Name</StyledTableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            <TableRow>
                              <StyledTableCell className={classes.label} >{entity.data.clsType}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.clsName}</StyledTableCell>
                            </TableRow>
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </StyledTableCell>
                  </TableRow>
                  <TableRow>
                    <StyledTableCell className={classes.label} align="center">
                      <strong>Changes</strong>
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      <TableContainer align="center">
                        <Table size="small" aria-label="a small table">
                          <TableHead>
                            <TableRow >
                              <StyledTableCell className={classes.label} >Date of Last Change</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Change Type</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Reason of Change</StyledTableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            <TableRow>
                              <StyledTableCell className={classes.label} >{entity.data.lastChngdVer}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.chngType}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.chngReasonPublic}</StyledTableCell>
                            </TableRow>
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </StyledTableCell>
                  </TableRow>
                  <TableRow>
                    <StyledTableCell className={classes.label} align="center">
                      <strong>Order vs. Obs</strong>
                    </StyledTableCell>
                    <StyledTableCell className={classes.label}>
                      {entity.data.orderObs}
                    </StyledTableCell>
                  </TableRow>
                  <TableRow>
                    <StyledTableCell className={classes.label} align="center">
                      <strong>Ranking</strong>
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      <TableContainer align="center">
                        <Table size="small" aria-label="a small table">
                          <TableHead>
                            <TableRow >
                              <StyledTableCell className={classes.label} >Common Test Rank</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Common Sl Test Rank</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Common Order Rank</StyledTableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            <TableRow >
                              <StyledTableCell className={classes.label} >{entity.data.cmnTestRnk}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.cmnSiTestRnk}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.cmnOrderRnk}</StyledTableCell>
                            </TableRow >
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </StyledTableCell>
                  </TableRow>
                  <TableRow>
                    <StyledTableCell className={classes.label} align="center">
                      <strong>RelatedNames</strong>
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      <TableContainer align="center">
                        <Table size="small" aria-label="a small table">
                          <TableHead>
                            <TableRow >
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            { names.map((name, index) => (
                              <TableRow key={index}>
                                <StyledTableCell className={classes.label} >{names[index]}</StyledTableCell>
                                <StyledTableCell className={classes.label} align="center">{names[index+1]}</StyledTableCell>
                                <StyledTableCell className={classes.label} align="center">{names[index+2]}</StyledTableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </StyledTableCell>
                  </TableRow>
                  <TableRow>
                    <StyledTableCell className={classes.label} align="center">
                      <strong>Units</strong>
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      <TableContainer align="center">
                        <Table size="small" aria-label="a small table">
                          <TableHead>
                            <TableRow >
                              <StyledTableCell className={classes.label} >Units and Range</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">Example UCUM Units</StyledTableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            <TableRow >
                              <StyledTableCell className={classes.label} >{entity.data.unitsAndRange}</StyledTableCell>
                              <StyledTableCell className={classes.label} align="center">{entity.data.exUcumUnits}</StyledTableCell>
                            </TableRow >
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </StyledTableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  !codeType && LA.data && LA.data[0] &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>Preferred Answer List Code</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                    <StyledTableCell className={classes.label} >Answer List ID</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Answer List Name</StyledTableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  { LA.data.map((la, index) => (
                  <TableRow key={index}>
                    <StyledTableCell className={classes.label} width="15%" component="th" scope="row">
                      {la.laid}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      {la.laname}
                    </StyledTableCell>
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  !codeType && LA.data && LA.data[0] &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>{LA.data.laid} Answer List</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                    <StyledTableCell className={classes.label} >Local Answer Code</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Answer String ID</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Display Text</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Ext Code ID</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Ext Code Display Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Ext Code System</StyledTableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  { LA.data[0].la.map((la, index) => (
                  <TableRow key={index}>
                    <StyledTableCell className={classes.label} width="15%" component="th" scope="row">
                      {la.localAnswerCode}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      {la.answerStringID}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      {la.displayText}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      {la.extCodeID}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      {la.extCodeDisplayName}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      {la.extCodeSystem}
                    </StyledTableCell>
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  !codeType && LGTerm.data && LGTerm.data[0] &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>Member of these Group</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                    <StyledTableCell className={classes.label} >LG ID</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">LG ID Name</StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">Category</StyledTableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  { LGTerm.data.map((lg, index) => (
                  <TableRow key={index}>
                    <StyledTableCell className={classes.label} width="15%" component="th" scope="row">
                      {lg.lgid}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label}>
                      {lg.lgidName}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} align="center">
                      {lg.category}
                    </StyledTableCell>
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  !codeType && entity.data && path.data && path.data[0] && panel.data && panel.data.panel && panel.data.panel.children && panel.data.panel.children[0] &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>Panel Hierarchy</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                    <StyledTableCell className={classes.label} >{entity.data.code}</StyledTableCell>
                    <StyledTableCell className={classes.label} >{path.data[0].prefName}</StyledTableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  { panel.data.panel.children.map((child, index) => (
                  <TableRow key={index}>
                    <StyledTableCell className={classes.label} width="20%" component="th" scope="row">
                      &emsp;&emsp;{child.code}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} >
                      &emsp;&emsp;{child.name}
                    </StyledTableCell>
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  !codeType && panel.data && panel.data.memberOfThesePanels && panel.data.memberOfThesePanels[0] &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>Member of these Panel</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                    <StyledTableCell className={classes.label} >Code</StyledTableCell>
                    <StyledTableCell className={classes.label} >Name</StyledTableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  { panel.data.memberOfThesePanels.map((panel, index) => (
                  <TableRow key={index}>
                    <StyledTableCell className={classes.label} width="15%" component="th" scope="row">
                      {panel.id}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} >
                      {panel.name}
                    </StyledTableCell>
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
        {  !codeType && LV.data && LV.data[0] &&
          <>
            <Box p={1}>
              <Typography variant="body2"><b>Linguistic Variant</b></Typography>
            </Box>
            <Divider className={classes.divider} />
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                  </TableRow>
                </TableHead>
                <TableBody>
                  { LV.data.map((lv, index) => (
                  <TableRow key={index}>
                    <StyledTableCell className={classes.label} width="15%" component="th" scope="row">
                      {lv.isoCountry}-{lv.isoLang}
                    </StyledTableCell>
                    <StyledTableCell className={classes.label} >
                      <strong>{lv.lang}</strong>
                    </StyledTableCell>
                    { lv.component
                      ? (
                      <StyledTableCell className={classes.label}>
                        {lv.component}:{lv.property}:{lv.timeAspect}:{lv.system}:{lv.scaleType}:{lv.methodType}
                      </StyledTableCell>
                      ) : (
                        <>
                        { lv.longCommonName
                          ? (
                          <StyledTableCell className={classes.label}>
                            {lv.longCommonName}
                          </StyledTableCell>
                          ) : (
                            <>
                            { lv.shortName &&
                              <StyledTableCell className={classes.label}>
                                {lv.shortName}
                              </StyledTableCell>
                            }
                            </>
                          )
                        }
                        </>
                      )
                    }
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        }
        </Grid>
      </Grid>
    </>
  )
}

