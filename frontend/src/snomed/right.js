import React, {useState, useEffect, useContext, createContext} from 'react';
import Search from './search.js';
import Children from './children.js';
import Parent from './parent.js';
import Mrcm from './mrcm.js';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import { MuiThemeProvider, createMuiTheme, useTheme, makeStyles, withStyles} from '@material-ui/core/styles';
import Toolbar from '@material-ui/core/Toolbar';
import Grid from "@material-ui/core/Grid";
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Container from '@material-ui/core/Container';
import { BrowserRouter as Router, Route, Switch, Redirect, Link } from "react-router-dom";

import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import MuiExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

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
  expansionPanel: {
    backgroundColor: 'transparent !important',
    boxShadow: 'none',
  },
  container: {
    '-ms-overflow-style': 'none', /* IE and Edge */
    scrollbarWidth: 'none', /* Firefox */
    '&::-webkit-scrollbar': {
        display: 'none', /* Chrome, Safari, Opera*/
    },
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
    fontSize: '0.9em',
  },
  tabs: {
    minHeight : '2vh',
    padding:'0',
  },
  tab1: {
    minHeight: '2vh',
    minWidth: '100px',
    opacity: 0.5,
  },
  tab2: {
    minHeight: '2vh',
    minWidth: '100px',
    opacity: 0.5,
  },
  link: {
    textDecoration: "none",
    color: '#000',
    /* '&:hover': {
      color: '#3a87ad',
    }, */
  },alertWarning: {
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
  heading: {
    fontSize: '0.7rem',
    fontWeight: '800',
  },
  expansionPanelRoot: {
      square: true,
  },
  gridcontainer: {
    height: '100vh',
  },
}));

const ExpansionPanelSummary = withStyles({
  root: {
    minHeight:16,
    '&$expanded': {
        minHeight:16
    }
  },
  content: {
    '&$expanded': {
      margin: '0',
    }
  },
  expanded: {
    backgroundColor: '#eee', //'#e3f2fd',
  },
})(MuiExpansionPanelSummary);

/*
export const AppContext = createContext();
*/

export default function Right(props) {

  const classes = useStyles();
  const theme = useTheme();
  const [value, setValue] = useState(0);
  const [valueFrom, setValueFrom] = useState('')

  const handleChange = (event, newValue) => {
    setValue(newValue);
    setValueFrom('TabClick');
  };

  useEffect(() => {
    setValue(0);
    setValueFrom('TabClick');
  }, [props.id]);

  useEffect(() => {
    if (props.mrcmFromMain.length !== 0) {
      /*console.log("right (props.mrcmFromMain) : " + props.mrcmFromMain);*/
      setValue(1);
      setValueFrom('BoxClick');
    }
  }, [props.mrcmFromMain]);

  /*console.log("Right (props.id) : " + props.id);*/

  return (
    <MuiThemeProvider theme={forTabs}>
      <Toolbar className={classes.toolbar} classes={{root: classes.toolbarRoot}} style={{ backgroundColor: "#ffffff", padding: "0 0 0 0"}}>
        <Tabs
          value={value}
          onChange={handleChange}
          aria-label="browser tab"
          variant="standard"
          classes={{root: classes.tabs}}
        >
          <Tab variant="body1" className={clsx(classes.tab1)} label="Parent/Children" {...a11yProps(0)} />
          <Tab variant="body1" className={clsx(classes.tab2)} label="MRCM" {...a11yProps(1)} />
        </Tabs>
      </Toolbar>

      <Container
        className={classes.container} /*ref={setRef}*/
        style={{
          padding: "0 0 0 0",
          height: "91vh",
          overflow: "scroll",
          maxWidth: '100vw'}}>
      <TabPanel style={{padding: "0 0 0 0"}} value={value} index={0}>
        <ExpansionPanel expanded square={true} elevation={0} style={{ backgroundColor: 'transparent' }}>
          <ExpansionPanelSummary>
            <Typography className={classes.heading}>Parent Hierarchy</Typography>
          </ExpansionPanelSummary>
          <ExpansionPanelDetails style={{padding: "8px 0 0 0", backgroundColor: 'transparent'}}>
            <Container className={classes.container}
              style={{
                margin : "0 0 0 0",
                padding: "0 0 0 0",
                overflow: "scroll",
                border:'none',
                backgroundColor: 'transparent'}}>
                <Parent firstId={props.id} setFromId={props.setFromId} />
            </Container>
          </ExpansionPanelDetails>
        </ExpansionPanel>
        <ExpansionPanel expanded square={true} elevation={0} style={{ backgroundColor: 'transparent' }}>
          <ExpansionPanelSummary>
            <Typography className={classes.heading}>Children Hierarchy</Typography>
          </ExpansionPanelSummary>
          <ExpansionPanelDetails style={{padding: "8px 0 0 0", backgroundColor: 'transparent'}}>
            <Container className={classes.container}
              style={{
                margin : "0 0 0 0",
                padding: "0 0 0 0",
                overflow: "scroll",
                backgroundColor: 'transparent'}}>
                <Children firstId={props.id} setFromId={props.setFromId} />
            </Container>
          </ExpansionPanelDetails>
        </ExpansionPanel>
      </TabPanel>
      <TabPanel value={value} index={1}>
        <Mrcm id={props.id} mrcmFromMain={props.mrcmFromMain} mrcmFromSearch={props.mrcmFromSearch} valueFrom={valueFrom}/>
      </TabPanel>
      </Container>
    </MuiThemeProvider>
  );
}
