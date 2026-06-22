import React, { useState, useRef, useEffect } from 'react';
import Search from './search.js';
import Hierarchy from './hierarchy.js';
import Ecl from './ecl.js';
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

const BOOKMARK_KEY = 'stom-bookmarks';

// 처음 활성화된 이후로는 DOM을 유지(hidden)하여 탭 상태 보존.
// 한 번도 활성화된 적 없는 탭은 렌더링하지 않아 초기 로드 속도 확보.
function TabPanel(props) {
  const { children, value, index, ...other } = props;
  const mountedRef = useRef(false);
  if (value === index) mountedRef.current = true;
  if (!mountedRef.current) return null;

  return (
    <Typography
      component="div"
      role="tabpanel"
      hidden={value !== index}
      id={`action-tabpanel-${index}`}
      aria-labelledby={`action-tab-${index}`}
      {...other}
    >
      <Box>{children}</Box>
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
    backgroundColor : '#1b2733',
    padding: '0 4px',
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
    minWidth: '80px',
    opacity: 0.5,
  },
  tab2: {
    minHeight : '2vh',
    minWidth: '80px',
    opacity: 0.5,
  },
  tab3: {
    minHeight : '2vh',
    minWidth: '80px',
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
    borderRadius: '10px',
    backgroundImage: 'linear-gradient(to bottom,#f7edb5 0,#f5e79e 100%)',
    backgroundColor: '#f5e79e',
  },
}));

function onError(error, errorInfo, props) {
  // you can report Error to service here
  console.error('onError:', error.message);
}


export default function Left(props) {
  const classes = useStyles();
  const [value, setValue] = useState(0);
  const [bookmarks, setBookmarks] = useState([]);

  useEffect(() => {
    const load = () => {
      try { setBookmarks(JSON.parse(localStorage.getItem(BOOKMARK_KEY) || '[]')); } catch {}
    };
    load();
    window.addEventListener('storage', load);
    const interval = setInterval(load, 1000);
    return () => { window.removeEventListener('storage', load); clearInterval(interval); };
  }, []);

  const removeBookmark = (id, e) => {
    e.stopPropagation();
    const next = bookmarks.filter(b => b.id !== id);
    setBookmarks(next);
    localStorage.setItem(BOOKMARK_KEY, JSON.stringify(next));
  };

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
            <Tab variant="body1" classes={{root: classes.tab1}} label="Search" {...a11yProps(0)} />
            <Tab variant="body1" classes={{root: classes.tab2}} label="Hierarchy" {...a11yProps(1)} />
            <Tab variant="body1" classes={{root: classes.tab3}} label="ECL" {...a11yProps(2)} />
            <Tab variant="body1" classes={{root: classes.tab3}}
              label={<span>⭐{bookmarks.length > 0 ? ` ${bookmarks.length}` : ''}</span>}
              {...a11yProps(3)} />
          </Tabs>
        </Toolbar>
        
        <TabPanel value={value} index={0}>
          <Search setFromId={props.setFromId} setMrcmFromSearch={props.setMrcmFromSearch}/>
        </TabPanel>
        <TabPanel value={value} index={1}>
          <Container
            className={classes.container} /*ref={setRef}*/
            style={{
              margin : "0 0 0 0",
              padding: "12px 0 0 12px",
              height: "88vh",
              overflow: "scroll"}}>
          <ErrorBoundary>
          <Hierarchy
            setFromId={props.setFromId}
          />
          </ErrorBoundary>
          </Container>
        </TabPanel>
        <TabPanel value={value} index={2}>
          <Ecl setFromId={props.setFromId} />
        </TabPanel>
        <TabPanel value={value} index={3}>
          <div style={{ padding: '8px 4px' }}>
            {bookmarks.length === 0 ? (
              <div className="empty-state" style={{ padding: '32px 12px' }}>
                <div style={{ fontSize: 28 }}>⭐</div>
                <div style={{ marginTop: 8 }}>No bookmarks yet</div>
                <div style={{ fontSize: 11, marginTop: 4 }}>Click ☆ Bookmark on any concept</div>
              </div>
            ) : (
              <>
                <div style={{ fontSize: 11, color: '#98a2b3', padding: '4px 12px 8px', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  {bookmarks.length} bookmark{bookmarks.length !== 1 ? 's' : ''}
                </div>
                {bookmarks.map((bm, i) => (
                  <div
                    key={i}
                    className="bookmark-item"
                    onClick={() => props.setFromId(bm.id)}
                  >
                    <span style={{ fontSize: 14 }}>⭐</span>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontSize: 12, fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{bm.term}</div>
                      <div className="bm-id">{bm.id}</div>
                    </div>
                    <span
                      style={{ fontSize: 11, color: '#98a2b3', cursor: 'pointer', padding: '0 4px' }}
                      onClick={(e) => removeBookmark(bm.id, e)}
                    >✕</span>
                  </div>
                ))}
              </>
            )}
          </div>
        </TabPanel>
      </Grid>
    </Grid>

    </MuiThemeProvider>
  );
}
