import React, { useState, useEffect, createContext } from 'react';
import axios from 'axios';
import Main from './main.js';
import Left from './left.js';
import Right from './right.js';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import Grid from "@material-ui/core/Grid";
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import { BrowserRouter as Router, Route, Switch, Redirect, Link } from "react-router-dom";

const useStyles = makeStyles((theme) => ({
  label: {
    fontSize: '0.9em',
  },
  gridcontainer: {
    position:"relative",
  },
  border: {
    borderRight: "1px solid text.secondary",
  },
  borderright: {
    borderRight: "1px solid #ffffff",
  },
  versionBar: {
    padding: '4px 12px',
    backgroundColor: '#162030',
    borderBottom: '1px solid #0f1923',
    display: 'flex',
    alignItems: 'center',
    gap: 8,
  },
  versionLabel: {
    fontSize: '0.75em',
    color: '#60a5fa',
    fontWeight: 600,
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
  },
  versionSelect: {
    fontSize: '0.8em',
    color: '#cbd5e1',
    '& .MuiSelect-root': { paddingTop: 4, paddingBottom: 4, color: '#cbd5e1' },
    '& .MuiSvgIcon-root': { color: '#64748b' },
  },
}));

function formatVersionLabel(scheme) {
  const d = scheme.date || '';
  const dateStr = d.length === 8
    ? `${d.slice(0,4)}-${d.slice(4,6)}-${d.slice(6,8)}`
    : d;
  return scheme.extensionName
    ? `${dateStr} (${scheme.extensionName})`
    : `${dateStr} (International)`;
}

/*export const AppContext = createContext();
*/
export default function Snomed(props) {

  const classes = useStyles();
  const [versions, setVersions] = useState([]);
  const [selectedVersion, setSelectedVersion] = useState('');

  useEffect(() => {
    axios.get('/version/SNOMEDCT').then(res => {
      const list = res.data || [];
      setVersions(list);
      if (list.length > 0) setSelectedVersion(list[0].version);
    }).catch(() => {});
  }, []);

  return (
    <>
      {versions.length > 0 && (
        <div className={classes.versionBar}>
          <span className={classes.versionLabel}>Release</span>
          <Select
            value={selectedVersion}
            onChange={e => setSelectedVersion(e.target.value)}
            disableUnderline
            className={classes.versionSelect}
          >
            {versions.map(v => (
              <MenuItem key={v.version} value={v.version} style={{ fontSize: '0.85em' }}>
                {formatVersionLabel(v)}
              </MenuItem>
            ))}
          </Select>
        </div>
      )}

      <Grid container
        className={clsx(classes.gridcontainer, classes.border)}
      >
          <Grid item md={3}>
            <Left setFromId={props.setFromId} setMrcmFromMain={props.setMrcmFromMain} setMrcmFromSearch={props.setMrcmFromSearch} version={selectedVersion} />
          </Grid>
          <Grid item md={6}>
            <Main
              id={props.fromId}
              setFromId={props.setFromId}
              setMrcmFromMain={props.setMrcmFromMain}
              version={selectedVersion}
            />
          </Grid>
          <Grid item className={clsx(classes.borderright)} md={3}>
            <Right id={props.fromId} setFromId={props.setFromId} mrcmFromMain={props.mrcmFromMain} mrcmFromSearch={props.mrcmFromSearch} version={selectedVersion}/>
          </Grid>
      </Grid>
    </>
  )
}
