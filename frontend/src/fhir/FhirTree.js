import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import CircularProgress from '@material-ui/core/CircularProgress';
import Tooltip from '@material-ui/core/Tooltip';

// resource type 정의
const RESOURCE_TYPES = [
  { type: 'CodeSystem',   label: 'CodeSystem',    icon: '⬡', color: '#60a5fa' },
  { type: 'ValueSet',     label: 'ValueSet',      icon: '◈', color: '#34d399' },
  { type: 'ConceptMap',   label: 'ConceptMap',    icon: '⇄', color: '#f59e0b' },
  { type: 'NamingSystem', label: 'NamingSystem',  icon: '⬕', color: '#a78bfa' },
];

const useStyles = makeStyles(() => ({
  sidebar: {
    width: 220,
    minWidth: 220,
    background: '#111827',
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    borderRight: '1px solid #1f2937',
  },
  sectionHeader: {
    padding: '16px 16px 8px',
    color: '#6b7280',
    fontSize: '0.65em',
    fontWeight: 700,
    letterSpacing: '0.1em',
    textTransform: 'uppercase',
  },
  resourceItem: {
    display: 'flex',
    alignItems: 'center',
    padding: '9px 16px',
    cursor: 'pointer',
    borderRadius: 6,
    margin: '1px 8px',
    transition: 'background 0.15s',
    '&:hover': { background: '#1f2937' },
  },
  resourceItemActive: {
    background: '#1e3a5f !important',
  },
  resourceIcon: {
    width: 26,
    height: 26,
    borderRadius: 6,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '0.9em',
    marginRight: 10,
    flexShrink: 0,
  },
  resourceLabel: {
    color: '#e5e7eb',
    fontSize: '0.82em',
    fontWeight: 500,
    flex: 1,
  },
  resourceCount: {
    background: '#1f2937',
    color: '#9ca3af',
    fontSize: '0.7em',
    padding: '1px 7px',
    borderRadius: 10,
    minWidth: 22,
    textAlign: 'center',
  },
  divider: {
    borderTop: '1px solid #1f2937',
    margin: '8px 16px',
  },
  footer: {
    marginTop: 'auto',
    padding: '12px 16px',
    borderTop: '1px solid #1f2937',
  },
  footerText: {
    color: '#374151',
    fontSize: '0.7em',
  },
}));

export default function FhirTree({ selected, onSelect }) {
  const classes = useStyles();
  const [counts, setCounts] = useState({});

  useEffect(() => {
    RESOURCE_TYPES.forEach(({ type }) => {
      axios.get(`/fhir/${type}`)
        .then(res => {
          try {
            const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
            setCounts(prev => ({ ...prev, [type]: data.total ?? 0 }));
          } catch (_) {}
        })
        .catch(() => {});
    });
  }, []);

  return (
    <div className={classes.sidebar}>
      <div className={classes.sectionHeader}>Terminology</div>

      {RESOURCE_TYPES.map(({ type, label, icon, color }) => (
        <Tooltip key={type} title={type} placement="right">
          <div
            className={`${classes.resourceItem} ${selected === type ? classes.resourceItemActive : ''}`}
            onClick={() => onSelect(type)}
          >
            <div className={classes.resourceIcon} style={{ background: color + '22', color }}>
              {icon}
            </div>
            <Typography className={classes.resourceLabel}>{label}</Typography>
            <span className={classes.resourceCount}>
              {counts[type] !== undefined ? counts[type] : <CircularProgress size={10} style={{ color: '#4b5563' }} />}
            </span>
          </div>
        </Tooltip>
      ))}

      <div className={classes.divider} />

      <div className={classes.footer}>
        <Typography className={classes.footerText}>FHIR R4 · Terminology</Typography>
      </div>
    </div>
  );
}
