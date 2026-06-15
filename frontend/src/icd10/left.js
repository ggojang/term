import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import InputAdornment from '@material-ui/core/InputAdornment';
import IconButton from '@material-ui/core/IconButton';
import SearchIcon from '@material-ui/icons/Search';
import ClearIcon from '@material-ui/icons/Clear';
import Typography from '@material-ui/core/Typography';
import CircularProgress from '@material-ui/core/CircularProgress';
import Box from '@material-ui/core/Box';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import TreeView from '@material-ui/lab/TreeView';
import TreeItem from '@material-ui/lab/TreeItem';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import RemoveIcon from '@material-ui/icons/Remove';

const useStyles = makeStyles(() => ({
  container: {
    '-ms-overflow-style': 'none',
    scrollbarWidth: 'none',
    '&::-webkit-scrollbar': { display: 'none' },
  },
  treeLabel: { fontSize: '0.92em', lineHeight: '1.6' },
  resultItem: {
    padding: '4px 8px',
    fontSize: '0.88em',
    lineHeight: '1.5',
    borderBottom: '1px solid #f0f0f0',
    cursor: 'pointer',
    '&:hover': { backgroundColor: '#f0f4ff' },
  },
  resultCode: { color: '#888', fontWeight: 'bold', marginRight: 4 },
  caption: { fontSize: '0.78em', color: '#999', padding: '2px 8px' },
  tab: { minWidth: 0, fontSize: '0.82em', padding: '4px 12px', minHeight: 36 },
  tabs: { minHeight: 36, borderBottom: '1px solid #e0e0e0' },
}));

const KcdFlag = () => (
  <span title="KCD-9 확장 코드" style={{ marginRight: 3 }}>🇰🇷</span>
);

// ─── KCD-9 트리 노드 ──────────────────────────────────────────────────────────
function KcdTreeNode({ node, onSelect, classes }) {
  const [children, setChildren] = useState(null);
  const hasChildren = node.childrenCount > 0;

  const label = (
    <Typography className={classes.treeLabel}>
      <span style={{ color: '#888', fontWeight: 'bold', marginRight: 4 }}>{node.code}</span>
      {node.isKcdExt && <KcdFlag />}
      {node.koreanLabel || node.label}
      {node.koreanLabel && node.label !== node.koreanLabel && (
        <span style={{ color: '#aaa', marginLeft: 4, fontSize: '0.85em' }}>{node.label}</span>
      )}
      {hasChildren && (
        <span style={{ color: '#bbb', marginLeft: 4, fontWeight: 'normal', fontSize: '0.85em' }}>
          ({node.childrenCount})
        </span>
      )}
    </Typography>
  );

  const handleToggle = (event, nodeIds) => {
    if (nodeIds.length > 0 && children === null) {
      axios.get(`/children/ICD10/${node.code}`)
        .then(res => setChildren(res.data))
        .catch(() => setChildren([]));
    }
  };

  if (!hasChildren) {
    return (
      <TreeView style={{ margin: 0 }}
        defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 15 }} />}
        defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 15 }} />}
      >
        <TreeItem
          endIcon={<RemoveIcon style={{ fontSize: 13 }} />}
          onLabelClick={e => { onSelect(node.code); e.preventDefault(); }}
          classes={{ label: classes.treeLabel }}
          nodeId={node.code}
          label={label}
        />
      </TreeView>
    );
  }

  return (
    <TreeView style={{ margin: 0 }}
      defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 15 }} />}
      defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 15 }} />}
      onNodeToggle={handleToggle}
    >
      <TreeItem
        onLabelClick={e => { onSelect(node.code); e.preventDefault(); }}
        classes={{ label: classes.treeLabel }}
        nodeId={node.code}
        label={label}
      >
        {children
          ? children.map((child, i) => (
            <KcdTreeNode key={i} node={child} onSelect={onSelect} classes={classes} />
          ))
          : [<div key="stub" />]
        }
      </TreeItem>
    </TreeView>
  );
}

// ─── 신생물 형태분류 항목 ─────────────────────────────────────────────────────
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
  const [tabValue, setTabValue] = useState(0);

  // KCD-9 탭 상태
  const [q, setQ] = useState('');
  const [roots, setRoots] = useState([]);
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);

  // 신생물 형태분류 탭 상태
  const [morphQ, setMorphQ] = useState('');
  const [morphAll, setMorphAll] = useState([]);
  const [morphResults, setMorphResults] = useState(null);
  const [morphLoading, setMorphLoading] = useState(false);

  // 루트 로드
  useEffect(() => {
    axios.get('/children/ICD10/root')
      .then(res => setRoots(res.data))
      .catch(() => setRoots([]));
  }, []);

  // 형태분류 전체 로드 (탭 전환 시 최초 1회)
  useEffect(() => {
    if (tabValue === 1 && morphAll.length === 0) {
      axios.get('/kcd9/morph/all')
        .then(res => setMorphAll(res.data))
        .catch(() => {});
    }
  }, [tabValue]);

  const search = async () => {
    if (!q.trim()) return;
    setLoading(true);
    setResults(null);
    try {
      const res = await axios.get(`/search/ICD10?q=${encodeURIComponent(q)}&page=1&size=50`);
      setResults(res.data.content || []);
    } catch {
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const searchMorph = async () => {
    if (!morphQ.trim()) { setMorphResults(null); return; }
    setMorphLoading(true);
    try {
      const res = await axios.get(`/kcd9/morph/search?q=${encodeURIComponent(morphQ)}&page=1&size=100`);
      setMorphResults(res.data.content || []);
    } catch {
      setMorphResults([]);
    } finally {
      setMorphLoading(false);
    }
  };

  const displayMorph = morphResults !== null ? morphResults : morphAll;

  return (
    <Box style={{ height: '90vh', overflowY: 'auto' }}>
      <Tabs
        value={tabValue}
        onChange={(e, v) => setTabValue(v)}
        className={classes.tabs}
        indicatorColor="primary"
        textColor="primary"
      >
        <Tab label="KCD-9" className={classes.tab} />
        <Tab label="신생물의 형태분류" className={classes.tab} />
      </Tabs>

      {/* KCD-9 탭 */}
      <div style={{ display: tabValue !== 0 ? 'none' : '' }}>
          <div style={{ padding: '6px 8px 4px 8px' }}>
            <TextField
              fullWidth
              size="small"
              variant="outlined"
              placeholder="KCD-9 검색 (한글/영문)..."
              value={q}
              onChange={e => { setQ(e.target.value); if (!e.target.value) setResults(null); }}
              onKeyPress={e => e.key === 'Enter' && search()}
              InputProps={{
                style: { fontSize: '0.88em', height: 32 },
                endAdornment: (
                  <InputAdornment position="end">
                    {q && (
                      <IconButton size="small" onClick={() => { setQ(''); setResults(null); }} style={{ padding: 2 }}>
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

          {/* 검색 결과 */}
          {results !== null && (
            <Box className={classes.container} style={{ margin: 0, padding: 0, height: '80vh', overflow: 'scroll' }}>
              <div className={classes.caption}>{results.length} 건</div>
              {results.map(item => (
                <div key={item.code} className={classes.resultItem} onClick={() => setSelectedCode(item.code)}>
                  {item.isKcdExt && <KcdFlag />}
                  <span className={classes.resultCode}>{item.code}</span>
                  {item.koreanLabel || item.label}
                  {item.koreanLabel && (
                    <span style={{ color: '#aaa', marginLeft: 4, fontSize: '0.85em' }}>{item.label}</span>
                  )}
                </div>
              ))}
            </Box>
          )}

          {/* 트리 */}
          {results === null && (
            <Box className={classes.container} style={{ margin: 0, padding: '4px 0 0 4px', height: '80vh', overflow: 'scroll' }}>
              {roots.map((node, i) => (
                <KcdTreeNode key={i} node={node} onSelect={setSelectedCode} classes={classes} />
              ))}
            </Box>
          )}
        </div>

      {/* 신생물 형태분류 탭 */}
      <div style={{ display: tabValue !== 1 ? 'none' : '' }}>
          <div style={{ padding: '6px 8px 4px 8px' }}>
            <TextField
              fullWidth
              size="small"
              variant="outlined"
              placeholder="형태분류 검색 (한글/영문)..."
              value={morphQ}
              onChange={e => { setMorphQ(e.target.value); if (!e.target.value) setMorphResults(null); }}
              onKeyPress={e => e.key === 'Enter' && searchMorph()}
              InputProps={{
                style: { fontSize: '0.88em', height: 32 },
                endAdornment: (
                  <InputAdornment position="end">
                    {morphQ && (
                      <IconButton size="small" onClick={() => { setMorphQ(''); setMorphResults(null); }} style={{ padding: 2 }}>
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
          <Box className={classes.container} style={{ margin: 0, padding: 0, height: '80vh', overflow: 'scroll' }}>
            {morphResults !== null && (
              <div className={classes.caption}>{morphResults.length} 건</div>
            )}
            {displayMorph.map(item => (
              <MorphItem key={item.code} item={item} classes={classes} />
            ))}
          </Box>
        </div>
    </Box>
  );
}
