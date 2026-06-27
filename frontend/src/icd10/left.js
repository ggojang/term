import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import InputAdornment from '@material-ui/core/InputAdornment';
import IconButton from '@material-ui/core/IconButton';
import SearchIcon from '@material-ui/icons/Search';
import ClearIcon from '@material-ui/icons/Clear';
import CircularProgress from '@material-ui/core/CircularProgress';
import Typography from '@material-ui/core/Typography';
import Container from '@material-ui/core/Container';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import TreeView from '@material-ui/lab/TreeView';
import TreeItem from '@material-ui/lab/TreeItem';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import RemoveIcon from '@material-ui/icons/Remove';

const BASE = '';

const useStyles = makeStyles(() => ({
  container: {
    '-ms-overflow-style': 'none',
    scrollbarWidth: 'none',
    '&::-webkit-scrollbar': { display: 'none' },
  },
  treeLabel: {
    fontSize: '0.92em',
    lineHeight: '1.6',
  },
  resultItem: {
    padding: '4px 8px',
    fontSize: '0.88em',
    lineHeight: '1.5',
    borderBottom: '1px solid #f0f0f0',
    cursor: 'pointer',
    '&:hover': { backgroundColor: '#f0f4ff' },
  },
  resultCode: {
    color: '#888',
    fontWeight: 'bold',
    marginRight: 4,
  },
  resultKorean: {
    display: 'block',
    fontSize: '0.82em',
    color: '#444',
    marginLeft: 2,
  },
  caption: {
    fontSize: '0.78em',
    color: '#999',
    padding: '2px 8px',
  },
  tab: {
    minWidth: 0,
    fontSize: '0.82em',
    padding: '4px 12px',
    minHeight: 36,
    color: '#94a3b8',
    '&.Mui-selected': { color: '#e2e8f0' },
  },
  tabs: {
    minHeight: 36,
    backgroundColor: '#1e2d40',
    borderBottom: '1px solid #0f1923',
  },
}));

// ─── KCD-9 Tree Node (LOINC pattern) ────────────────────────────────────────
function Kcd9TreeNode({ node, onSelect, classes }) {
  const [childNodes, setChildNodes] = useState(null);

  const hasChildren = node.childrenCount > 0;

  const handleToggle = (event, nodeIds) => {
    if (nodeIds.length === 0 || childNodes !== null) return;
    axios.get(`${BASE}/children/ICD10/${node.code}`)
      .then(res => {
        setChildNodes(
          res.data.map((child, i) => (
            <Kcd9TreeNode key={i} node={child} onSelect={onSelect} classes={classes} />
          ))
        );
      })
      .catch(() => setChildNodes([]));
  };

  const label = (
    <Typography className={classes.treeLabel}>
      <span style={{ color: '#888', fontWeight: 'bold', marginRight: 4 }}>{node.code}</span>
      {node.isKcdExt && <img src="/flag-kr.svg" alt="KCD-9 확장 코드" title="KCD-9 확장 코드" style={{ height: '1em', width: 'auto', marginRight: 5, verticalAlign: 'middle', borderRadius: 2, border: '1px solid #ddd' }} />}
      {node.koreanLabel || node.label}
      {node.koreanLabel && node.label !== node.koreanLabel && (
        <span style={{ color: '#aaa', marginLeft: 4, fontSize: '0.85em' }}>{node.label}</span>
      )}
      {node.childrenCount > 0 && (
        <span style={{ color: '#bbb', marginLeft: 4, fontWeight: 'normal', fontSize: '0.85em' }}>
          ({node.childrenCount})
        </span>
      )}
    </Typography>
  );

  if (!hasChildren) {
    return (
      <TreeView
        style={{ margin: 0 }}
        defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 15 }} />}
        defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 15 }} />}
      >
        <TreeItem
          endIcon={<RemoveIcon style={{ fontSize: 13 }} />}
          onLabelClick={(e) => { onSelect(node.code); e.preventDefault(); }}
          classes={{ label: classes.treeLabel }}
          nodeId={node.code}
          label={label}
        />
      </TreeView>
    );
  }

  return (
    <TreeView
      style={{ margin: 0 }}
      defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 15 }} />}
      defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 15 }} />}
      onNodeToggle={handleToggle}
    >
      <TreeItem
        onLabelClick={(e) => { onSelect(node.code); e.preventDefault(); }}
        classes={{ label: classes.treeLabel }}
        nodeId={node.code}
        label={label}
      >
        {childNodes || [<div key="stub" />]}
      </TreeItem>
    </TreeView>
  );
}

// ─── Neoplasm Morphology Node ────────────────────────────────────────────────
function MorphItem({ item, classes }) {
  return (
    <div className={classes.resultItem}>
      <span className={classes.resultCode}>{item.code}</span>
      {item.koreanLabel}
      {item.englishLabel && (
        <span style={{ color: '#aaa', marginLeft: 4, fontSize: '0.85em' }}>{item.englishLabel}</span>
      )}
    </div>
  );
}

// ─── Left Panel ──────────────────────────────────────────────────────────────
export default function Left({ setSelectedCode }) {
  const classes = useStyles();
  const [tab, setTab] = useState(0);

  // KCD-9 tab state
  const [q, setQ] = useState('');
  const [roots, setRoots] = useState([]);
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);

  // Morph tab state
  const [morphQ, setMorphQ] = useState('');
  const [morphItems, setMorphItems] = useState(null);
  const [morphAll, setMorphAll] = useState([]);
  const [morphLoading, setMorphLoading] = useState(false);

  useEffect(() => {
    axios.get(`${BASE}/children/ICD10/root`)
      .then(res => setRoots(res.data))
      .catch(() => setRoots([]));
  }, []);

  useEffect(() => {
    if (tab === 1 && morphAll.length === 0) {
      axios.get(`${BASE}/kcd9/morph/all`)
        .then(res => setMorphAll(res.data))
        .catch(() => {});
    }
  }, [tab]);

  const search = async () => {
    if (!q.trim()) return;
    setLoading(true);
    setResults(null);
    try {
      const res = await axios.get(`${BASE}/search/ICD10?q=${encodeURIComponent(q)}&page=1&size=50`);
      setResults(res.data.content || []);
    } catch {
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const clear = () => { setQ(''); setResults(null); };

  const searchMorph = async () => {
    if (!morphQ.trim()) { setMorphItems(null); return; }
    setMorphLoading(true);
    try {
      const res = await axios.get(`${BASE}/kcd9/morph/search?q=${encodeURIComponent(morphQ)}&page=1&size=100`);
      setMorphItems(res.data.content || []);
    } catch {
      setMorphItems([]);
    } finally {
      setMorphLoading(false);
    }
  };

  const clearMorph = () => { setMorphQ(''); setMorphItems(null); };

  const displayMorph = morphItems !== null ? morphItems : morphAll;

  return (
    <div>
      {/* Tabs */}
      <Tabs
        value={tab}
        onChange={(e, v) => setTab(v)}
        className={classes.tabs}
        TabIndicatorProps={{ style: { backgroundColor: '#60a5fa', height: 2 } }}
      >
        <Tab label="KCD-9" className={classes.tab} />
        <Tab label="신생물의 형태분류" className={classes.tab} />
      </Tabs>

      {/* ── KCD-9 Tab ── */}
      {tab === 0 && (
        <div>
          <div style={{ padding: '6px 8px 4px 8px' }}>
            <TextField
              fullWidth
              size="small"
              variant="outlined"
              placeholder="KCD-9 검색 (한글/영문)..."
              value={q}
              onChange={(e) => { setQ(e.target.value); if (!e.target.value) setResults(null); }}
              onKeyPress={(e) => e.key === 'Enter' && search()}
              InputProps={{
                style: { fontSize: '0.88em', height: 32 },
                endAdornment: (
                  <InputAdornment position="end">
                    {q && (
                      <IconButton size="small" onClick={clear} style={{ padding: 2 }}>
                        <ClearIcon style={{ fontSize: 14 }} />
                      </IconButton>
                    )}
                    <IconButton size="small" onClick={search} style={{ padding: 2 }}>
                      <SearchIcon style={{ fontSize: 14 }} />
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
          </div>

          {loading && <CircularProgress size={16} style={{ margin: '6px auto', display: 'block' }} />}

          {results !== null && (
            <Container className={classes.container} style={{ margin: 0, padding: 0, height: '80vh', overflow: 'scroll' }}>
              <div className={classes.caption}>{results.length} 건</div>
              {results.map((r) => (
                <div key={r.code} className={classes.resultItem} onClick={() => setSelectedCode(r.code)}>
                  {r.isKcdExt && <img src="/flag-kr.svg" alt="KCD-9 확장 코드" title="KCD-9 확장 코드" style={{ height: '1em', width: 'auto', marginRight: 5, verticalAlign: 'middle', borderRadius: 2, border: '1px solid #ddd' }} />}
                  <span className={classes.resultCode}>{r.code}</span>
                  {r.koreanLabel || r.label}
                  {r.koreanLabel && (
                    <span style={{ color: '#aaa', marginLeft: 4, fontSize: '0.85em' }}>{r.label}</span>
                  )}
                </div>
              ))}
            </Container>
          )}

          {results === null && (
            <Container className={classes.container} style={{ margin: 0, padding: '4px 0 0 4px', height: '80vh', overflow: 'scroll' }}>
              {roots.map((node, i) => (
                <Kcd9TreeNode key={i} node={node} onSelect={setSelectedCode} classes={classes} />
              ))}
            </Container>
          )}
        </div>
      )}

      {/* ── 신생물의 형태분류 Tab ── */}
      {tab === 1 && (
        <div>
          <div style={{ padding: '6px 8px 4px 8px' }}>
            <TextField
              fullWidth
              size="small"
              variant="outlined"
              placeholder="형태분류 검색 (한글/영문)..."
              value={morphQ}
              onChange={(e) => { setMorphQ(e.target.value); if (!e.target.value) setMorphItems(null); }}
              onKeyPress={(e) => e.key === 'Enter' && searchMorph()}
              InputProps={{
                style: { fontSize: '0.88em', height: 32 },
                endAdornment: (
                  <InputAdornment position="end">
                    {morphQ && (
                      <IconButton size="small" onClick={clearMorph} style={{ padding: 2 }}>
                        <ClearIcon style={{ fontSize: 14 }} />
                      </IconButton>
                    )}
                    <IconButton size="small" onClick={searchMorph} style={{ padding: 2 }}>
                      <SearchIcon style={{ fontSize: 14 }} />
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
          </div>

          {morphLoading && <CircularProgress size={16} style={{ margin: '6px auto', display: 'block' }} />}

          <Container className={classes.container} style={{ margin: 0, padding: 0, height: '80vh', overflow: 'scroll' }}>
            {morphItems !== null && <div className={classes.caption}>{morphItems.length} 건</div>}
            {displayMorph.map((item) => (
              <MorphItem key={item.code} item={item} classes={classes} />
            ))}
          </Container>
        </div>
      )}
    </div>
  );
}
