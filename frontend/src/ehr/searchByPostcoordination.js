import React, { useState, useEffect} from 'react';

import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';

import Toolbar from '@material-ui/core/Toolbar';
import Grid from "@material-ui/core/Grid";
import Divider from "@material-ui/core/Divider";
import Typography from '@material-ui/core/Typography';
import FormControl from '@material-ui/core/FormControl';
import TextField from '@material-ui/core/TextField';
import Select from "@material-ui/core/Select";
import InputLabel from "@material-ui/core/InputLabel";
import MenuItem from '@material-ui/core/MenuItem';
import Container from '@material-ui/core/Container';
import { Link } from "react-router-dom";
import Box from '@material-ui/core/Box';
import Drawer from '@material-ui/core/Drawer';
import FormLabel from '@material-ui/core/FormLabel';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import Button from '@material-ui/core/Button';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';

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
  alertWarning: {
    backgroundImage: 'linear-gradient(to bottom,#f7edb5 0,#f5e79e 100%)',
    backgroundRepeat: 'repeat-x',
    color: '#8a6d3b',
    backgroundColor: '#fcf8e3',
    borderColor: '#f5e79e',
    /*'&::after' : {
      content: '"F"',
    }*/
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
    backgroundColor: '#999',
    borderRadius: '10px',

  },
  label: {
    fontSize: '1em',
  },
  badgeLabel: {
    fontSize: '0.5em',
  },
  inputLabel: {
    minWidth: "10ch",
    fontSize: '1em',
  },
  inputSearchLabel: {
    minWidth: "30ch",
    fontSize: '1em',
  },
  select: {
    minWidth: '15ch',
    height: '4ch',
    fontSize : "1em",
    verticalAlign: "middle"
  },
  textfield: {
    marginTop: theme.spacing(1),
    height: '4ch',
    width: "20rem",
  },
  tf: {
    fontSize: "1em",
  },
  menuitem: {
    fontSize : "1em",
  },
  fsn: {
    fontSize:'0.4em', fontWeight:'800', color: '#d9534f',
  },
  preferred: {
    fontSize:'0.4em', fontWeight:'800', color: '#468847'
  },
  synonym: {
    fontSize:'0.4em', fontWeight:'800', color: '#3a87ad'
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
  line: {
    padding: "0.5em 0 0.5rem 0",
  },
  link: {
    textDecoration: "none",
    color: '#000',
    /* '&:hover': {
      color: '#3a87ad',
    }, */
  },
  list: { /* for drawer */
    width: "17vw",
  },
  formControl: {
    margin: theme.spacing(1),
    alignItems : "center",
  },
  hover: {
    /* 마우스 오버(마우스 올렸을때) */
    '&:hover' : {
        backgroundColor: "#dce6f0",

    },
  },
  gridBorder: {
    border: "dotted 1px Gray",
    minWidth : "400px",
  },
}));

function ListItemLink(props) {
  return <ListItem button component="a" {...props} />;
}

export default function SearchByPostcoordination(props) {

  const classes = useStyles();

  const [q, setQ] = useState('');
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(1000);

  const [result, setResult] = useState([]);

  const posts = [
    "emergency closed reduction fracture of left femur",
    "emergency closed reduction fracture of right femur",
    "emergency closed reduction fracture of both femur",
    "emergency closed reduction fracture of left and right femur"
  ];

  useEffect(() => {
    if (q.length > 0) {
      setResult([]);

      let opt = `match=FULLTEXT&state=ACTIVE&q=${q}&page=${page}&size=${size}`;

      axios
        .get(`http://localhost:8080/search/SNOMEDCT?${opt}`)
        .then(response => setResult(response));
    }
  }, [q]);

  const handleQueryKeyUp = (event) => {
    if (window.event.keyCode === 13) {
      setQ(event.target.value);
    }
  };

  return (
    <>
      <Typography variant="h6">
        4.3.2. Extend Search by Postcoordinated Searching
      </Typography>
      <Grid container className={classes.gridBorder} alignItems="center" spacing={2} >
        <Grid item p={2} md={6}>
          <Typography>
            Enter search string
          </Typography>
          <Typography>
            - emergency closed reduction fracture of left femur
          </Typography>
          <Typography>
            - emergency closed reduction fracture of right femu
          </Typography>
          <Typography>
            - emergency closed reduction fracture of both femur
          </Typography>
          <Typography>
            - emergency closed reduction fracture of left and right femur
          </Typography>
          <FormControl className={classes.form}>

            <TextField
              labelid="queryLabel"
              className={classes.textfield}
              InputProps={{
                classes: {
                  input: classes.tf,
                },
              }}
              id="query"
              type="search"
              onKeyUp={handleQueryKeyUp}
              />
          </FormControl>
        </Grid>
        <Grid item  p={2} md={12}>
          <Typography>
          Search results
          </Typography>
          <Grid item className={classes.gridBorder}>
            <List dense style={{padding:"0 0 0 0"}}>
              { (result.length !== 0 && q.length !=0) &&
                <>
                { result.data.page.content
                  .map( (res,index) => (
                  <div key={index}>
                        <>
                        { res.term !== res.fsn &&
                          <ListItem dense disableGutters style={{height: "16px", padding:"0 0 0 0", margin:"0 0 0 0 "}}>
                            <ListItemLink href="#simple-list">
                              <ListItemText primary={`${res.term} (${res.semanticTag})`} style={{padding:"0 0 0 0"}}/>
                            </ListItemLink>
                          </ListItem>
                        }
                        </>
                  </div>
                ))}
                </>
              }
              { (posts.includes(q) && q.length !=0) &&
                <>
                { posts.map( (post, index) => (
                    <div key={index}>
                      {post.includes(q) &&
                        <ListItem dense disableGutters style={{height: "16px", padding:"0 0 0 0", margin:"0 0 0 0 "}}>
                          <ListItemLink href="#simple-list">
                            <ListItemText primary={`${post}`} style={{padding:"0 0 0 0"}}/>
                          </ListItemLink>
                        </ListItem>
                      }
                    </div>
                  ))
                }
                </>
              }
            </List>
          </Grid>
        </Grid>
      </Grid>
    </>
  );
}
