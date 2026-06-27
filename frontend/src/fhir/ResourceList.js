import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import LinearProgress from '@material-ui/core/LinearProgress';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import InputAdornment from '@material-ui/core/InputAdornment';
import Chip from '@material-ui/core/Chip';
import SearchIcon from '@material-ui/icons/Search';
import AddIcon from '@material-ui/icons/Add';

const STATUS_COLOR = {
  active:   { bg: '#dcfce7', color: '#15803d' },
  draft:    { bg: '#fef9c3', color: '#a16207' },
  retired:  { bg: '#fee2e2', color: '#b91c1c' },
};

const useStyles = makeStyles(() => ({
  root: { display: 'flex', flexDirection: 'column', height: '100%' },
  header: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '16px 24px 12px', borderBottom: '1px solid #e5e7eb',
    background: '#fff',
  },
  title: { fontSize: '1.1em', fontWeight: 600, color: '#111827' },
  addBtn: {
    background: '#2563eb', color: '#fff', fontSize: '0.78em',
    padding: '5px 14px', minWidth: 0,
    '&:hover': { background: '#1d4ed8' },
  },
  searchBar: { padding: '12px 24px', background: '#f9fafb', borderBottom: '1px solid #e5e7eb' },
  searchField: { background: '#fff', borderRadius: 8 },
  table: { flex: 1, overflowY: 'auto' },
  thead: {
    display: 'grid', gridTemplateColumns: '1fr 2fr 1fr 80px',
    padding: '8px 24px', background: '#f9fafb',
    borderBottom: '1px solid #e5e7eb',
  },
  theadCell: { color: '#6b7280', fontSize: '0.72em', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' },
  row: {
    display: 'grid', gridTemplateColumns: '1fr 2fr 1fr 80px',
    padding: '11px 24px', borderBottom: '1px solid #f3f4f6',
    cursor: 'pointer', transition: 'background 0.1s',
    alignItems: 'center',
    '&:hover': { background: '#f0f7ff' },
  },
  cell: { fontSize: '0.82em', color: '#374151', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', paddingRight: 8 },
  cellBold: { fontSize: '0.82em', fontWeight: 600, color: '#111827', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', paddingRight: 8 },
  empty: { textAlign: 'center', padding: 48, color: '#9ca3af', fontSize: '0.85em' },
}));

function StatusChip({ status }) {
  const s = STATUS_COLOR[status] || { bg: '#f3f4f6', color: '#6b7280' };
  return (
    <span style={{
      background: s.bg, color: s.color,
      padding: '2px 8px', borderRadius: 9999,
      fontSize: '0.72em', fontWeight: 600,
    }}>{status || '-'}</span>
  );
}

export default function ResourceList({ resourceType, isAdmin, onSelect, onNew }) {
  const classes = useStyles();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');

  const load = (q) => {
    setLoading(true);
    const params = q ? `?name=${encodeURIComponent(q)}` : '';
    axios.get(`/fhir/${resourceType}${params}`)
      .then(res => {
        const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
        const entries = (data.entry || []).map(e => e.resource).filter(Boolean);
        setItems(entries);
      })
      .catch(() => setItems([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { setSearch(''); load(''); }, [resourceType]); // eslint-disable-line

  // 검색어 입력 후 500ms debounce로 백엔드 호출
  useEffect(() => {
    const t = setTimeout(() => load(search), 400);
    return () => clearTimeout(t);
  }, [search]); // eslint-disable-line

  const filtered = items;

  return (
    <div className={classes.root}>
      <div className={classes.header}>
        <Typography className={classes.title}>{resourceType}</Typography>
        {isAdmin && (
          <Button className={classes.addBtn} variant="contained" size="small" startIcon={<AddIcon />} onClick={onNew}>
            New
          </Button>
        )}
      </div>
      <div className={classes.searchBar}>
        <TextField
          className={classes.searchField}
          size="small" variant="outlined" fullWidth
          placeholder={`Search ${resourceType}...`}
          value={search} onChange={e => setSearch(e.target.value)}
          InputProps={{
            startAdornment: <InputAdornment position="start"><SearchIcon style={{ fontSize: 18, color: '#9ca3af' }} /></InputAdornment>,
            style: { fontSize: '0.85em' },
          }}
        />
      </div>
      {loading && <LinearProgress style={{ borderRadius: 0 }} />}
      <div className={classes.table}>
        <div className={classes.thead}>
          <span className={classes.theadCell}>ID / Name</span>
          <span className={classes.theadCell}>URL</span>
          <span className={classes.theadCell}>Version</span>
          <span className={classes.theadCell}>Status</span>
        </div>
        {filtered.length === 0 && !loading && (
          <div className={classes.empty}>항목이 없습니다.</div>
        )}
        {filtered.map((item, i) => (
          <div key={item.id || i} className={classes.row} onClick={() => onSelect(item.id)}>
            <div>
              <div className={classes.cellBold}>{item.name || item.id}</div>
              <div style={{ fontSize: '0.72em', color: '#9ca3af' }}>{item.id}</div>
            </div>
            <div className={classes.cell} title={item.url}>{item.url || '-'}</div>
            <div className={classes.cell}>{item.version || '-'}</div>
            <div><StatusChip status={item.status} /></div>
          </div>
        ))}
      </div>
    </div>
  );
}
