import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import LinearProgress from '@material-ui/core/LinearProgress';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import EditIcon from '@material-ui/icons/Edit';
import DeleteIcon from '@material-ui/icons/Delete';
import FileCopyIcon from '@material-ui/icons/FileCopy';

const useStyles = makeStyles(() => ({
  root: { display: 'flex', flexDirection: 'column', height: '100%' },
  header: {
    display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between',
    padding: '16px 24px 14px', borderBottom: '1px solid #e5e7eb',
    background: '#fff',
  },
  name: { fontSize: '1.05em', fontWeight: 700, color: '#111827' },
  url:  { fontSize: '0.75em', color: '#6b7280', marginTop: 2 },
  actions: { display: 'flex', gap: 8, flexShrink: 0, marginLeft: 16 },
  editBtn: {
    background: '#2563eb', color: '#fff', fontSize: '0.78em', padding: '5px 12px',
    '&:hover': { background: '#1d4ed8' },
  },
  deleteBtn: {
    background: '#fff', color: '#dc2626', border: '1px solid #fca5a5', fontSize: '0.78em', padding: '5px 12px',
    '&:hover': { background: '#fef2f2' },
  },
  copyBtn: {
    background: '#f9fafb', color: '#374151', border: '1px solid #e5e7eb', fontSize: '0.78em', padding: '5px 12px',
    '&:hover': { background: '#f3f4f6' },
  },
  meta: {
    display: 'flex', gap: 24, padding: '10px 24px',
    background: '#f9fafb', borderBottom: '1px solid #e5e7eb',
  },
  metaItem: {},
  metaLabel: { fontSize: '0.65em', color: '#9ca3af', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' },
  metaValue: { fontSize: '0.8em', color: '#374151', marginTop: 2 },
  jsonWrap: { flex: 1, overflow: 'auto', background: '#0d1117', padding: '20px 24px' },
  json: {
    fontFamily: "'JetBrains Mono', 'Fira Code', 'Consolas', monospace",
    fontSize: '0.8em', lineHeight: 1.7, color: '#e6edf3', whiteSpace: 'pre', margin: 0,
  },
}));

// 간단한 JSON 구문 색상 처리
function colorizeJson(json) {
  return json
    .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+-]?\d+)?)/g, match => {
      if (/^"/.test(match)) {
        if (/:$/.test(match)) return `<span style="color:#79c0ff">${match}</span>`;
        return `<span style="color:#a5d6ff">${match}</span>`;
      }
      if (/true|false/.test(match)) return `<span style="color:#ff7b72">${match}</span>`;
      if (/null/.test(match)) return `<span style="color:#6e7681">${match}</span>`;
      return `<span style="color:#f2cc60">${match}</span>`;
    });
}

export default function ResourceDetail({ resourceType, resourceId, isAdmin, onEdit, onDeleted, onBack }) {
  const classes = useStyles();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  useEffect(() => {
    if (!resourceId) return;
    setLoading(true);
    axios.get(`/fhir/${resourceType}/${resourceId}`)
      .then(res => {
        const d = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
        setData(d);
      })
      .catch(() => setData(null))
      .finally(() => setLoading(false));
  }, [resourceType, resourceId]);

  const handleDelete = () => {
    axios.delete(`/fhir/${resourceType}/${resourceId}`)
      .then(() => { setConfirmOpen(false); onDeleted(); })
      .catch(e => alert('삭제 실패: ' + e.message));
  };

  const handleCopy = () => {
    if (data) navigator.clipboard.writeText(JSON.stringify(data, null, 2));
  };

  const pretty = data ? JSON.stringify(data, null, 2) : '';

  return (
    <div className={classes.root}>
      {loading && <LinearProgress />}
      {data && (
        <>
          <div className={classes.header}>
            <div>
              <Typography className={classes.name}>{data.name || data.id}</Typography>
              <Typography className={classes.url}>{data.url || ''}</Typography>
            </div>
            <div className={classes.actions}>
              <Button className={classes.copyBtn} variant="outlined" size="small" startIcon={<FileCopyIcon />} onClick={handleCopy}>
                Copy
              </Button>
              {isAdmin && (
                <>
                  <Button className={classes.editBtn} variant="contained" size="small" startIcon={<EditIcon />} onClick={() => onEdit(data)}>
                    Edit
                  </Button>
                  <Button className={classes.deleteBtn} variant="outlined" size="small" startIcon={<DeleteIcon />} onClick={() => setConfirmOpen(true)}>
                    Delete
                  </Button>
                </>
              )}
            </div>
          </div>

          <div className={classes.meta}>
            {[
              ['ID', data.id],
              ['Version', data.version || '-'],
              ['Status', data.status || '-'],
              ['Date', data.date || '-'],
              ['Publisher', data.publisher || '-'],
            ].map(([label, val]) => (
              <div key={label} className={classes.metaItem}>
                <div className={classes.metaLabel}>{label}</div>
                <div className={classes.metaValue}>{val}</div>
              </div>
            ))}
          </div>

          <div className={classes.jsonWrap}>
            <pre
              className={classes.json}
              dangerouslySetInnerHTML={{ __html: colorizeJson(pretty) }}
            />
          </div>
        </>
      )}

      {/* 삭제 확인 다이얼로그 */}
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>삭제 확인</DialogTitle>
        <DialogContent>
          <Typography style={{ fontSize: '0.9em' }}>
            <strong>{data && (data.name || data.id)}</strong>을(를) 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button size="small" onClick={() => setConfirmOpen(false)}>취소</Button>
          <Button size="small" variant="contained" style={{ background: '#dc2626', color: '#fff' }} onClick={handleDelete}>
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}
