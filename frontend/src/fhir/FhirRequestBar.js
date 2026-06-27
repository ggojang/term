import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import LinearProgress from '@material-ui/core/LinearProgress';
import PlayArrowIcon from '@material-ui/icons/PlayArrow';
import AddIcon from '@material-ui/icons/Add';
import DeleteIcon from '@material-ui/icons/Delete';
import ContentCopyIcon from '@material-ui/icons/FileCopy';

const useStyles = makeStyles(() => ({
  root: {
    background: '#1a2332', borderBottom: '2px solid #0d1b2a',
    padding: '10px 16px 8px', flexShrink: 0,
  },
  row: { display: 'flex', alignItems: 'center', gap: 6 },
  label: { color: '#4b5563', fontSize: '0.65em', fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 4 },
  baseInput: {
    background: '#0d1b2a', border: '1px solid #1f3452', borderRadius: 5,
    color: '#94a3b8', fontSize: '0.78em', padding: '4px 8px',
    outline: 'none', width: 200, flexShrink: 0,
    '&:focus': { borderColor: '#3b82f6' },
  },
  pathInput: {
    background: '#0d1b2a', border: '1px solid #1f3452', borderRadius: 5,
    color: '#e2e8f0', fontSize: '0.78em', padding: '4px 8px',
    outline: 'none', flex: 1,
    '&:focus': { borderColor: '#3b82f6' },
  },
  slash: { color: '#4b5563', fontSize: '0.9em', flexShrink: 0 },
  runBtn: {
    background: '#2563eb', color: '#fff', borderRadius: 5,
    padding: 4, flexShrink: 0,
    '&:hover': { background: '#1d4ed8' },
  },
  paramsRow: { display: 'flex', alignItems: 'center', gap: 4, marginTop: 5, flexWrap: 'wrap' },
  paramGroup: {
    display: 'flex', alignItems: 'center', gap: 3,
    background: '#0d1b2a', border: '1px solid #1f3452', borderRadius: 5,
    padding: '2px 4px', minWidth: 0,
  },
  paramEq: { color: '#4b5563', fontSize: '0.8em', flexShrink: 0 },
  paramInput: {
    background: 'transparent', border: 'none', outline: 'none',
    color: '#93c5fd', fontSize: '0.75em', padding: '1px 2px',
  },
  paramVal: {
    background: 'transparent', border: 'none', outline: 'none',
    color: '#d1d5db', fontSize: '0.75em', padding: '1px 2px', minWidth: 60,
  },
  addParamBtn: { color: '#4b5563', padding: 2, '&:hover': { color: '#60a5fa' } },
  delParamBtn: { color: '#374151', padding: 1, '&:hover': { color: '#f87171' } },
  urlPreview: {
    marginTop: 4, color: '#374151', fontSize: '0.68em',
    fontFamily: 'monospace', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
  },
  copyBtn: { color: '#374151', padding: 2, '&:hover': { color: '#94a3b8' } },
}));

function baseInputStyle(focused) {
  return {
    background: '#0d1b2a', border: `1px solid ${focused ? '#3b82f6' : '#1f3452'}`, borderRadius: 5,
    color: '#94a3b8', fontSize: '0.78em', padding: '4px 8px',
    outline: 'none', width: 200, flexShrink: 0,
  };
}
function pathInputStyle(focused) {
  return {
    background: '#0d1b2a', border: `1px solid ${focused ? '#3b82f6' : '#1f3452'}`, borderRadius: 5,
    color: '#e2e8f0', fontSize: '0.78em', padding: '4px 8px',
    outline: 'none', flex: 1,
  };
}
function paramInputStyle() {
  return { background: 'transparent', border: 'none', outline: 'none', color: '#93c5fd', fontSize: '0.75em', padding: '1px 2px' };
}
function paramValStyle() {
  return { background: 'transparent', border: 'none', outline: 'none', color: '#d1d5db', fontSize: '0.75em', padding: '1px 2px', minWidth: 60 };
}

/**
 * FhirRequestBar
 * Props:
 *   request: { base: string, path: string, params: [{key, value}] }
 *   onResult: (result: { data, url, error } | null) => void
 *   onRequestChange: (req) => void
 */
export default function FhirRequestBar({ request, onResult, onRequestChange }) {
  const classes = useStyles();
  const [baseFocused, setBaseFocused] = useState(false);
  const [pathFocused, setPathFocused] = useState(false);
  const [loading, setLoading] = useState(false);

  const { base, path, params } = request;

  const buildUrl = () => {
    const qs = params.filter(p => p.key).map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value)}`).join('&');
    return `${base}/${path}${qs ? '?' + qs : ''}`;
  };

  const handleRun = () => {
    const url = buildUrl();
    setLoading(true);
    onResult(null);
    axios.get(url)
      .then(res => {
        const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
        onResult({ data, url, error: null });
      })
      .catch(err => {
        const msg = err.response?.data ? JSON.stringify(err.response.data, null, 2) : err.message;
        onResult({ data: null, url, error: msg });
      })
      .finally(() => setLoading(false));
  };

  const handleKeyDown = (e) => { if (e.key === 'Enter') handleRun(); };

  const setBase = (v) => onRequestChange({ ...request, base: v });
  const setPath = (v) => onRequestChange({ ...request, path: v });
  const setParam = (i, field, v) => {
    const next = params.map((p, idx) => idx === i ? { ...p, [field]: v } : p);
    onRequestChange({ ...request, params: next });
  };
  const addParam = () => onRequestChange({ ...request, params: [...params, { key: '', value: '' }] });
  const delParam = (i) => onRequestChange({ ...request, params: params.filter((_, idx) => idx !== i) });

  const fullUrl = buildUrl();

  return (
    <div className={classes.root}>
      {/* URL 입력 줄 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <input
          style={baseInputStyle(baseFocused)}
          value={base}
          onChange={e => setBase(e.target.value)}
          onFocus={() => setBaseFocused(true)}
          onBlur={() => setBaseFocused(false)}
          onKeyDown={handleKeyDown}
          spellCheck={false}
        />
        <span style={{ color: '#4b5563', fontSize: '0.9em' }}>/</span>
        <input
          style={pathInputStyle(pathFocused)}
          value={path}
          onChange={e => setPath(e.target.value)}
          onFocus={() => setPathFocused(true)}
          onBlur={() => setPathFocused(false)}
          onKeyDown={handleKeyDown}
          placeholder="CodeSystem/$lookup"
          spellCheck={false}
        />
        <Tooltip title="실행 (Enter)">
          <span>
            <IconButton
              style={{ background: '#2563eb', color: '#fff', borderRadius: 6, padding: 5 }}
              size="small"
              onClick={handleRun}
              disabled={loading}
            >
              <PlayArrowIcon style={{ fontSize: 18 }} />
            </IconButton>
          </span>
        </Tooltip>
      </div>

      {/* 파라미터 줄 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginTop: 6, flexWrap: 'wrap' }}>
        {params.map((p, i) => (
          <div key={i} style={{
            display: 'flex', alignItems: 'center', gap: 2,
            background: '#0d1b2a', border: '1px solid #1f3452', borderRadius: 5,
            padding: '2px 4px',
          }}>
            <input
              style={paramInputStyle()}
              value={p.key}
              onChange={e => setParam(i, 'key', e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="key"
              size={Math.max(p.key.length || 4, 4)}
              spellCheck={false}
            />
            <span style={{ color: '#4b5563', fontSize: '0.8em' }}>=</span>
            <input
              style={paramValStyle()}
              value={p.value}
              onChange={e => setParam(i, 'value', e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="value"
              size={Math.max(p.value.length || 8, 8)}
              spellCheck={false}
            />
            <IconButton size="small" style={{ color: '#374151', padding: 1 }} onClick={() => delParam(i)}>
              <DeleteIcon style={{ fontSize: 12 }} />
            </IconButton>
          </div>
        ))}
        <Tooltip title="파라미터 추가">
          <IconButton size="small" style={{ color: '#4b5563', padding: 2 }} onClick={addParam}>
            <AddIcon style={{ fontSize: 14 }} />
          </IconButton>
        </Tooltip>
      </div>

      {/* URL 미리보기 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginTop: 3 }}>
        <span style={{ color: '#374151', fontSize: '0.68em', fontFamily: 'monospace', flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {fullUrl}
        </span>
        <Tooltip title="URL 복사">
          <IconButton size="small" style={{ color: '#374151', padding: 1 }} onClick={() => navigator.clipboard.writeText(fullUrl)}>
            <ContentCopyIcon style={{ fontSize: 12 }} />
          </IconButton>
        </Tooltip>
      </div>

      {loading && <LinearProgress style={{ marginTop: 4, borderRadius: 2 }} />}
    </div>
  );
}
