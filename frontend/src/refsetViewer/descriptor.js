import React, { useState, useEffect} from 'react';
import axios from 'axios';
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

const StyledTableCell = withStyles((theme) => ({
  head: {
    backgroundColor: "#e3f2fd",
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
  gridcontainer: {
    height: '100vh',
  },
  divider: {
    borderBottom: "solid 2px #2196F3",
  },
  gridBorder: {
    borderRight: "dotted 1px lightGray",
    borderLeft: "dotted 1px lightGray",
  },
  fsn: {
    fontSize:'1rem', fontWeight:'800', color: '#d9534f',
  },
  preferred: {
    fontSize:'1rem', fontWeight:'800', color: '#468847'
  },
  synonym: {
    fontSize:'1rem', fontWeight:'800', color: '#3a87ad'
  },
  acceptable: {
    fontSize:'1rem', fontWeight:'800', color: '#FFD700'
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

export default function Descriptor(props) {

  const classes = useStyles();

  const [descript, setDescript] = useState([]);
  const [refset, setRefset] = useState([]);

  const refsetDescriptor = '900000000000456007';

  useEffect(() => {
    axios
      .get(`http://api.infoclinic.co/members/SNOMEDCT?refcpntid=${props.refset.id}`)
      .then(response => setDescript(response));
  }, [props.refset.id])

  useEffect(() => {

    let order;

    let ref = [];
    ref[refsetDescriptor] = [];

    if (descript.data) {
      descript.data.forEach((item, index, mem) => {
        if (mem[index].type.id === refsetDescriptor) {
          if (mem[index].extra["Attribute order"]) {
              order = mem[index].extra["Attribute order"].id;
              ref[refsetDescriptor][order] = [];
          }
          for (let me in mem[index].extra) {
            ref[refsetDescriptor][order].push({
              "title": me,
              "id" : mem[index].extra[me].id,
              "name" : mem[index].extra[me].name
            })
          }
        }
      })
      console.log(ref);
      setRefset(ref);
    }
  },[descript])


  return (
    <div>
      { refset[refsetDescriptor] &&
      <Grid item className={classes.gridBorder} md={12} >
        <Box p={1}>
          <Typography variant="body2">
            <b>Reference set descriptor reference set (foundation metadata concept)</b>
          </Typography>
          <Typography className={classes.label}>
            @{props.refset.id}, {props.refset.name}
          </Typography>
        </Box>
        <Divider className={classes.divider}/>
        < br/>
        { refset[refsetDescriptor][0] &&
          <Box p={1}>
          <TableContainer p={1} align="center">
            <Table size="small" aria-label="a small table">
              <TableHead>
                <TableRow >
                  { refset[refsetDescriptor][0].map((rs0, index) => (
                  <StyledTableCell key={index} className={classes.label}>
                    {rs0.title}
                  </StyledTableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                { refset[refsetDescriptor].map((rs,index2) => (
                <TableRow key={index2}>
                  { rs.map((r,index3) => (
                    <StyledTableCell key={index3}  >
                    { r.title === "Attribute order" && <div>
                      {r.id}
                      </div>
                    }
                    { r.title !== "Attribute order" && <div>
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
          <br />
          </Box>
        }
      </Grid>
      }
    </div>
  );
}
