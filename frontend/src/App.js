import React, { useState, useEffect, useCallback, useRef } from 'react';
import Snomed from './snomed/layout.js';
import EHRLayout from './ehr/layout.js';
import RefsetLayout from './refsetViewer/layout.js';
import MapLayout from './map/layout.js';
import LoincLayout from './loinc/layout.js';
import Icd10Layout from './icd10/layout.js';
import HiraLayout from './hira/layout.js';
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

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: theme.palette.background.paper,
    width: '100vw',
    position: 'relative',
    minHeight: '100vh',
    fontSize: '0.8em',
  },
  appbar: {
    backgroundColor: '#2e3e4e',
    padding: '4px',
  },
  indicator: {
    backgroundColor: '#2e3e4e',
  },
  toolbar: {
    minHeight: '40px',
  },
  label: {
    fontSize: '1.3em',
  },
  msg: {
    fontSize: '1em',
  },
  tabs: {
    padding: '0',
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

// 초기 상태를 URL / history.state 에서 복원
function getInitialState() {
  const s = window.history.state;
  if (s && typeof s.tab === 'number') return s;

  const urlId = window.location.href.split('/').reverse()[0];
  const fromId = (!urlId || isNaN(Number(urlId))) ? '138875005' : urlId;
  return { tab: 0, fromId, kcdCode: '', loincId: '' };
}

export default function App() {
  const classes = useStyles();
  const init = getInitialState();

  const [value, setValue] = useState(init.tab);
  const [fromId, setFromId] = useState(init.fromId);
  const [kcdCode, setKcdCode] = useState(init.kcdCode || '');
  const [loincId, setLoincId] = useState(init.loincId || '');
  const [hiraCode, setHiraCode] = useState(init.hiraCode || null);
  const [mrcmFromMain, setMrcmFromMain] = useState([]);
  const [mrcmFromSearch, setMrcmFromSearch] = useState('');
  const [msg, setMsg] = useState('');

  // 히스토리에 현재 상태를 push (변경이 있을 때만)
  const pushHistory = useCallback((tab, fId, kcd, loinc) => {
    const state = { tab, fromId: fId, kcdCode: kcd, loincId: loinc };
    window.history.pushState(state, '');
  }, []);

  // 뒤로가기 / 앞으로가기 이벤트 처리
  useEffect(() => {
    const onPop = (e) => {
      const s = e.state;
      if (!s) return;
      if (typeof s.tab === 'number') setValue(s.tab);
      if (s.fromId) setFromId(s.fromId);
      if (typeof s.kcdCode === 'string') setKcdCode(s.kcdCode);
      if (typeof s.loincId === 'string') setLoincId(s.loincId);
    };
    window.addEventListener('popstate', onPop);
    // 첫 페이지 로드 시 초기 상태를 replaceState로 등록
    window.history.replaceState(
      { tab: init.tab, fromId: init.fromId, kcdCode: init.kcdCode || '', loincId: init.loincId || '' },
      ''
    );
    return () => window.removeEventListener('popstate', onPop);
  }, []); // eslint-disable-line

  // 탭 변경
  const handleChange = (event, newValue) => {
    setValue(newValue);
    pushHistory(newValue, fromId, kcdCode, loincId);
  };

  // SNOMED CT 개념 이동
  const handleSetFromId = useCallback((id) => {
    setFromId(id);
    pushHistory(value, id, kcdCode, loincId);
  }, [value, kcdCode, loincId, pushHistory]);

  // KCD-9 코드 이동
  const handleSetKcdCode = useCallback((code) => {
    setKcdCode(code);
    pushHistory(value, fromId, code, loincId);
  }, [value, fromId, loincId, pushHistory]);

  // LOINC 이동
  const handleSetLoincId = useCallback((id) => {
    setLoincId(id);
    pushHistory(value, fromId, kcdCode, id);
  }, [value, fromId, kcdCode, pushHistory]);

  document.title = 'InfoClinic STOM Browser';

  useEffect(() => {
    if (value >= 0 && value < 3) setMsg('International Edition 2026.06.01');
    else if (value === 3) setMsg('Version 2.82 (2026-02-24)');
    else if (value === 4) setMsg('Release 2025.12.23');
    else if (value === 5) setMsg('2016 Release (2014-10-14)');
    else if (value === 6) setMsg('2026.06.01 기준');
    else setMsg('');
  }, [value]);

  return (
    <>
      <CssBaseline />
      <div className={clsx(classes.root, classes.toolbar)}>
        <AppBar position="sticky" className={classes.appbar}>
          <Toolbar className={classes.toolbar}>
            <Grid container alignItems="center">
              <Grid item md={1}>
                <Typography variant="h5" color="inherit" noWrap>
                  InfoClinic
                </Typography>
              </Grid>
              <Grid item md={9}>
                <Tabs
                  value={value}
                  onChange={handleChange}
                  classes={{ indicator: classes.indicator }}
                  aria-label="browser tab"
                  variant="standard"
                >
                  <Tab className={clsx(classes.label, classes.tab1)} label="SNOMED CT Browser" {...a11yProps(0)} />
                  <Tab className={clsx(classes.label, classes.tab3)} label="Refset Viewer" {...a11yProps(1)} />
                  <Tab className={clsx(classes.label, classes.tab2)} label="Mapping Support" {...a11yProps(2)} />
                  <Tab className={clsx(classes.label, classes.tab3)} label="LOINC Browser" {...a11yProps(3)} />
                  <Tab className={clsx(classes.label, classes.tab3)} label="KCD-9 Browser" {...a11yProps(4)} />
                  <Tab className={clsx(classes.label, classes.tab3)} label="요양급여청구코드" {...a11yProps(5)} />
                </Tabs>
              </Grid>
              <Grid item md={2}>
                <Typography className={classes.msg} color="inherit" noWrap>
                  {msg}
                </Typography>
              </Grid>
            </Grid>
          </Toolbar>
        </AppBar>

        <TabPanel value={value} index={0}>
          <Snomed
            fromId={fromId}
            setMrcmFromMain={setMrcmFromMain}
            setMrcmFromSearch={setMrcmFromSearch}
            setFromId={handleSetFromId}
            mrcmFromMain={mrcmFromMain}
            mrcmFromSearch={mrcmFromSearch}
          />
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
            setLoincId={handleSetLoincId}
          />
        </TabPanel>
        <TabPanel value={value} index={4}>
          <Icd10Layout
            selectedCode={kcdCode}
            setSelectedCode={handleSetKcdCode}
          />
        </TabPanel>
        <TabPanel value={value} index={5}>
          <HiraLayout
            selectedCode={hiraCode}
            setSelectedCode={setHiraCode}
          />
        </TabPanel>
      </div>
    </>
  );
}
