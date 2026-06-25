import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import Grid from "@material-ui/core/Grid";
import Divider from "@material-ui/core/Divider";
import Typography from '@material-ui/core/Typography';
import FormControl from '@material-ui/core/FormControl';
import Select from "@material-ui/core/Select";
import InputLabel from "@material-ui/core/InputLabel";
import MenuItem from '@material-ui/core/MenuItem';
import Container from '@material-ui/core/Container';
import Drawer from '@material-ui/core/Drawer';
import FormLabel from '@material-ui/core/FormLabel';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import Button from '@material-ui/core/Button';

const useStyles = makeStyles((theme) => ({
  container: {
    '-ms-overflow-style': 'none',
    scrollbarWidth: 'none',
    '&::-webkit-scrollbar': { display: 'none' },
  },
  select: {
    minWidth: '9ch',
    height: '28px',
    fontSize: '12px',
    verticalAlign: 'middle',
  },
  menuitem: { fontSize: '12px' },
  inputlabel: { minWidth: '10ch', fontSize: '11px' },
  list: { width: '17vw' },
  formControl: { margin: theme.spacing(1), alignItems: 'center' },
  label: { fontSize: '11px' },
}));

const HISTORY_KEY = 'stom-search-history';
const MAX_HISTORY = 10;

function getSemanticTag(fsn) {
  if (!fsn) return null;
  const m = fsn.match(/\(([^)]+)\)$/);
  return m ? m[1] : null;
}

function getSemanticTagClass(tag) {
  if (!tag) return 'badge-st-default';
  const t = tag.toLowerCase();
  if (t.includes('disorder')) return 'badge-st-disorder';
  if (t.includes('procedure')) return 'badge-st-procedure';
  if (t.includes('finding')) return 'badge-st-finding';
  if (t.includes('observable')) return 'badge-st-observable';
  if (t.includes('substance')) return 'badge-st-substance';
  if (t.includes('organism')) return 'badge-st-organism';
  if (t.includes('body')) return 'badge-st-body';
  if (t.includes('qualifier')) return 'badge-st-qualifier';
  if (t.includes('situation')) return 'badge-st-situation';
  if (t.includes('event')) return 'badge-st-event';
  return 'badge-st-default';
}

export default function Search(props) {
  const classes = useStyles();

  const [q, setQ] = useState('');
  const [inputVal, setInputVal] = useState('');
  const [matchType, setMatchType] = useState('PARTIAL');
  const [state, setState] = useState('ACTIVE');
  const [page, setPage] = useState(1);
  const [size] = useState(100);
  const [stateTag, setStateTag] = useState({ left: false });
  const [semanticTags, setSemanticTags] = useState([]);
  const [listCheckBox, setListCheckBox] = useState({});
  const [result, setResult] = useState([]);
  const [result2, setResult2] = useState([]);
  const [searchResult, setSearchResult] = useState([]);
  const [searchResult2, setSearchResult2] = useState([]);
  const [showHistory, setShowHistory] = useState(false);
  const [history, setHistory] = useState(() => {
    try { return JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]'); } catch { return []; }
  });
  const inputRef = useRef(null);

  const saveHistory = (term) => {
    const next = [term, ...history.filter(h => h !== term)].slice(0, MAX_HISTORY);
    setHistory(next);
    localStorage.setItem(HISTORY_KEY, JSON.stringify(next));
  };

  const removeHistory = (term, e) => {
    e.stopPropagation();
    const next = history.filter(h => h !== term);
    setHistory(next);
    localStorage.setItem(HISTORY_KEY, JSON.stringify(next));
  };

  const doSearch = (term) => {
    if (term.length > 1) {
      saveHistory(term);
      setQ(term);
      setInputVal(term);
    }
    setShowHistory(false);
  };

  useEffect(() => {
    if (q.length > 1) {
      setPage(1);
      setListCheckBox({});
      const vq = props.version ? `&version=${props.version}` : '';
      axios
        .get(`/search/SNOMEDCT?match=${matchType}&state=${state}&q=${q}&page=${page}&size=${size}${vq}`)
        .then(response => setSearchResult(response));
    }
  }, [q, state, matchType]);

  useEffect(() => {
    if (q.length > 1 && page > 1) {
      let tmp = '';
      for (var l in listCheckBox) {
        if (listCheckBox[l]) tmp += '&semanticfilter=' + l;
      }
      tmp = tmp.replace(/ /gi, '%20').replace(/\+/gi, '%2B');
      const vq2 = props.version ? `&version=${props.version}` : '';
      axios
        .get(`/search/SNOMEDCT?match=${matchType}&state=${state}&q=${q}${tmp}&page=${page}&size=${size}${vq2}`)
        .then(response => setSearchResult(response));
    }
  }, [page]);

  useEffect(() => {
    if (searchResult.length !== 0) {
      if (searchResult.data.page.totalElements > 0) {
        if (page === 1) {
          setResult(searchResult);
          setResult2(searchResult.data.page.content);
          setSemanticTags(searchResult.data.semanticTags);
          searchResult.data.semanticTags.forEach(tags => { listCheckBox[tags.name] = false; });
        } else {
          setResult2([...result2, ...searchResult.data.page.content]);
        }
      } else {
        setResult([]); setResult2([]); setSemanticTags([]);
      }
    }
  }, [searchResult]);

  useEffect(() => {
    if (q.length > 1 && listCheckBox.length !== 0) {
      setPage(1);
      let tmp = '';
      for (var l in listCheckBox) {
        if (listCheckBox[l]) tmp += '&semanticfilter=' + l;
      }
      tmp = tmp.replace(/ /gi, '%20').replace(/\+/gi, '%2B');
      const vq3 = props.version ? `&version=${props.version}` : '';
      axios
        .get(`/search/SNOMEDCT?match=${matchType}&state=${state}&q=${q}${tmp}&page=1&size=${size}${vq3}`)
        .then(response => setSearchResult2(response));
    }
  }, [listCheckBox]);

  useEffect(() => {
    if (searchResult2.length !== 0) {
      if (searchResult2.data.page.totalElements > 0) {
        setResult2([]);
        setResult(searchResult2);
        setResult2([...searchResult2.data.page.content]);
        setSearchResult2([]);
      }
    }
  }, [searchResult2]);

  const handleMatchTypeChange = (e) => setMatchType(e.target.value);
  const handleStatusChange = (e) => setState(e.target.value);
  const handleCheckBoxChange = (e) => setListCheckBox({ ...listCheckBox, [e.target.name]: e.target.checked });
  const nextPage = () => setPage(page + 1);
  const toggleDrawer = (anchor, open) => (e) => {
    if (e.type === 'keydown' && (e.key === 'Tab' || e.key === 'Shift')) return;
    setStateTag({ ...stateTag, [anchor]: open });
  };

  const filterDrawer = (anchor) => (
    <div className={classes.list} role="presentation">
      <FormControl component="fieldset" className={classes.formControl}>
        <FormLabel component="legend" style={{ fontSize: 13, fontWeight: 700 }}>Semantic Tag</FormLabel>
        <div style={{ height: 8 }} />
        <FormGroup>
          {semanticTags.map((tags, i) => (
            <FormControlLabel
              key={i}
              style={{ height: 26 }}
              control={<Checkbox size="small" checked={!!listCheckBox[tags.name]} onClick={handleCheckBoxChange} name={tags.name} />}
              label={
                <Grid container wrap="nowrap" spacing={1} alignItems="center">
                  <Grid item><Typography color="secondary" style={{ fontSize: 11 }}>{tags.count}</Typography></Grid>
                  <Grid item><Typography style={{ fontSize: 12 }}>{tags.name}</Typography></Grid>
                </Grid>
              }
            />
          ))}
        </FormGroup>
      </FormControl>
    </div>
  );

  return (
    <Grid container>
      <Grid item md={12}>

        {/* ── 필터 컨트롤 ── */}
        <div style={{ padding: '6px 8px 4px', display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
          <FormControl size="small">
            <InputLabel style={{ fontSize: 11 }} id="matchTypeLabel">Match</InputLabel>
            <Select labelId="matchTypeLabel" className={classes.select} value={matchType} onChange={handleMatchTypeChange}>
              <MenuItem className={classes.menuitem} value="FULLTEXT">exact</MenuItem>
              <MenuItem className={classes.menuitem} value="PARTIAL">partial</MenuItem>
              <MenuItem className={classes.menuitem} value="REGEX">regex</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small">
            <InputLabel style={{ fontSize: 11 }} id="statusTypeLabel">Status</InputLabel>
            <Select labelId="statusTypeLabel" className={classes.select} value={state} onChange={handleStatusChange}>
              <MenuItem className={classes.menuitem} value="ACTIVE">active</MenuItem>
              <MenuItem className={classes.menuitem} value="INACTIVE">inactive</MenuItem>
              <MenuItem className={classes.menuitem} value="BOTH">both</MenuItem>
            </Select>
          </FormControl>
        </div>

        {/* ── 검색박스 ── */}
        <div className="search-box-wrap" style={{ position: 'relative' }}>
          <input
            ref={inputRef}
            className="search-box-modern"
            type="search"
            placeholder="At least 2 characters"
            value={inputVal}
            onChange={e => setInputVal(e.target.value)}
            onKeyDown={e => {
              if (e.key === 'Enter' && e.target.value.length > 1) doSearch(e.target.value);
            }}
            onFocus={() => setShowHistory(true)}
            onBlur={() => setTimeout(() => setShowHistory(false), 150)}
          />
          {/* 검색 히스토리 드롭다운 */}
          {showHistory && history.length > 0 && (
            <div className="search-history">
              <div className="search-history-label">Recent searches</div>
              {history.map((h, i) => (
                <div key={i} className="search-history-item" onMouseDown={() => doSearch(h)}>
                  <span style={{ fontSize: 13 }}>🕐</span>
                  <span style={{ flex: 1 }}>{h}</span>
                  <span
                    style={{ fontSize: 11, color: '#98a2b3', padding: '0 4px', cursor: 'pointer' }}
                    onMouseDown={(e) => removeHistory(h, e)}
                  >✕</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ── 검색 결과 요약 ── */}
        <div style={{ padding: '4px 8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography style={{ fontSize: 11, color: '#98a2b3' }}>
            {result.length !== 0 && result.data ? `${result.data.page.totalElements.toLocaleString()} results` : 'At least 2 characters'}
          </Typography>
          {result.length !== 0 && semanticTags.length > 0 && (
            <div>
              {['left'].map(anchor => (
                <div key={anchor}>
                  <Button
                    size="small"
                    variant="outlined"
                    style={{ fontSize: 11, padding: '2px 8px', borderRadius: 6, textTransform: 'none' }}
                    onClick={toggleDrawer(anchor, true)}
                  >
                    Filter ({semanticTags.length})
                  </Button>
                  <Drawer anchor={anchor} open={stateTag[anchor]} onClose={toggleDrawer(anchor, false)}>
                    {filterDrawer(anchor)}
                  </Drawer>
                </div>
              ))}
            </div>
          )}
        </div>
        <Divider style={{ borderBottom: '1px solid #e4e7ec' }} />

        {/* ── 검색결과 목록 ── */}
        {q && result2 && result.length !== 0 && (
          <Container
            className={classes.container}
            style={{ padding: '4px 4px', height: '82vh', overflow: 'scroll', maxWidth: 720 }}
          >
            {result2.map((re, index) => {
              const isActive = re.conceptActive && re.descriptionActive;
              const isFullyDefined = re.definitionStatusId === '900000000000073002';

              // 노란 원형 배지 스타일 (공통)
              const yellowBadge = {
                display: 'inline-block',
                padding: '1px 6px',
                fontSize: 11,
                fontWeight: 'bold',
                lineHeight: 1,
                borderRadius: 10,
                backgroundImage: 'linear-gradient(to bottom,#f7edb5 0,#f5e79e 100%)',
                color: '#8a6d3b',
                marginRight: 4,
                verticalAlign: 'middle',
              };

              return (
                <div key={index}>
                  <div
                    className={`result-item ${!isActive ? 'inactive' : ''}`}
                    onClick={() => {
                      props.setFromId(re.conceptId);
                      if (isActive) props.setMrcmFromSearch(re.conceptId);
                    }}
                  >
                    {/* 1줄: 정의상태 배지 + term(PT/SYN) + semantic tag */}
                    <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 6 }}>
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <div className="result-term">
                          <span style={yellowBadge}>
                            {isFullyDefined ? '≡' : '  '}
                          </span>
                          {re.term}
                          {!isActive && <span style={{ fontSize: 10, color: '#ef4444', marginLeft: 6, fontWeight: 600 }}>Inactive</span>}
                        </div>
                        <div style={{ fontSize: 11, color: '#6b7280', marginTop: 2, paddingLeft: 2 }}>
                          {re.fsn}
                        </div>
                      </div>
                    </div>
                  </div>
                  <Divider style={{ borderBottom: '1px solid #f0f0f0' }} />
                </div>
              );
            })}

            <div style={{ padding: '12px 0', textAlign: 'center' }}>
              {page < result.data.page.totalPages && (
                <Button variant="outlined" size="small" onClick={nextPage}
                  style={{ fontSize: 12, borderRadius: 6, textTransform: 'none' }}>
                  Load more
                </Button>
              )}
            </div>
          </Container>
        )}
      </Grid>
    </Grid>
  );
}
