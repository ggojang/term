import React, { useState, useEffect, useCallback } from 'react';
import Snomed from './snomed/layout.js';
import RefsetLayout from './refsetViewer/layout.js';
import MapLayout from './map/layout.js';
import LoincLayout from './loinc/layout.js';
import Icd10Layout from './icd10/layout.js';
import PropTypes from 'prop-types';
import CssBaseline from "@material-ui/core/CssBaseline";
import clsx from 'clsx';
import Grid from "@material-ui/core/Grid";
import { makeStyles, useTheme } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';

// ── URL hash helpers ──────────────────────────────────────────────────────────
function parseHash() {
  const params = new URLSearchParams(window.location.hash.slice(1));
  return {
    tab:     parseInt(params.get('t') || '0', 10),
    snomedId: params.get('id')    || '138875005',
    loincId:  params.get('loinc') || '',
    kcdCode:  params.get('code')  || '',
  };
}

function buildHash(tab, snomedId, loincId, kcdCode) {
  const p = new URLSearchParams();
  p.set('t', tab);
  if (tab === 0 && snomedId) p.set('id', snomedId);
  if (tab === 3 && loincId)  p.set('loinc', loincId);
  if (tab === 4 && kcdCode)  p.set('code', kcdCode);
  return '#' + p.toString();
}

// ── TabPanel: always mounted, only hidden ────────────────────────────────────
function TabPanel({ children, value, index }) {
  return (
    <div
      role="tabpanel"
      id={`action-tabpanel-${index}`}
      aria-labelledby={`action-tab-${index}`}
      style={{ display: value !== index ? 'none' : '' }}
    >
      <Box>{children}</Box>
    </div>
  );
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index:    PropTypes.any.isRequired,
  value:    PropTypes.any.isRequired,
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
  appbar:    { backgroundColor: '#2e3e4e', padding: '4px' },
  indicator: { backgroundColor: '#2e3e4e' },
  toolbar:   { minHeight: '40px' },
  label:     { fontSize: '1.3em' },
  msg:       { fontSize: '1em' },
  tab1: { minHeight: '50px', minWidth: '150px', opacity: 0.5 },
  tab2: { minHeight: '50px', minWidth: '110px', opacity: 0.5 },
  tab3: { minHeight: '50px', minWidth: '65px',  opacity: 0.5 },
}));

const TAB_MSGS = [
  'International Edition 2026.06.01',
  'International Edition 2026.06.01',
  'International Edition 2026.06.01',
  'Version 2.82 (2026-02-24)',
  'KCD-9 Browser',
];

export default function App() {
  const classes = useStyles();

  // ── initialise from URL hash ──────────────────────────────────────────────
  const initial = parseHash();
  const [value,    setValue]    = useState(initial.tab);
  const [fromId,   setFromIdRaw]   = useState(initial.snomedId);
  const [loincId,  setLoincIdRaw]  = useState(initial.loincId);
  const [kcdCode,  setKcdCodeRaw]  = useState(initial.kcdCode);
  const [mrcmFromMain,   setMrcmFromMain]   = useState([]);
  const [mrcmFromSearch, setMrcmFromSearch] = useState('');

  document.title = 'InfoClinic STOM Browser';

  // ── push history whenever meaningful state changes ────────────────────────
  const pushHistory = useCallback((tab, snomed, loinc, kcd) => {
    const hash = buildHash(tab, snomed, loinc, kcd);
    if (window.location.hash !== hash) {
      window.history.pushState({ tab, snomed, loinc, kcd }, '', hash);
    }
  }, []);

  // wrapped setters that also update URL
  const setFromId = useCallback((id) => {
    setFromIdRaw(id);
    pushHistory(0, id, loincId, kcdCode);
  }, [loincId, kcdCode, pushHistory]);

  const setLoincId = useCallback((id) => {
    setLoincIdRaw(id);
    pushHistory(3, fromId, id, kcdCode);
  }, [fromId, kcdCode, pushHistory]);

  const setKcdCode = useCallback((code) => {
    setKcdCodeRaw(code);
    pushHistory(4, fromId, loincId, code);
  }, [fromId, loincId, pushHistory]);

  const handleChange = (event, newValue) => {
    setValue(newValue);
    pushHistory(newValue, fromId, loincId, kcdCode);
  };

  // ── popstate: browser back/forward ───────────────────────────────────────
  useEffect(() => {
    const onPop = () => {
      const h = parseHash();
      setValue(h.tab);
      setFromIdRaw(h.snomedId);
      setLoincIdRaw(h.loincId);
      setKcdCodeRaw(h.kcdCode);
    };
    window.addEventListener('popstate', onPop);
    // write initial hash without pushing a new entry
    const hash = buildHash(initial.tab, initial.snomedId, initial.loincId, initial.kcdCode);
    window.history.replaceState(
      { tab: initial.tab, snomed: initial.snomedId, loinc: initial.loincId, kcd: initial.kcdCode },
      '', hash
    );
    return () => window.removeEventListener('popstate', onPop);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const msg = TAB_MSGS[value] || '';

  return (
    <>
      <CssBaseline />
      <div className={clsx(classes.root, classes.toolbar)}>
        <AppBar position="sticky" className={classes.appbar}>
          <Toolbar className={classes.toolbar}>
            <Grid container alignItems="center">
              <Grid item md={1}>
                <Typography variant="h5" color="inherit" noWrap>InfoClinic</Typography>
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
                  <Tab className={clsx(classes.label, classes.tab3)} label="Refset Viewer"      {...a11yProps(1)} />
                  <Tab className={clsx(classes.label, classes.tab2)} label="Mapping Support"    {...a11yProps(2)} />
                  <Tab className={clsx(classes.label, classes.tab3)} label="LOINC Browser"      {...a11yProps(3)} />
                  <Tab className={clsx(classes.label, classes.tab3)} label="KCD-9 Browser"      {...a11yProps(4)} />
                </Tabs>
              </Grid>
              <Grid item md={2}>
                <Typography className={classes.msg} color="inherit" noWrap>{msg}</Typography>
              </Grid>
            </Grid>
          </Toolbar>
        </AppBar>

        <TabPanel value={value} index={0}>
          <Snomed
            fromId={fromId}
            setFromId={setFromId}
            mrcmFromMain={mrcmFromMain}
            mrcmFromSearch={mrcmFromSearch}
            setMrcmFromMain={setMrcmFromMain}
            setMrcmFromSearch={setMrcmFromSearch}
          />
        </TabPanel>
        <TabPanel value={value} index={1}>
          <RefsetLayout />
        </TabPanel>
        <TabPanel value={value} index={2}>
          <MapLayout />
        </TabPanel>
        <TabPanel value={value} index={3}>
          <LoincLayout loincId={loincId} setLoincId={setLoincId} />
        </TabPanel>
        <TabPanel value={value} index={4}>
          <Icd10Layout selectedCode={kcdCode} setSelectedCode={setKcdCode} />
        </TabPanel>
      </div>
    </>
  );
}
