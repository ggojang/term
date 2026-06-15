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
import { Link } from "react-router-dom";
import Drawer from '@material-ui/core/Drawer';
import FormLabel from '@material-ui/core/FormLabel';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
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
    fontSize: '0.7em',
  },
  badgeLabel: {
    fontSize: '0.5em',
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
    minWidth: "10ch",
    fontSize: '0.8em',
  },
  select: {
    minWidth: '10ch',
    height: '3ch',
    fontSize : "0.7em",
    verticalAlign: "middle"
  },
  textfield: {
    marginTop: theme.spacing(1),
    width: "10rem",
  },
  tf: {
    fontSize: "0.7em",
  },
  menuitem: {
    fontSize : "0.7em",
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
  const [matchType, setMatchType] = useState('PARTIAL');
  const [state, setState] = useState('ACTIVE');
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(100);
  const [stateTag, setStateTag] = useState({
    left: false,
  });
  const [semanticTags, setSemanticTags] = useState([]);
  const [listCheckBox, setListCheckBox] = useState({});
  const [stateCheckBox, setStateCheckBox] = useState({});
  /*
  const [stateSearchResult, refetch] = useAsync(() => getSearch(matchType, state, q, tag, page, size), [], true);
  const { loadingSearchResult, data: searchResult, errorSearchResult } = stateSearchResult;
  */
  const [result, setResult] = useState([]);
  const [result2, setResult2] = useState([]);

  const [searchResult, setSearchResult] = useState([]);
  const [searchResult2, setSearchResult2] = useState([]);

  /*
  const preventDefault = (event) => event.preventDefault();
  */

  useEffect(() => {
    if (q.length > 1) {

      setPage(1);
      setListCheckBox({});

      axios
        .get(`http://api.infoclinic.co/search/SNOMEDCT?match=${matchType}&state=${state}&q=${q}&page=${page}&size=${size}`)
        .then(response => setSearchResult(response));
    }
  }, [q, state, matchType]);

  useEffect(() => {
    if (q.length > 1 && page > 1) {
      let tmp="";
      for (var l in listCheckBox) {
        if (listCheckBox[l]) {
          tmp += "&semanticfilter=" + l;
          /*
          console.log(l + ":" + listCheckBox[l]);
          */
        }
      }
      tmp = tmp.replace(/ /gi,'%20');
      tmp = tmp.replace(/\+/gi,'%2B');
      /*
      console.log("tmp : " + tmp);
      console.log("page : " + page);
      console.log("page query: "+`match=${matchType}&state=${state}&q=${q}${tmp}&page=${page}&size=${size}`);
      */
      axios
        .get(`http://api.infoclinic.co/search/SNOMEDCT?match=${matchType}&state=${state}&q=${q}${tmp}&page=${page}&size=${size}`)
        .then(response => setSearchResult(response));
    }
  }, [page]);

  useEffect(() => {

    if (searchResult.length !== 0)  {
      /*console.log("=>");
      */
      if (searchResult.data.page.totalElements > 0) {
        /*console.log("= =>");
        */
        if (page === 1) {
          /*console.log("= = =>");
          */
          setResult(searchResult);
          setResult2(searchResult.data.page.content);
          setSemanticTags(searchResult.data.semanticTags);
          /*if (listCheckBox.length === 0) {*/
            /*var tmp={};*/
            searchResult.data.semanticTags.map((tags) => {
              /*tmp[tags.name] = false;*/
              listCheckBox[tags.name] = false;
            });
            /*setListCheckBox(tmp);*/
          /*}*/
        } else {
          /*console.log("= = = =>");
          */
          /*searchResult.data.page.content.map(r => {*/
            setResult2([...result2,  ...searchResult.data.page.content]);
          /*});*/
        }
        /*
        console.log("result.length : " + Object.keys(result).length);
        console.log("result : " + JSON.stringify(result));
        console.log("result2.length : " + Object.keys(result2).length);
        console.log("result2 : " + typeof(result2) + " , " + JSON.stringify(result2));
        */
      } else {
        setResult([]);
        setResult2([]);
        setSemanticTags([]);
      }
    /*console.log(listCheckBox);*/
    }
  }, [searchResult])

  useEffect(() => {
    if (q.length>1 && listCheckBox.length !==0) {
      setPage(1);
      let tmp = "";
      for (var l in listCheckBox) {
        if (listCheckBox[l]) {
          /* console.log(l + ":" + listCheckBox[l]);
          */
          tmp += "&semanticfilter=" + l;
        }
      }
      tmp = tmp.replace(/ /gi,'%20');
      tmp = tmp.replace(/\+/gi,'%2B');
      /*
      console.log("tmp : " + tmp );
      console.log("==> " + `match=${matchType}&state=${state}&q=${q}${tmp}&page=1&size=${size}`);
      */
      axios
        .get(`http://api.infoclinic.co/search/SNOMEDCT?match=${matchType}&state=${state}&q=${q}${tmp}&page=1&size=${size}`)
        .then(response => setSearchResult2(response));

    }
  },[listCheckBox])

  useEffect(() => {
    if (searchResult2.length !== 0)  {
      if (searchResult2.data.page.totalElements > 0) {
        setResult2([]); /* 화면을 끝부분에 약간 올라간 상태에서 draw에서 숫자가 작은 semantic tag을 선택하면 scroll이 된 상태가 되어 결과가 없는 것 처럼보임. */
        setResult(searchResult2);
        setResult2([...searchResult2.data.page.content]);
        setSearchResult2([]);
      }
    }
  }, [searchResult2])



  const handleMatchTypeChange = (event) => {
    setMatchType(event.target.value);
  };

  const handleStatusChange = (event) => {
    setState(event.target.value);
  };

  const handleQueryKeyUp = (event) => {
    if (window.event.keyCode === 13) {
      setQ(event.target.value);
    }
  };
/*
  const handleGotoMainOnClick = (event) => {

    return `<Main conceptId=${event.target.value}/>`;
  };
*/
  const nextPage = (event) => {
      setPage(page + 1);
  }

  const handleCheckBoxChange = (event) => {
    setListCheckBox({ ...listCheckBox, [event.target.name] : event.target.checked});
  }

  /* Drawer */

  const toggleDrawer = (anchor, open) => (event) => {

    if (event.type === 'keydown' && (event.key === 'Tab' || event.key === 'Shift')) {
      return;
    }

    setStateTag({ ...stateTag, [anchor]: open });

  };

  const list = (anchor) => (

    <div
      className={clsx(classes.list)}
      role="presentation"
    >

      <FormControl component="fieldset" className={classes.formControl}>
        <FormLabel variant="body1" component="legend">Semantic Tag</FormLabel>
        <div className={classes.line}></div>
        <FormGroup>
          { semanticTags.map((tags, index) => (
          <FormControlLabel style={{height:"22px"}}
            key={index}
            control={<Checkbox checked={listCheckBox[tags.name]} onClick={handleCheckBoxChange} name={tags.name} />}
            label={
              <Grid container wrap="nowrap" spacing={1}>
                <Grid item>
                  <Typography color="secondary" className={classes.label}>{tags.count}</Typography>
                </Grid>
                <Grid item>
                  <Typography className={classes.label}>{tags.name}</Typography>
                </Grid>
              </Grid>
            }
          />
          ))}
        </FormGroup>
      </FormControl>

    </div>
  );

  return (
    <Grid container>
    <Grid item md={12}>
      <div style={{padding: "0 0 0.5rem 0",}}></div>
      <Toolbar classes={{root: classes.toolbarRoot}} className={classes.toolbar} style={{backgroundColor: "#ffffff", padding: "0 0 0 0"}}>
      <Grid container justify="space-around">
        <Grid item>
          <FormControl className={classes.form} >
            <InputLabel className={classes.inputlabel} id="matchTypeLabel">Match Type</InputLabel>
            <Select
              labelId="matchTypeLabel"
              id="matchType"
              className={classes.select}
              value={matchType}
              onChange={handleMatchTypeChange}

            >
              <MenuItem className={classes.menuitem} value={'FULLTEXT'}>exact</MenuItem>
              <MenuItem className={classes.menuitem} value={'PARTIAL'}>partial</MenuItem>
              <MenuItem className={classes.menuitem} value={'REGEX'}>regex</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item>
          <FormControl className={classes.form}>
            <InputLabel className={classes.inputlabel} id="statusTypeLabel">Status Type</InputLabel>
            <Select
              labelId="statusTypeLabel"
              id="statusType"
              className={classes.select}
              value={state}
              onChange={handleStatusChange}

            >
              <MenuItem className={classes.menuitem} value={'ACTIVE'}>active</MenuItem>
              <MenuItem className={classes.menuitem} value={'INACTIVE'}>inactive</MenuItem>
              <MenuItem className={classes.menuitem} value={'BOTH'}>both</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item >
          <FormControl>
            <InputLabel shrink
              className={classes.inputlabel}
              id="queryLabel">At least 2 more characters
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

        { (!q || !result2 || (result.length === 0)) ? (
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
        ) :
        (
        <Grid item md={11}>
          <div className={classes.line}>
          <Grid container justify="space-around" alignItems="center">
            <Grid item>
              <Typography className={classes.label} align="center">
                Total : {result.data.page.totalElements}
              </Typography>
            </Grid>

            <Grid item>
            {['left'].map((anchor) => (
              <div key={anchor} >
                <Button
                  style={{lineHeight:"10px"}}
                  className={classes.label}
                  variant="contained"
                  disableElevation
                  onClick={toggleDrawer(anchor, true)}>
                  Semantic Tag ({semanticTags.length})
                </Button>
                <Drawer  className={classes.label} anchor={anchor} open={stateTag[anchor]} onClose={toggleDrawer(anchor, false)}>
                  {list(anchor)}
                </Drawer>
              </div>
            ))}
            </Grid>
          </Grid>
          </div>
          <Divider className={classes.leftdivider}/>
        </Grid>
        )}
      </Grid>


      </Toolbar>

        { (q && result2 && (result.length !== 0)) &&

          <Container
            className={classes.container} /*ref={setRef}*/
            style={{
              padding: "0 0 0 12px",
              height: "88vh",
              overflow: "scroll",
              maxWidth: '720px' }}>

            { result2
              .map((re, index) => (
              <div key={index}>
                { (re.conceptActive === true && re.descriptionActive === true) &&
                <div className={classes.line}>
                  <div onClick={()=> {props.setFromId(re.conceptId); props.setMrcmFromSearch(re.conceptId)}} >
                    <Grid container classes={{root:classes.hover}}>
                      <Grid item md={5} >
                        <Grid container wrap="nowrap" >

                            {re.definitionStatusId === '900000000000073002' &&
                              <>
                              { re.typeId === "900000000000003001" && re.acceptabilityId === "900000000000548007" &&
                                <>
                                  <span style={{position: "relative"}}>
                                    <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>≡</Typography>
                                  </span>
                                  <span style={{position: "relative", right:"5px"}}>
                                    <sup className={classes.fsn}>F</sup>
                                  </span>
                                </>
                              }
                              { re.typeId === "900000000000013009" && re.acceptabilityId === "900000000000548007" &&
                                <>
                                  <span style={{position: "relative"}}>
                                    <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>≡</Typography>
                                  </span>
                                  <span style={{position: "relative", right:"5px"}}>
                                    <sup className={classes.preferred}>P</sup>
                                  </span>
                                </>
                              }
                              { re.typeId === "900000000000013009" && re.acceptabilityId === "900000000000549004" &&
                                <>
                                  <span style={{position: "relative"}}>
                                    <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>≡</Typography>
                                  </span>
                                  <span style={{position: "relative", right:"5px"}}>
                                    <sup className={classes.synonym}>S</sup>
                                  </span>
                                </>
                              }
                              </>
                            }
                            {re.definitionStatusId === '900000000000074008' &&
                              <>
                              { re.typeId === "900000000000003001" && re.acceptabilityId === "900000000000548007" &&
                                <>
                                  <span style={{position: "relative"}}>
                                    <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</Typography>
                                  </span>
                                  <span style={{position: "relative", right:"5px"}}>
                                    <sup className={classes.fsn}>F</sup>
                                  </span>
                                </>
                              }
                              { re.typeId === "900000000000013009" && re.acceptabilityId === "900000000000548007" &&
                                <>
                                  <span style={{position: "relative"}}>
                                    <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</Typography>
                                  </span>
                                  <span style={{position: "relative", right:"5px"}}>
                                    <sup className={classes.preferred}>P</sup>
                                  </span>
                                </>
                              }
                              { re.typeId === "900000000000013009" && re.acceptabilityId === "900000000000549004" &&
                                <>
                                  <span style={{position: "relative"}}>
                                    <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</Typography>
                                  </span>
                                  <span style={{position: "relative", right:"5px"}}>
                                    <sup className={classes.synonym}>S</sup>
                                  </span>
                                </>
                              }
                              </>
                            }

                            <div display="inline" className={classes.label} > {re.term} </div>

                        </Grid>
                      </Grid>
                      <Grid item md={7}>
                        <Grid container wrap="nowrap" >

                            {re.definitionStatusId === '900000000000073002' &&
                              <>
                                <span style={{position: "relative"}}>
                                  <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>≡</Typography>
                                </span>
                                <span style={{position: "relative", right:"5px"}}>
                                  <sup className={classes.fsn}>F</sup>
                                </span>
                              </>
                            }
                            {re.definitionStatusId === '900000000000074008' &&
                              <>
                                <span style={{position: "relative"}}>
                                  <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</Typography>
                                </span>
                                <span style={{position: "relative", right:"5px"}}>
                                  <sup className={classes.fsn}>F</sup>
                                </span>
                              </>
                            }

                            <Typography display="inline" className={classes.label} > {re.fsn} </Typography>

                        </Grid>
                      </Grid>
                    </Grid>
                  </div>
                </div>
                }

                { (re.conceptActive === false || re.descriptionActive === false) &&
                <div className={classes.line} style={{backgroundColor: '#ffefef'}}>
                  <div onClick={()=>props.setFromId(re.conceptId)} >
                    <Grid container >
                      <Grid item md={5}>
                        <Grid container wrap="nowrap" >

                            {re.definitionStatusId === '900000000000073002' &&
                              <>
                                { re.typeId === "900000000000003001" && re.acceptabilityId === "900000000000548007" &&
                                  <>
                                    <span style={{position: "relative"}}>
                                      <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>≡</Typography>
                                    </span>
                                    <span style={{position: "relative", right:"5px"}}>
                                      <sup className={classes.fsn}>F</sup>
                                    </span>
                                  </>
                                }
                                { re.typeId === "900000000000013009" && re.acceptabilityId === "900000000000548007" &&
                                  <>
                                    <span style={{position: "relative"}}>
                                      <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>≡</Typography>
                                    </span>
                                    <span style={{position: "relative", right:"5px"}}>
                                      <sup className={classes.preferred}>P</sup>
                                    </span>
                                  </>
                                }
                                { re.typeId === "900000000000013009" && re.acceptabilityId === "900000000000549004" &&
                                  <>
                                    <span style={{position: "relative"}}>
                                      <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>≡</Typography>
                                    </span>
                                    <span style={{position: "relative", right:"5px"}}>
                                      <sup className={classes.synonym}>S</sup>
                                    </span>
                                  </>
                                }
                              </>
                            }
                            {re.definitionStatusId === '900000000000074008' &&
                              <>
                              { re.typeId === "900000000000003001" && re.acceptabilityId === "900000000000548007" &&
                                <>
                                <span style={{position: "relative"}}>
                                  <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</Typography>
                                </span>
                                <span style={{position: "relative", right:"5px"}}>
                                  <sup className={classes.fsn}>F</sup>
                                </span>
                                </>
                              }
                              { re.typeId === "900000000000013009" && re.acceptabilityId === "900000000000548007" &&
                                <>
                                <span style={{position: "relative"}}>
                                  <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</Typography>
                                </span>
                                <span style={{position: "relative", right:"5px"}}>
                                  <sup className={classes.preferred}>P</sup>
                                </span>
                                </>
                              }
                              { re.typeId === "900000000000013009" && re.acceptabilityId === "900000000000549004" &&
                                <>
                                <span style={{position: "relative"}}>
                                  <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</Typography>
                                </span>
                                <span style={{position: "relative", right:"5px"}}>
                                  <sup className={classes.synonym}>S</sup>
                                </span>
                                </>
                              }
                              </>
                            }

                            <Typography display="inline" className={classes.label} > {re.term} </Typography>

                        </Grid>
                      </Grid>
                      <Grid item md={7}>
                        <Grid container wrap="nowrap" >

                            {re.definitionStatusId === '900000000000073002' &&
                              <>
                                <span style={{position: "relative"}}>
                                  <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>≡</Typography>
                                </span>
                                <span style={{position: "relative", right:"5px"}}>
                                  <sup className={classes.fsn}>F</sup>
                                </span>
                              </>
                            }
                            {re.definitionStatusId === '900000000000074008' &&
                              <>
                                <span style={{position: "relative"}}>
                                  <Typography display="inline" className={classes.label} className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</Typography>
                                </span>
                                <span style={{position: "relative", right:"5px"}}>
                                  <sup className={classes.fsn}>F</sup>
                                </span>
                              </>
                            }

                            <Typography display="inline" className={classes.label} > {re.fsn} </Typography>

                        </Grid>
                      </Grid>
                    </Grid>
                  </div>
                </div>
                }
                <Divider className={classes.leftdivider}/>
              </div>
            ))}
            <Divider className={classes.leftdivider}/>
            <div className={classes.line}>
              { (page < result.data.page.totalPages) &&
              <Grid container justify="center">
                <Button variant="contained" onClick={nextPage}>Next Page</Button>
              </Grid>
              }
            </div>
          </Container>
        }

    </Grid></Grid>
  )
}
