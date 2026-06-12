import React, { useState, useEffect} from 'react';
import Snomed from './snomed/layout.js';
import Main from './snomed/main.js';
import EHRLayout from './ehr/layout.js';
import RefsetLayout from './refsetViewer/layout.js';
import MapLayout from './map/layout.js';
import LoincLayout from './loinc/layout.js';
import PropTypes from 'prop-types';
import CssBaseline from "@material-ui/core/CssBaseline"
import clsx from 'clsx';
import Grid from "@material-ui/core/Grid";
import { makeStyles, useTheme } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import { BrowserRouter as Router, Route, Switch, Redirect, Link } from "react-router-dom";

function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <Typography
      component="div"
      role="tabpanel"
      hidden={value !== index}
      id={`action-tabpanel-${index}`}
      aria-labelledby={`action-tab-${index}`}
      {...other}
    >
      {value === index && <Box>{children}</Box>}
    </Typography>
  );
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.any.isRequired,
  value: PropTypes.any.isRequired,
};

function a11yProps(index) {
  return {
    id: `action-tab-${index}`,
    'aria-controls': `action-tabpanel-${index}`,
  };
}

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: theme.palette.background.paper,
    width: '100vw',
    position: 'relative',
    minHeight: '100vh',
    fontSize: '0.8em',

  },
  appbar : {
    backgroundColor : '#2e3e4e',
    padding: '4px',
  },
  indicator : {
    backgroundColor : '#2e3e4e',
  },
  toolbar: {
    minHeight:'40px',
  },
  label: {
    fontSize: '1.3em',
  },
  msg: {
    fontSize: '1em',
  },
  tabs: {
    padding:'0',
  },
  tab1: {
    minHeight: '50px',
    minWidth: '150px',
    opacity: 0.5,
  },
  tab2: {
    minHeight: '50px',
    minWidth: '110px',
    opacity: 0.5,
  },
  tab3: {
    minHeight: '50px',
    minWidth: '65px',
    opacity: 0.5,
  },
}));

export default function App(props) {
  const classes = useStyles();
  const theme = useTheme();
  const [value, setValue] = useState(0);
  const [msg, setMsg] = useState("International Edition 2026.06.01");

  document.title = "InfoClinic STOM Browser";

  const urlId = window.location.href.split('/').reverse()[0]
  let id="";


  //if (!urlId) {
  if (!urlId || isNaN(Number(urlId))) {
    id = '138875005';
        //window.location.replace("http://localhost:3000/snomed/id/138875005");
        //window.history.pushState(null, "STOM Browser", "http://localhost:3000/snomed/id/138875005");
  } else {
    id = urlId;
  }


  const [fromId, setFromId] = useState(id);
  const [mrcmFromMain, setMrcmFromMain] = useState([]);
  const [mrcmFromSearch, setMrcmFromSearch] = useState("");
  const [loincId, setLoincId] = useState("");

  const handleChange = (event, newValue) => {
    /*switch (newValue) {
      case 4 :
        window.location.replace("/ehr");
        break;
    }*/
    setValue(newValue);
  };


  useEffect(
    () => {
      if (value >= 0 && value < 3) {
        setMsg("International Edition 2026.06.01");
      } else if (value === 3) {
        setMsg("Version 2.82 (2026-02-24)");
      } else if (value === 5){
        setMsg("2016 Release (2014-10-14)");
      } else {
        setMsg("");
      }
    },
    [value],
  );

  return (
    <>
    <CssBaseline />
    <div className={clsx(classes.root, classes.toolbar)}>
      <AppBar position="sticky" className={classes.appbar}>
        <Toolbar className={classes.toolbar}>
          <Grid container alignItems="center" >
            <Grid item md={1} >
              <Typography variant="h5" color="inherit" noWrap className={classes.title}>
                InfoClinic
              </Typography>
            </Grid>

            <Grid item md={9} >
                <Tabs
                  value={value}
                  onChange={handleChange}
                  classes={{indicator: classes.indicator}} /* important */
                  aria-label="browser tab"
                  variant="standard"
                >
                  <Tab className={clsx(classes.label, classes.tab1)} label="SNOMED CT Browser" {...a11yProps(0)} />
                  <Tab className={clsx(classes.label, classes.tab3)} label="Refset Viewer" {...a11yProps(1)} />
                  <Tab className={clsx(classes.label, classes.tab2)} label="Mapping Support" {...a11yProps(2)} />
                  <Tab className={clsx(classes.label, classes.tab3)} label="LOINC Browser" {...a11yProps(3)} />
                </Tabs>
            </Grid>

            <Grid item md={2} >
              <Typography className={clsx(classes.msg)} color="inherit" noWrap >
                {msg}
              </Typography>
            </Grid>
          </Grid>
        </Toolbar>
      </AppBar>


      <TabPanel value={value} index={0}>
        {
          !urlId
          ? (
            <Snomed
              fromId={fromId}
              setMrcmFromMain={setMrcmFromMain}
              setMrcmFromSearch={setMrcmFromSearch}
              setFromId={setFromId}
              mrcmFromMain={mrcmFromMain}
              mrcmFromSearch={mrcmFromSearch}
            />
          ):(
            <Route path="/snomed/id/:id"
              render={ () =>
                <Snomed fromId={fromId}
                  setMrcmFromMain={setMrcmFromMain}
                  setMrcmFromSearch={setMrcmFromSearch}
                  setFromId={setFromId}
                  mrcmFromMain={mrcmFromMain}
                  mrcmFromSearch={mrcmFromSearch}
                />
              }
            />
          )
        }
      </TabPanel>
      <TabPanel value={value} index={1}>
        <RefsetLayout />
      </TabPanel>
      <TabPanel value={value} index={2}>
        <MapLayout />
      </TabPanel>
      <TabPanel value={value} index={3}>
        <LoincLayout
          loincId={loincId}
          setLoincId={setLoincId}
        />
      </TabPanel>
    </div>
    </>
  );
}
