import React, { useState, useEffect, useCallback } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableHead from '@material-ui/core/TableHead';
import TableBody from '@material-ui/core/TableBody';
import TableRow from '@material-ui/core/TableRow';
import TableCell from '@material-ui/core/TableCell';
import TablePagination from '@material-ui/core/TablePagination';
import TextField from '@material-ui/core/TextField';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import RefreshIcon from '@material-ui/icons/Refresh';
import LinearProgress from '@material-ui/core/LinearProgress';
import Chip from '@material-ui/core/Chip';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';

const useStyles = makeStyles(() => ({
  root: { display: 'flex', flexDirection: 'column', height: '100%', background: '#fff' },
  toolbar: {
    display: 'flex', alignItems: 'center', gap: 8, padding: '8px 12px',
    borderBottom: '1px solid #e5e7eb', background: '#f8fafc', flexShrink: 0, flexWrap: 'wrap',
  },
  statsRow: {
    display: 'flex', gap: 12, padding: '8px 12px', flexShrink: 0,
    borderBottom: '1px solid #e5e7eb', background: '#fff', flexWrap: 'wrap',
  },
  statBox: {
    padding: '4px 12px', borderRadius: 6, background: '#f1f5f9',
    display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: 80,
  },
  statVal: { fontSize: '1.1em', fontWeight: 700, color: '#1e2d40' },
  statLabel: { fontSize: '0.68em', color: '#64748b' },
  filterInput: { background: '#fff', '& input': { padding: '6px 8px', fontSize: '0.8em' } },
  methodSelect: { background: '#fff', fontSize: '0.8em', '& select': { padding: '6px 8px' } },
  tableWrap: { flex: 1, overflow: 'auto' },
  th: { fontSize: '0.75em', fontWeight: 700, padding: '6px 10px', background: '#f8fafc', color: '#475569', whiteSpace: 'nowrap' },
  td: { fontSize: '0.75em', padding: '4px 10px', maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' },
  methodChip: { height: 18, fontSize: '0.65em', fontWeight: 700 },
  statusOk: { color: '#16a34a' },
  statusErr: { color: '#dc2626' },
  pagination: { borderTop: '1px solid #e5e7eb', flexShrink: 0 },
}));

const METHOD_COLORS = { GET: '#0284c7', POST: '#7c3aed', PUT: '#d97706', DELETE: '#dc2626' };

export default function ActivityPanel() {
  const classes = useStyles();
  const [rows, setRows]       = useState([]);
  const [total, setTotal]     = useState(0);
  const [page, setPage]       = useState(0);
  const [rowsPerPage]         = useState(50);
  const [loading, setLoading] = useState(false);

  const [filterMethod, setFilterMethod] = useState('');
  const [filterPath, setFilterPath]     = useState('');
  const [filterIp, setFilterIp]         = useState('');
  const [filterFrom, setFilterFrom]     = useState('');
  const [filterTo, setFilterTo]         = useState('');
  const [stats, setStats]               = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    const q = new URLSearchParams();
    q.set('page', page + 1);
    q.set('size', rowsPerPage);
    if (filterMethod) q.set('method', filterMethod);
    if (filterPath)   q.set('path', filterPath);
    if (filterIp)     q.set('ip', filterIp);
    if (filterFrom)   q.set('from', filterFrom);
    if (filterTo)     q.set('to', filterTo);

    fetch(`/fhir/$access-log?${q}`)
      .then(r => r.json())
      .then(data => { setRows(data.items || []); setTotal(data.total || 0); })
      .finally(() => setLoading(false));
  }, [page, rowsPerPage, filterMethod, filterPath, filterIp, filterFrom, filterTo]);

  const loadStats = useCallback(() => {
    fetch('/fhir/$access-log/stats')
      .then(r => r.json())
      .then(setStats)
      .catch(() => {});
  }, []);

  useEffect(() => { load(); }, [load]);
  useEffect(() => { loadStats(); }, [loadStats]);

  const handleFilter = (setter) => (e) => {
    setter(e.target.value);
    setPage(0);
  };

  return (
    <div className={classes.root}>
      {/* 통계 요약 */}
      {stats && (
        <div className={classes.statsRow}>
          <div className={classes.statBox}>
            <span className={classes.statVal}>{stats.todayTotal ?? 0}</span>
            <span className={classes.statLabel}>오늘 요청</span>
          </div>
          {(stats.byMethod || []).map(m => (
            <div key={m.method} className={classes.statBox} style={{ background: `${METHOD_COLORS[m.method] || '#64748b'}11` }}>
              <span className={classes.statVal} style={{ color: METHOD_COLORS[m.method] || '#64748b' }}>{m.cnt}</span>
              <span className={classes.statLabel}>{m.method}</span>
            </div>
          ))}
          {(stats.topIps || []).slice(0, 3).map(ip => (
            <div key={ip.ip} className={classes.statBox}>
              <span className={classes.statVal} style={{ fontSize: '0.85em' }}>{ip.cnt}</span>
              <span className={classes.statLabel}>{ip.ip}</span>
            </div>
          ))}
        </div>
      )}

      {/* 필터 바 */}
      <div className={classes.toolbar}>
        <Select value={filterMethod} onChange={handleFilter(setFilterMethod)}
          displayEmpty className={classes.methodSelect} variant="outlined"
          style={{ height: 32, fontSize: '0.8em', minWidth: 90 }}>
          <MenuItem value="">전체 메서드</MenuItem>
          {['GET','POST','PUT','DELETE'].map(m => <MenuItem key={m} value={m}>{m}</MenuItem>)}
        </Select>
        <TextField placeholder="Path 검색" value={filterPath} onChange={handleFilter(setFilterPath)}
          variant="outlined" size="small" className={classes.filterInput} style={{ width: 160 }} />
        <TextField placeholder="IP 검색" value={filterIp} onChange={handleFilter(setFilterIp)}
          variant="outlined" size="small" className={classes.filterInput} style={{ width: 130 }} />
        <TextField type="date" label="From" value={filterFrom} onChange={handleFilter(setFilterFrom)}
          variant="outlined" size="small" className={classes.filterInput} InputLabelProps={{ shrink: true }} style={{ width: 140 }} />
        <TextField type="date" label="To" value={filterTo} onChange={handleFilter(setFilterTo)}
          variant="outlined" size="small" className={classes.filterInput} InputLabelProps={{ shrink: true }} style={{ width: 140 }} />
        <Tooltip title="새로고침">
          <IconButton size="small" onClick={() => { load(); loadStats(); }}><RefreshIcon fontSize="small" /></IconButton>
        </Tooltip>
        <Typography variant="caption" style={{ color: '#94a3b8', marginLeft: 'auto' }}>
          총 {total.toLocaleString()}건
        </Typography>
      </div>

      {loading && <LinearProgress style={{ flexShrink: 0 }} />}

      {/* 테이블 */}
      <div className={classes.tableWrap}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              {['시간','메서드','경로','쿼리','IP','상태','응답시간'].map(h => (
                <TableCell key={h} className={classes.th}>{h}</TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map(r => (
              <TableRow key={r.id} hover>
                <TableCell className={classes.td} style={{ whiteSpace: 'nowrap', minWidth: 130 }}>
                  {r.ts ? r.ts.replace('T', ' ').substring(0, 19) : ''}
                </TableCell>
                <TableCell className={classes.td}>
                  <Chip label={r.method} size="small" className={classes.methodChip}
                    style={{ background: `${METHOD_COLORS[r.method] || '#64748b'}22`, color: METHOD_COLORS[r.method] || '#64748b' }} />
                </TableCell>
                <TableCell className={classes.td} style={{ maxWidth: 220 }}>
                  <Tooltip title={r.path || ''}><span>{r.path}</span></Tooltip>
                </TableCell>
                <TableCell className={classes.td} style={{ maxWidth: 180 }}>
                  <Tooltip title={r.query || ''}><span>{r.query || '-'}</span></Tooltip>
                </TableCell>
                <TableCell className={classes.td}>{r.clientIp}</TableCell>
                <TableCell className={classes.td}
                  style={{ color: r.status >= 400 ? '#dc2626' : r.status >= 200 ? '#16a34a' : '#64748b', fontWeight: 600 }}>
                  {r.status}
                </TableCell>
                <TableCell className={classes.td}>{r.durationMs}ms</TableCell>
              </TableRow>
            ))}
            {!loading && rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} style={{ textAlign: 'center', color: '#94a3b8', padding: 32, fontSize: '0.8em' }}>
                  로그가 없습니다
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      <TablePagination
        className={classes.pagination}
        component="div"
        count={total}
        rowsPerPage={rowsPerPage}
        page={page}
        onChangePage={(_, p) => setPage(p)}
        rowsPerPageOptions={[50]}
        labelDisplayedRows={({ from, to, count }) => `${from}-${to} / ${count}`}
      />
    </div>
  );
}
