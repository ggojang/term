import React, { useEffect, useState, useRef } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import CircularProgress from '@material-ui/core/CircularProgress';
import Tooltip from '@material-ui/core/Tooltip';
import IconButton from '@material-ui/core/IconButton';
import CloudUploadIcon from '@material-ui/icons/CloudUpload';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import CheckCircleOutlineIcon from '@material-ui/icons/CheckCircleOutline';
import ErrorOutlineIcon from '@material-ui/icons/ErrorOutline';

const RESOURCE_TYPES = [
  { type: 'CodeSystem',   icon: '⬡', color: '#60a5fa' },
  { type: 'ValueSet',     icon: '◈', color: '#34d399' },
  { type: 'ConceptMap',   icon: '⇄', color: '#f59e0b' },
  { type: 'NamingSystem', icon: '⬕', color: '#a78bfa' },
];

const useStyles = makeStyles(() => ({
  sidebar: {
    width: 220, minWidth: 220, background: '#111827',
    height: '100%', display: 'flex', flexDirection: 'column',
    borderRight: '1px solid #1f2937', overflowY: 'auto',
  },
  sectionHeader: {
    padding: '16px 16px 6px', color: '#4b5563',
    fontSize: '0.65em', fontWeight: 700, letterSpacing: '0.1em', textTransform: 'uppercase',
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
  },
  resourceItem: {
    display: 'flex', alignItems: 'center', padding: '9px 16px',
    cursor: 'pointer', borderRadius: 6, margin: '1px 8px', transition: 'background 0.15s',
    '&:hover': { background: '#1f2937' },
  },
  active: { background: '#1e3a5f !important' },
  resourceIcon: {
    width: 26, height: 26, borderRadius: 6, display: 'flex', alignItems: 'center',
    justifyContent: 'center', fontSize: '0.9em', marginRight: 10, flexShrink: 0,
  },
  resourceLabel: { color: '#e5e7eb', fontSize: '0.82em', fontWeight: 500, flex: 1 },
  badge: {
    background: '#1f2937', color: '#9ca3af', fontSize: '0.7em',
    padding: '1px 7px', borderRadius: 10, minWidth: 22, textAlign: 'center',
  },
  divider: { borderTop: '1px solid #1f2937', margin: '8px 16px' },

  // IG 섹션
  igItem: {
    display: 'flex', alignItems: 'center', padding: '7px 16px 7px 12px',
    cursor: 'pointer', margin: '1px 8px', borderRadius: 6, transition: 'background 0.15s',
    '&:hover': { background: '#1f2937' },
  },
  igActive: { background: '#1e3a5f !important' },
  igLabel: { color: '#d1d5db', fontSize: '0.78em', flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' },
  igVersion: {
    color: '#1e2d40', background: '#60a5fa', fontSize: '0.62em', fontWeight: 700,
    padding: '1px 5px', borderRadius: 4, marginLeft: 6, flexShrink: 0, whiteSpace: 'nowrap',
  },
  igSubItem: {
    display: 'flex', alignItems: 'center',
    padding: '6px 16px 6px 28px', cursor: 'pointer',
    margin: '0 8px', borderRadius: 6, transition: 'background 0.12s',
    '&:hover': { background: '#1f2937' },
  },
  igSubActive: { background: '#1e3a5f !important' },
  igSubIcon: { fontSize: '0.75em', marginRight: 8, flexShrink: 0 },
  igSubLabel: { color: '#9ca3af', fontSize: '0.76em', flex: 1 },
  igSubBadge: { color: '#6b7280', fontSize: '0.68em' },

  uploadBtn: { color: '#4b5563', padding: 2, '&:hover': { color: '#9ca3af' } },
  uploadInput: { display: 'none' },
  uploadStatus: { padding: '6px 16px', fontSize: '0.72em', display: 'flex', alignItems: 'center', gap: 6 },
  footer: { marginTop: 'auto', padding: '12px 16px', borderTop: '1px solid #1f2937' },
  footerText: { color: '#374151', fontSize: '0.7em' },
}));

export default function FhirTree({ selected, selectedIg, onSelect, onSelectIg, isAdmin }) {
  const classes = useStyles();
  const [counts, setCounts]       = useState({});
  const [packages, setPackages]   = useState([]);
  const [expandedIg, setExpandedIg] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadMsg, setUploadMsg] = useState(null); // {ok, text}
  const fileRef = useRef();

  useEffect(() => {
    RESOURCE_TYPES.forEach(({ type }) => {
      axios.get(`/fhir/${type}`).then(res => {
        try {
          const d = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
          setCounts(prev => ({ ...prev, [type]: d.total ?? 0 }));
        } catch (_) {}
      }).catch(() => {});
    });
    loadPackages();
  }, []);

  const loadPackages = () => {
    axios.get('/fhir/Package').then(res => setPackages(res.data || [])).catch(() => {});
  };

  const handleUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploading(true); setUploadMsg(null);
    const form = new FormData();
    form.append('package', file);
    axios.post('/fhir/$install-package', form, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then(res => {
        const d = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
        const msg = d.parameter?.find(p => p.name === 'message')?.valueString || '설치 완료';
        setUploadMsg({ ok: true, text: msg });
        loadPackages();
      })
      .catch(err => setUploadMsg({ ok: false, text: '설치 실패: ' + (err.message || '') }))
      .finally(() => { setUploading(false); e.target.value = ''; });
  };

  const isIgSubSelected = (igId, type) => selectedIg === igId && selected === type;

  return (
    <div className={classes.sidebar}>
      {/* Terminology 섹션 */}
      <div className={classes.sectionHeader}>Terminology</div>
      {RESOURCE_TYPES.map(({ type, icon, color }) => (
        <Tooltip key={type} title={type} placement="right">
          <div
            className={`${classes.resourceItem} ${!selectedIg && selected === type ? classes.active : ''}`}
            onClick={() => onSelect(type)}
          >
            <div className={classes.resourceIcon} style={{ background: color + '22', color }}>{icon}</div>
            <Typography className={classes.resourceLabel}>{type}</Typography>
            <span className={classes.badge}>
              {counts[type] !== undefined ? counts[type] : <CircularProgress size={10} style={{ color: '#4b5563' }} />}
            </span>
          </div>
        </Tooltip>
      ))}

      <div className={classes.divider} />

      {/* IG 패키지 섹션 */}
      <div className={classes.sectionHeader}>
        <span>IG Packages</span>
        {isAdmin && (
          <>
            <input ref={fileRef} className={classes.uploadInput} type="file" accept=".tgz,.tar.gz"
              onChange={handleUpload} />
            <Tooltip title="IG 패키지 업로드 (.tgz)">
              <span>
                <IconButton className={classes.uploadBtn} size="small" onClick={() => fileRef.current.click()}
                  disabled={uploading}>
                  {uploading ? <CircularProgress size={14} style={{ color: '#6b7280' }} /> : <CloudUploadIcon style={{ fontSize: 16 }} />}
                </IconButton>
              </span>
            </Tooltip>
          </>
        )}
      </div>

      {uploadMsg && (
        <div className={classes.uploadStatus}>
          {uploadMsg.ok
            ? <CheckCircleOutlineIcon style={{ fontSize: 14, color: '#34d399' }} />
            : <ErrorOutlineIcon style={{ fontSize: 14, color: '#f87171' }} />}
          <span style={{ color: uploadMsg.ok ? '#34d399' : '#f87171', fontSize: '0.7em' }}>
            {uploadMsg.text.slice(0, 60)}
          </span>
        </div>
      )}

      {packages.length === 0 && (
        <div style={{ padding: '6px 16px', color: '#374151', fontSize: '0.72em' }}>
          {isAdmin ? '↑ .tgz 업로드' : '등록된 IG 없음'}
        </div>
      )}

      {packages.map(pkg => (
        <div key={pkg.id}>
          <div
            className={`${classes.igItem} ${expandedIg === pkg.id && !selected ? classes.igActive : ''}`}
            onClick={() => setExpandedIg(expandedIg === pkg.id ? null : pkg.id)}
          >
            {expandedIg === pkg.id
              ? <ExpandMoreIcon style={{ fontSize: 14, color: '#6b7280', marginRight: 6 }} />
              : <ChevronRightIcon style={{ fontSize: 14, color: '#6b7280', marginRight: 6 }} />}
            <Typography className={classes.igLabel} title={pkg.id}>{pkg.name}</Typography>
            <span className={classes.igVersion}>v{pkg.version}</span>
          </div>

          {expandedIg === pkg.id && RESOURCE_TYPES.map(({ type, icon, color }) => {
            const cnt = pkg.counts?.[type];
            if (!cnt) return null;
            return (
              <div
                key={type}
                className={`${classes.igSubItem} ${isIgSubSelected(pkg.id, type) ? classes.igSubActive : ''}`}
                onClick={() => onSelectIg(pkg.id, type)}
              >
                <span className={classes.igSubIcon} style={{ color }}>{icon}</span>
                <Typography className={classes.igSubLabel}>{type}</Typography>
                <span className={classes.igSubBadge}>{cnt}</span>
              </div>
            );
          })}
        </div>
      ))}

      <div className={classes.footer}>
        <Typography className={classes.footerText}>FHIR R4 · Terminology</Typography>
      </div>
    </div>
  );
}
