import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { makeStyles, withStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import LinearProgress from '@material-ui/core/LinearProgress';
import Button from '@material-ui/core/Button';
import Chip from '@material-ui/core/Chip';
import Popover from '@material-ui/core/Popover';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import InputAdornment from '@material-ui/core/InputAdornment';
import SearchIcon from '@material-ui/icons/Search';
import TuneIcon from '@material-ui/icons/Tune';

// ── SNOMED CT 공식 semantic tag 기본 목록 (API 미응답 시 fallback) ────────────
const DEFAULT_TAGS = [
  'administrative concept','assessment scale','attribute','basic dose form',
  'body structure','cell','cell structure','clinical drug','context-dependent category',
  'core metadata concept','disorder','disposition','dose form','environment',
  'environment / location','ethnic group','event','finding',
  'foundation metadata concept','geographic location','inactive concept',
  'intended site','life style','link assertion','linkage concept',
  'medicinal product','medicinal product form','metadata','morphologic abnormality',
  'namespace concept','navigational concept','observable entity','occupation',
  'organism','OWL metadata concept','person','physical force','physical object',
  'procedure','product','product name','qualifier value','racial group',
  'record artifact','regime/therapy','release characteristic','religion/philosophy',
  'role','situation','SNOMED RT+CTV3','social concept','special concept',
  'specimen','staging scale','state of matter','substance','supplier',
  'transformation','tumor staging','unit of presentation','virtual clinical drug',
];

// ── Preset 정의 ───────────────────────────────────────────────────────────────
const PRESET_NAMES = {
  diagnosis: new Set(['finding','disorder','event','situation']),
  procedure: new Set(['procedure','regime/therapy','situation']),
};

function makeState(allTags, active) {
  return allTags.map(name => ({ name, state: active.has(name) }));
}

// ── 스타일 ───────────────────────────────────────────────────────────────────
const StyledTableCell = withStyles(() => ({
  head: {
    backgroundColor: '#1e2d40',
    color: '#94a3b8',
    fontWeight: 700,
    fontSize: '0.75em',
    padding: '8px 12px',
  },
  body: { fontSize: 12, padding: '6px 12px' },
}))(TableCell);

const useStyles = makeStyles(() => ({
  root: { display: 'flex', flexDirection: 'column', height: 'calc(100vh - 48px)' },

  // ── 상단 필터 바 ──
  filterBar: {
    background: '#162030',
    borderBottom: '1px solid #0f1923',
    padding: '8px 16px',
    display: 'flex',
    flexDirection: 'column',
    gap: 6,
    flexShrink: 0,
  },
  presetRow: { display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' },
  presetLabel: { fontSize: '0.72em', color: '#60a5fa', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em', marginRight: 4 },
  presetBtn: {
    fontSize: '0.75em', padding: '3px 12px', minWidth: 0, textTransform: 'none',
    borderRadius: 14, border: '1px solid #334155', color: '#94a3b8',
    '&:hover': { borderColor: '#60a5fa', color: '#e2e8f0', background: '#1e2d40' },
  },
  presetBtnActive: {
    fontSize: '0.75em', padding: '3px 12px', minWidth: 0, textTransform: 'none',
    borderRadius: 14, color: '#1e2d40', background: '#60a5fa', border: '1px solid #60a5fa',
    fontWeight: 700,
    '&:hover': { background: '#93c5fd' },
  },
  filterBtn: {
    fontSize: '0.75em', padding: '3px 10px', minWidth: 0, textTransform: 'none',
    borderRadius: 14, border: '1px solid #334155', color: '#94a3b8', marginLeft: 'auto',
    '&:hover': { borderColor: '#60a5fa', color: '#e2e8f0' },
  },
  searchRow: { display: 'flex', alignItems: 'center', gap: 8 },
  searchInput: {
    flex: 1,
    '& .MuiOutlinedInput-root': {
      background: '#fff', borderRadius: 8,
      '& fieldset': { borderColor: '#cbd5e1' },
      '&:hover fieldset': { borderColor: '#2f6fed' },
      '&.Mui-focused fieldset': { borderColor: '#2f6fed' },
    },
    '& input': { color: '#111827 !important', background: 'transparent !important', fontSize: '0.88em', padding: '8px 12px' },
    '& .MuiInputAdornment-root svg': { color: '#64748b' },
  },
  chipRow: { display: 'flex', flexWrap: 'wrap', gap: 4, minHeight: 0 },
  activeChip: {
    height: 20, fontSize: '0.68em',
    background: '#1e3a5f', color: '#93c5fd',
    border: '1px solid #2563eb44',
    '& .MuiChip-deleteIcon': { color: '#60a5fa', width: 14, height: 14 },
  },

  // ── Popover 체크박스 ──
  popoverPaper: { padding: '12px 16px', maxWidth: 520, maxHeight: '70vh', overflow: 'auto', background: '#1e2d40' },
  popoverTitle: { fontSize: '0.75em', color: '#60a5fa', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8 },
  checkGrid: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0 8px' },
  checkLabel: { fontSize: '0.72em', color: '#cbd5e1' },
  checkBox: { color: '#334155', '&.Mui-checked': { color: '#60a5fa' }, padding: 3 },

  // ── 결과 영역 ──
  tableWrap: { flex: 1, overflow: 'auto', padding: '0 16px 16px' },
  emptyMsg: { padding: '32px 0', color: '#94a3b8', fontSize: '0.82em', textAlign: 'center' },
}));

const BASE = '';

export default function Main() {
  const classes = useStyles();

  // semantic tag 목록 (API 로드)
  const [allTags, setAllTags] = useState([]);

  useEffect(() => {
    axios.get('/tc/SNOMEDCT/semanticTags').then(res => {
      const tags = (res.data || []).map(t => t.name).filter(Boolean);
      const list = tags.length > 0 ? tags : DEFAULT_TAGS;
      setAllTags(list);
      setCheckState(makeState(list, PRESET_NAMES.diagnosis));
    }).catch(() => {
      setAllTags(DEFAULT_TAGS);
      setCheckState(makeState(DEFAULT_TAGS, PRESET_NAMES.diagnosis));
    });
  }, []);

  // 필터 상태
  const [checkState, setCheckState] = useState([]);
  const [activePreset, setActivePreset] = useState('diagnosis');
  const [anchorEl, setAnchorEl] = useState(null);

  // 검색 상태
  const [q, setQ] = useState('');
  const [inputVal, setInputVal] = useState('');
  const [val, setVal] = useState([]);
  const [loading, setLoading] = useState(false);

  const activeTags = checkState.filter(t => t.state);

  // preset 선택
  const applyPreset = (key) => {
    setActivePreset(key);
    const active = key === 'all'  ? new Set(allTags)
                 : key === 'none' ? new Set()
                 : PRESET_NAMES[key] || new Set();
    setCheckState(makeState(allTags, active));
  };

  // 개별 체크 변경
  const handleCheck = (name) => {
    setActivePreset(null);
    setCheckState(prev => prev.map(t => t.name === name ? { ...t, state: !t.state } : t));
  };

  // 태그 칩 ×
  const removeTag = (name) => {
    setActivePreset(null);
    setCheckState(prev => prev.map(t => t.name === name ? { ...t, state: false } : t));
  };

  // 검색 실행
  useEffect(() => {
    if (!q || q.length < 2) { setVal([]); return; }
    setLoading(true);
    setVal([]);
    const semanticTags = activeTags.map(t => t.name);
    axios.post(`${BASE}/map/SNOMEDCT/search`, {
      q: q.replace(/(^\s*)|(\s*$)/gi, '').replace(/\s+/g, ' ').replace(/\s*-\s*/gi, ' '),
      semanticTags,
    }).then(res => {
      const hits = (res.data && res.data.hits) ? res.data.hits : [];
      const rows = hits.map(h => ({ conceptId: h.conceptId, fsn: h.fsn, term: h.term }));
      // postexpr 병렬 조회
      return Promise.all(rows.map(r =>
        axios.get(`${BASE}/postexpr/SNOMEDCT/${r.conceptId}`)
          .then(r2 => r2.data)
          .catch(() => null)
      )).then(exprs => rows.map((r, i) => ({ ...r, expr: exprs[i] })));
    }).then(rows => {
      setVal(rows.filter(r => r.fsn !== r.term));
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [q, checkState]);

  const handleKeyUp = (e) => {
    if (e.key === 'Enter') setQ(e.target.value);
  };

  return (
    <div className={classes.root}>
      {/* ── 필터 바 ── */}
      <div className={classes.filterBar}>
        {/* Preset + Filter 버튼 */}
        <div className={classes.presetRow}>
          <span className={classes.presetLabel}>Semantic Tag</span>
          {['diagnosis','procedure','all','none'].map(key => (
            <Button key={key}
              className={activePreset === key ? classes.presetBtnActive : classes.presetBtn}
              onClick={() => applyPreset(key)}>
              {key.charAt(0).toUpperCase() + key.slice(1)}
            </Button>
          ))}
          <Button className={classes.filterBtn}
            startIcon={<TuneIcon style={{ fontSize: 14 }} />}
            onClick={e => setAnchorEl(e.currentTarget)}>
            Filter ({activeTags.length})
          </Button>
        </div>

        {/* 검색창 */}
        <div className={classes.searchRow}>
          <TextField
            className={classes.searchInput}
            variant="outlined"
            placeholder="검색어 입력 후 Enter (2자 이상)"
            value={inputVal}
            onChange={e => setInputVal(e.target.value)}
            onKeyUp={handleKeyUp}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            }}
          />
        </div>

        {/* 활성 태그 칩 */}
        {activeTags.length > 0 && activeTags.length < 20 && (
          <div className={classes.chipRow}>
            {activeTags.map(t => (
              <Chip key={t.name} label={t.name} size="small"
                className={classes.activeChip}
                onDelete={() => removeTag(t.name)} />
            ))}
          </div>
        )}
        {activeTags.length >= 20 && (
          <div style={{ fontSize: '0.72em', color: '#64748b' }}>
            {activeTags.length}개 태그 선택됨
          </div>
        )}
      </div>

      {loading && <LinearProgress style={{ flexShrink: 0 }} />}

      {/* ── 결과 테이블 ── */}
      <div className={classes.tableWrap}>
        {val.length > 0 ? (
          <TableContainer>
            <Table stickyHeader size="small">
              <colgroup>
                <col style={{ width: '3%' }} />
                <col style={{ width: '25%' }} />
                <col style={{ width: '72%' }} />
              </colgroup>
              <TableHead>
                <TableRow>
                  <StyledTableCell>No.</StyledTableCell>
                  <StyledTableCell>Mapped term</StyledTableCell>
                  <StyledTableCell>Fully Specified Name &amp; Defining Relationship</StyledTableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {val.map((v, i) => (
                  <TableRow key={i} hover>
                    <StyledTableCell>{i + 1}</StyledTableCell>
                    <StyledTableCell>{v.term}</StyledTableCell>
                    <StyledTableCell>
                      <span style={{ color: '#475569' }}>{v.conceptId}</span>
                      {' | '}
                      <span>{v.fsn}</span>
                      {v.expr && <><br /><span style={{ color: '#64748b', fontSize: '0.9em' }}>{v.expr}</span></>}
                    </StyledTableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        ) : !loading && q ? (
          <div className={classes.emptyMsg}>검색 결과가 없습니다.</div>
        ) : !loading && (
          <div className={classes.emptyMsg}>
            Semantic Tag를 선택하고 검색어를 입력한 뒤 Enter를 누르세요.
          </div>
        )}
      </div>

      {/* ── Semantic Tag Popover ── */}
      <Popover
        open={Boolean(anchorEl)}
        anchorEl={anchorEl}
        onClose={() => setAnchorEl(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        PaperProps={{ className: classes.popoverPaper }}>
        <div className={classes.popoverTitle}>
          Semantic Tags — {activeTags.length} / {allTags.length} selected
        </div>
        <div className={classes.checkGrid}>
          {checkState.map(t => (
            <FormControlLabel key={t.name}
              control={
                <Checkbox size="small" checked={t.state}
                  onChange={() => handleCheck(t.name)}
                  className={classes.checkBox} />
              }
              label={<span className={classes.checkLabel}>{t.name}</span>}
              style={{ margin: 0 }}
            />
          ))}
        </div>
      </Popover>
    </div>
  );
}
