import React, { useState } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Container from '@material-ui/core/Container';
import CircularProgress from '@material-ui/core/CircularProgress';

const EXAMPLES = [
  { label: '< 404684003', desc: 'Clinical finding의 하위' },
  { label: '<< 64572001', desc: 'Disease 및 하위' },
  { label: '<! 404684003', desc: 'Clinical finding의 직계 자식' },
  { label: '^ 900000000000497000', desc: 'CTV3 simple map refset 멤버' },
  { label: '<< 64572001 AND ^ 900000000000497000', desc: 'Disease 하위 중 refset 멤버' },
];

const useStyles = makeStyles(() => ({
  root: { padding: '8px 12px' },
  textarea: { width: '100%', fontFamily: 'monospace', fontSize: '0.85em', marginBottom: 6 },
  runBtn: {
    backgroundColor: '#2e3e4e', color: '#fff', fontSize: '0.75em', padding: '4px 14px', minHeight: 0,
    '&:hover': { backgroundColor: '#3d5166' },
  },
  exampleTitle: { fontSize: '0.72em', color: '#666', marginTop: 8, marginBottom: 2 },
  exampleItem: {
    fontSize: '0.75em', color: '#1565c0', cursor: 'pointer', fontFamily: 'monospace', lineHeight: 1.8,
    '&:hover': { textDecoration: 'underline' },
  },
  container: { '-ms-overflow-style': 'none', scrollbarWidth: 'none', '&::-webkit-scrollbar': { display: 'none' }, padding: 0 },
  row: { padding: '3px 0', cursor: 'pointer', borderBottom: '1px solid #f0f0f0', '&:hover': { backgroundColor: '#f5f5f5' } },
  conceptId: { fontSize: '0.78em', color: '#000', fontWeight: 700, fontFamily: 'monospace' },
  term: { fontSize: '0.78em', color: '#333' },
  badge: {
    display: 'inline-block', minWidth: 10, padding: '1px 5px', fontSize: '0.68em', fontWeight: 'bold',
    lineHeight: 1, textAlign: 'center', backgroundColor: '#999', borderRadius: 10, color: '#fff',
    marginRight: 4, verticalAlign: 'middle',
  },
  badgeDefined: { backgroundColor: '#5c85d6' },
  error: { fontSize: '0.78em', color: '#c62828', marginTop: 6 },
}));

export default function Ecl(props) {
  const classes = useStyles();
  const [expr, setExpr] = useState('');
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const run = () => {
    const e = expr.trim();
    if (!e) return;
    setLoading(true);
    setError('');
    setResults(null);
    axios.get('/entities/SNOMEDCT', { params: { ecl: e, size: 500 } })
      .then(res => {
        const data = res.data;
        if (Array.isArray(data)) {
          setResults(data);
        } else {
          setError((data && (data.message || data.error)) || '쿼리 오류: 표현식을 확인해주세요.');
        }
      })
      .catch(err => {
        const msg = err.response && err.response.data && (err.response.data.message || err.response.data.error);
        setError(msg || '쿼리 오류: 표현식을 확인해주세요.');
      })
      .finally(() => setLoading(false));
  };

  return (
    <div className={classes.root}>
      <TextField
        className={classes.textarea}
        multiline
        rows={4}
        variant="outlined"
        size="small"
        placeholder={'예) < 404684003\n또는 << 64572001 AND ^ 900000000000497000'}
        value={expr}
        onChange={e => setExpr(e.target.value)}
        onKeyDown={e => { if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') run(); }}
        inputProps={{ style: { fontFamily: 'monospace', fontSize: '0.85em' } }}
      />
      <Grid container alignItems="center" spacing={1}>
        <Grid item>
          <Button className={classes.runBtn} onClick={run} disabled={loading}>
            {loading ? <CircularProgress size={12} style={{ color: '#fff' }} /> : 'Run'}
          </Button>
        </Grid>
        {results !== null && (
          <Grid item>
            <Typography style={{ fontSize: '0.75em', color: '#555' }}>{results.length}건</Typography>
          </Grid>
        )}
      </Grid>

      {error && <div className={classes.error}>{error}</div>}

      {results === null && !loading && (
        <>
          <div className={classes.exampleTitle}>예시 (클릭하면 입력됨)</div>
          {EXAMPLES.map((ex, i) => (
            <div key={i} onClick={() => setExpr(ex.label)}>
              <span className={classes.exampleItem}>{ex.label}</span>
              <span style={{ fontSize: '0.72em', color: '#888', marginLeft: 6 }}>{ex.desc}</span>
            </div>
          ))}
        </>
      )}

      {results !== null && (
        <Container className={classes.container} style={{ marginTop: 6, height: '70vh', overflow: 'scroll' }}>
          {results.length === 0
            ? <div className={classes.error} style={{ color: '#888' }}>결과 없음</div>
            : results.map((item, i) => (
              <div
                key={i}
                className={classes.row}
                onClick={() => props.setFromId && props.setFromId(item.conceptId)}
              >
                <span
                  className={classes.badge}
                  style={item.definitionStatusId === '900000000000073002' ? { backgroundColor: '#5c85d6' } : {}}
                >
                  {item.definitionStatusId === '900000000000073002' ? '≡' : '○'}
                </span>
                <span className={classes.conceptId}>{item.conceptId}</span>{' '}
                <span className={classes.term}>{item.term || item.fsn}</span>
              </div>
            ))
          }
        </Container>
      )}
    </div>
  );
}
