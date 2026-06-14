import React, {useState, useEffect} from 'react';
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
import FormLabel from '@material-ui/core/FormLabel';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Button from '@material-ui/core/Button';


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
    fontSize: '0.7em',
  },
  appbar : {
    backgroundColor : '#ffffff',
    padding: '4px',
  },
  toolbarRoot: {
      minHeight: "12px",
  },
  toolbar: {
    regular:{
      position: "fixed",
    }
  },
  divider: {
    borderBottom: "solid 2px #2196F3",
  },
  leftdivider: {
    borderBottom: "dotted 2px text.secondary",
  },
  inputlabel: {
    minWidth: "20rem",
    fontSize: '0.8em',
  },
  textfield: {
    marginTop: theme.spacing(1),
    width: "20rem",
  },
  tf: {
    fontSize: "0.7em",
  },
  menuitem: {
    fontSize : "0.7em",
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
}));

export default function Search(props) {
  const classes = useStyles();

  const [q, setQ] = useState('');
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(100);

  const [result, setResult] = useState([]);
  const [result2, setResult2] = useState([]);

  const [searchResult, setSearchResult] = useState([]);
  const [searchResult2, setSearchResult2] = useState([]);

  useEffect(() => {
    if (q.length > 1) {
      setPage(1);
      axios
        .get(`http://localhost:8080/search/LOINC?q=${q}&page=${page}&size=${size}`)
        .then(response => setSearchResult(response));
    }
  }, [q]);

  useEffect(() => {
    if (q.length > 1 && page > 1) {
      axios
        .get(`http://localhost:8080/search/LOINC?q=${q}&page=${page}&size=${size}`)
        .then(response => setSearchResult(response));
    }
  }, [page]);

  useEffect(() => {

    if (searchResult.data)  {
      if (searchResult.data.totalElements > 0) {
        if (page === 1) {
          setResult(searchResult);
          setResult2(searchResult.data.content);
        } else {
          setResult2([...result2,  ...searchResult.data.content]);
        }
      } else {
        setResult([]);
        setResult2([]);
      }
    }
  }, [searchResult])
/*
  useEffect(() => {
    if (searchResult2.data)  {
      if (searchResult2.data.totalElements > 0) {
        setResult2([]);
        setResult(searchResult2);
        setResult2([...searchResult2.data.content]);
        setSearchResult2([]);
      }
    }
  }, [searchResult2])
*/
  const handleQueryKeyUp = (event) => {
    if (window.event.keyCode === 13) {
      setQ(event.target.value);
    }
  };

  const nextPage = (event) => {
      setPage(page + 1);
  }
  /*
  console.log("q : " + q);
  console.log("searchResult => " + JSON.stringify(searchResult.data));
  */
  return (
    <Grid container>
    <Grid item md={12}>
      <div style={{padding: "0 0 0.5rem 0",}}></div>
      <Toolbar classes={{root: classes.toolbarRoot}} className={classes.toolbar} style={{backgroundColor: "#ffffff", padding: "0 0 0 0"}}>
      <Grid container justify="space-around">
        <Grid item >
          <FormControl>
            <InputLabel shrink
              className={classes.inputlabel}
              id="queryLabel">At least 2 more characters (strings of FSN)
            </InputLabel>
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

        { (!q || !result2 || !result.data)
        ? (
          <Grid item md={12}>
            <div className={classes.line}>
              <Grid container justify="space-around" alignItems="center">
                <Grid item>
                  <Typography className={classes.label}>
                    Total : 0
                  </Typography>
                </Grid>
              </Grid>
            </div>
            <Divider className={classes.leftdivider}/>
          </Grid>
        ) : (
          <Grid item md={11}>
            <div className={classes.line}>
            <Grid container justify="space-around" alignItems="center">
              <Grid item>
                <Typography className={classes.label} align="center">
                  Total : {result.data.totalElements}
                </Typography>
              </Grid>
            </Grid>
            </div>
            <Divider className={classes.leftdivider}/>
          </Grid>
        )}
      </Grid>


      </Toolbar>

        { (q && result2 && (result.data)) &&

          <Container
            className={classes.container}
            style={{
              padding: "0 0 0 12px",
              height: "88vh",
              overflow: "scroll",
              maxWidth: '720px' }}>

            { result2
              .map((re, index) => (
              <div key={index}>
                <div className={classes.line}>
                  <div onClick={()=> {props.setLoincId(re.code)}} >
                    { (re.status === "ACTIVE" || re.status === "Active")
                    ? (
                      <Grid container classes={{root:classes.hover}}>
                        <Grid item md={2} >
                          <Grid container wrap="nowrap" >
                            <Typography display="inline" className={classes.label} > {re.code} </Typography>
                          </Grid>
                        </Grid>
                        <Grid item md={10}>
                          <Grid container wrap="nowrap" >
                            <Typography display="inline" className={classes.label} > {re.fsn} </Typography>
                          </Grid>
                        </Grid>
                      </Grid>
                    ) : (
                      <Grid container classes={{root:classes.hover}}>
                        <Grid item md={2} >
                          <Grid container wrap="nowrap" >
                            <Typography display="inline" className={classes.label} > <del>{re.code}</del> </Typography>
                          </Grid>
                        </Grid>
                        <Grid item md={10}>
                          <Grid container wrap="nowrap" >
                            <Typography display="inline" className={classes.label} > <del>{re.fsn}</del> </Typography>
                          </Grid>
                        </Grid>
                      </Grid>
                    )
                    }
                  </div>
                </div>

                <Divider className={classes.leftdivider}/>
              </div>
            ))}
            <Divider className={classes.leftdivider}/>
            <div className={classes.line}>
              { (page < result.data.totalPages) &&
              <Grid container justify="center">
                <Button style={{lineHeight:"0.5rem"}} variant="contained" onClick={nextPage}>Next Page</Button>
              </Grid>
              }
            </div>
          </Container>
        }

    </Grid></Grid>
  )
}
