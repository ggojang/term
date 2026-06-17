import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import TextField from '@material-ui/core/TextField';
import InputAdornment from '@material-ui/core/InputAdornment';
import IconButton from '@material-ui/core/IconButton';
import SearchIcon from '@material-ui/icons/Search';
import ClearIcon from '@material-ui/icons/Clear';
import CircularProgress from '@material-ui/core/CircularProgress';
import Typography from '@material-ui/core/Typography';
import TreeView from '@material-ui/lab/TreeView';
import TreeItem from '@material-ui/lab/TreeItem';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import RemoveIcon from '@material-ui/icons/Remove';

const BASE = '';

const CAT_LABELS = { 행위: '행위', 약제: '약제', 치료재료: '치료재료' };
const CATS = ['행위', '약제', '치료재료'];

const useStyles = makeStyles(() => ({
  tab: { minWidth: 0, fontSize: '0.82em', padding: '4px 8px', minHeight: 36, flex: 1 },
  tabs: { minHeight: 36, borderBottom: '1px solid #e0e0e0' },
  treeLabel: { fontSize: '0.88em', lineHeight: '1.6' },
  searchBox: { padding: '8px 8px 4px' },
  resultItem: {
    padding: '4px 10px', fontSize: '0.86em', lineHeight: '1.5',
    borderBottom: '1px solid #f0f0f0', cursor: 'pointer',
    '&:hover': { backgroundColor: '#f0f4ff' },
  },
  codeTag: { color: '#888', fontWeight: 'bold', marginRight: 4 },
  caption: { fontSize: '0.75em', color: '#999', padding: '2px 10px' },
  priceTag: { color: '#4caf50', fontSize: '0.78em', marginLeft: 6 },
}));

// ─── 재귀 트리 노드 ────────────────────────────────────────────────────────────
function HiraTreeNode({ node, cat, depth, onSelect, classes }) {
  const [children, setChildren] = useState(null);
  const [loading, setLoading] = useState(false);

  const isLeaf = node.type === 'leaf';

  const loadChildren = () => {
    if (isLeaf || children !== null || loading) return;
    setLoading(true);
    const parts = node.code.split('|');
    let url = '';
    if (cat === '행위') {
      const p = parts.map(encodeURIComponent);
      if (p.length === 1) url = `/hira/행위/tree/${p[0]}`;
      else if (p.length === 2) url = `/hira/행위/tree/${p[0]}/${p[1]}`;
      else if (p.length === 3) url = `/hira/행위/tree/${p[0]}/${p[1]}/${p[2]}`;
      else if (p.length === 4) url = `/hira/행위/tree/${p[0]}/${p[1]}/${p[2]}/${p[3]}`;
      else url = `/hira/행위/tree/${p[0]}/${p[1]}/${p[2]}/${p[3]}/${p[4]}`;
    } else if (cat === '약제') {
      url = `/hira/약제/atc/tree/${encodeURIComponent(node.code)}`;
    } else if (cat === '치료재료') {
      if (parts.length === 1) url = `/hira/치료재료/tree/${encodeURIComponent(node.code)}`;
      else url = `/hira/치료재료/tree/${encodeURIComponent(parts[0])}/${encodeURIComponent(parts[1])}`;
    }
    axios.get(url).then(res => {
      setChildren(res.data.map((child, i) => (
        <HiraTreeNode key={i} node={child} cat={cat} depth={depth + 1} onSelect={onSelect} classes={classes} />
      )));
    }).catch(() => setChildren([])).finally(() => setLoading(false));
  };

  let labelMain = node.label;
  let labelEn = '';
  if (!isLeaf && node.label && node.label.includes('\t')) {
    const parts = node.label.split('\t');
    // parts: [code, hname, ename]
    const hname = parts[1] || '';
    const ename = parts[2] || '';
    labelMain = parts[0] + (hname ? ' ' + hname : '');
    labelEn = ename;
  }

  const label = (
    <Typography className={classes.treeLabel} component="span">
      {isLeaf && <span className={classes.codeTag}>{node.code}</span>}
      <span>{labelMain}</span>
      {labelEn && <span style={{ fontSize: '0.82em', color: '#999', marginLeft: 4 }}>{labelEn}</span>}
      {node.price != null && (
        <span className={classes.priceTag}>
          {Number(node.price).toLocaleString()}원
        </span>
      )}
      {!isLeaf && node.childCount > 0 && (
        <span style={{ color: '#bbb', fontSize: '0.82em', marginLeft: 4 }}>
          ({node.childCount})
        </span>
      )}
      {loading && <CircularProgress size={10} style={{ marginLeft: 6 }} />}
    </Typography>
  );

  const nodeId = node.code + '_' + depth;

  if (isLeaf) {
    return (
      <TreeItem
        nodeId={nodeId}
        label={label}
        endIcon={<RemoveIcon style={{ fontSize: 12 }} />}
        onLabelClick={(e) => { onSelect(node.code); e.preventDefault(); }}
      />
    );
  }

  return (
    <TreeItem
      nodeId={nodeId}
      label={label}
      onIconClick={loadChildren}
      onLabelClick={loadChildren}
    >
      {children === null ? <div /> : children}
    </TreeItem>
  );
}

// ─── 좌측 패널 ─────────────────────────────────────────────────────────────────
export default function HiraLeft({ category, setCategory, selectedCode, onSelect }) {
  const classes = useStyles();
  const [roots, setRoots] = useState([]);
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    setRoots([]);
    setSearchResults(null);
    setQuery('');
    setLoading(true);
    const treeUrl = category === '약제' ? `${BASE}/hira/약제/atc/tree` : `${BASE}/hira/${category}/tree`;
    axios.get(treeUrl)
      .then(res => setRoots(res.data))
      .catch(() => setRoots([]))
      .finally(() => setLoading(false));
  }, [category]);

  const doSearch = () => {
    if (!query.trim()) { setSearchResults(null); return; }
    setSearching(true);
    const searchUrl = category === '약제'
      ? `${BASE}/hira/약제/atc/search`
      : `${BASE}/hira/${category}/search`;
    axios.get(searchUrl, { params: { q: query, size: 100 } })
      .then(res => setSearchResults(res.data))
      .catch(() => setSearchResults({ items: [], total: 0 }))
      .finally(() => setSearching(false));
  };

  const clearSearch = () => { setQuery(''); setSearchResults(null); };

  const getItemLabel = (item) => {
    if (category === '행위') return item.koreanLabel || item.code;
    return item.name || item.code;
  };

  return (
    <div>
      {/* 카테고리 탭 */}
      <Tabs
        value={CATS.indexOf(category)}
        onChange={(_, v) => setCategory(CATS[v])}
        className={classes.tabs}
        indicatorColor="primary"
        textColor="primary"
        variant="fullWidth"
      >
        {CATS.map((c) => (
          <Tab key={c} label={CAT_LABELS[c]} className={classes.tab} />
        ))}
      </Tabs>

      {/* 검색창 */}
      <div className={classes.searchBox}>
        <TextField
          fullWidth
          size="small"
          variant="outlined"
          placeholder="코드 또는 명칭 검색..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && doSearch()}
          InputProps={{
            style: { fontSize: '0.85em' },
            endAdornment: (
              <InputAdornment position="end">
                {query && <IconButton size="small" onClick={clearSearch}><ClearIcon fontSize="small" /></IconButton>}
                <IconButton size="small" onClick={doSearch}>
                  {searching ? <CircularProgress size={16} /> : <SearchIcon fontSize="small" />}
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
      </div>

      {/* 검색결과 */}
      {searchResults !== null && (
        <div>
          <Typography className={classes.caption}>
            {searchResults.total}건 (최대 100건 표시)
          </Typography>
          {(searchResults.items || []).map((item, i) => (
            <div
              key={i}
              className={classes.resultItem}
              onClick={() => onSelect(item.code)}
              style={{ backgroundColor: item.code === selectedCode ? '#e3f2fd' : undefined }}
            >
              <span className={classes.codeTag}>{item.code}</span>
              {getItemLabel(item)}
              {category === '행위' && item.classNo && (
                <span style={{ color: '#888', fontSize: '0.78em', marginLeft: 6 }}>
                  [{item.classNo}]
                </span>
              )}
              {category === '약제' && item.atcCode && (
                <span style={{ color: '#888', fontSize: '0.78em', marginLeft: 6 }}>
                  [{item.atcCode}]
                </span>
              )}
              {item.price != null && (
                <span className={classes.priceTag}>
                  {Number(item.price).toLocaleString()}원
                </span>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 로딩 */}
      {searchResults === null && loading && (
        <div style={{ textAlign: 'center', paddingTop: 20 }}>
          <CircularProgress size={24} />
        </div>
      )}

      {/* 트리 — 검색 중에도 마운트 유지, display:none으로 숨김 */}
      <div style={{ display: searchResults !== null || loading ? 'none' : 'block' }}>
        <TreeView
          defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 16 }} />}
          defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 16 }} />}
          onNodeToggle={(_, nodeIds) => {
            // 트리 노드 확장 시 자식 로드는 각 노드에서 처리
          }}
          style={{ padding: '4px 4px' }}
        >
          {roots.map((node, i) => (
            <HiraTreeNode
              key={i}
              node={node}
              cat={category}
              depth={0}
              onSelect={onSelect}
              classes={classes}
            />
          ))}
        </TreeView>
      </div>
    </div>
  );
}
