import React, {useState} from 'react';
import Search from './search.js';
import ClassTree from './classTree.js';
import LPTree from './lpTree.js';
import LGTree from './lgTree.js';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import { MuiThemeProvider, createMuiTheme, makeStyles, useTheme } from '@material-ui/core/styles';
import Toolbar from '@material-ui/core/Toolbar';
import Grid from "@material-ui/core/Grid";
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Container from '@material-ui/core/Container';
import { Link } from "react-router-dom";

import ErrorBoundary from 'react-error-boundaries'

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

const forTabs = createMuiTheme({
  overrides: {
    MuiTabs: {
      indicator : {
        backgroundColor : '#fff'/*'#2e3e4e'*/
      }
    }
  },
});

const useStyles = makeStyles((theme) => ({
  root: {
    width: "100%",
  },
  container: {
    '-ms-overflow-style': 'none', /* IE and Edge */
    scrollbarWidth: 'none', /* Firefox */
    '&::-webkit-scrollbar': {
        display: 'none', /* Chrome, Safari, Opera*/
    },
  },
  appbar : {
    backgroundColor : '#2e3e4e',
    padding: '4px',
  },
  toolbarRoot: {
      minHeight: "2vh",
  },
  toolbar: {
    regular:{
      position: "fixed",
    },
  },
  label: {
    fontSize: '0.7em',
  },
  tabs: {
    minHeight : '2vh',
    padding:'0',
  },
  tab1: {
    minHeight : '2vh',
    minWidth: '10px',
    opacity: 0.5,
  },
  link: {
    textDecoration: "none",
    color: '#000',
    /* '&:hover': {
      color: '#3a87ad',
    }, */
  },
}));

function onError(error, errorInfo, props) {
  // you can report Error to service here
  console.error('onError:', error.message);
}


export default function Left(props) {
  const classes = useStyles();
  /*const theme = useTheme();*/
  const [value, setValue] = useState(0);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  return (
    <MuiThemeProvider theme={forTabs}>
    <Grid container>
      <Grid item md={12}>
        <Toolbar className={classes.toolbar} classes={{root: classes.toolbarRoot}} style={{ backgroundColor: "#ffffff", padding: "0 0 0 0"}}>
          <Tabs
            value={value}
            onChange={handleChange}
            aria-label="browser tab"
            variant="standard"
            classes={{root: classes.tabs}}
          >
            <Tab variant="body2" classes={{root: classes.tab1}} label="Search" {...a11yProps(0)} />
            <Tab variant="body2" classes={{root: classes.tab1}} label="Class" {...a11yProps(1)} />
            <Tab variant="body2" classes={{root: classes.tab1}} label="Parts" {...a11yProps(2)} />
            <Tab variant="body2" classes={{root: classes.tab1}} label="Group" {...a11yProps(3)} />
          </Tabs>
        </Toolbar>

        <TabPanel value={value} index={0}>
          <Search
            setLoincId={props.setLoincId}
            setIdFromSearchToMain={props.setIdFromSearchToMain}
            setIdFromSearchToPanel={props.setIdFromSearchToPanel}
          />
        </TabPanel>
        <TabPanel value={value} index={1}>
          <Container
            className={classes.container}
            style={{
              margin : "0 0 0 0",
              padding: "12px 0 0 12px",
              height: "88vh",
              overflow: "scroll"}}>
              <ClassTree
                setLoincId={props.setLoincId}
                setIdFromTreeToMain={props.setIdFromTreeToMain}
                setIdFromTreeToPanel={props.setIdFromTreeToPanel}
              />
          </Container>
        </TabPanel>
        <TabPanel value={value} index={2}>
          <Container
            className={classes.container}
            style={{
              margin : "0 0 0 0",
              padding: "12px 0 0 12px",
              height: "88vh",
              overflow: "scroll"}}>
            <LPTree
              setLoincId={props.setLoincId}
              setIdFromTreeToMain={props.setIdFromTreeToMain}
              setIdFromTreeToPanel={props.setIdFromTreeToPanel}
            />
          </Container>
        </TabPanel>
        <TabPanel value={value} index={3}>
          <Container
            className={classes.container}
            style={{
              margin : "0 0 0 0",
              padding: "12px 0 0 12px",
              height: "88vh",
              overflow: "scroll"}}>
            <LGTree
              setLoincId={props.setLoincId}
              setIdFromTreeToMain={props.setIdFromTreeToMain}
              setIdFromTreeToPanel={props.setIdFromTreeToPanel}
            />
          </Container>
        </TabPanel>
      </Grid>
    </Grid>

    </MuiThemeProvider>
  );
}
