import React, { useState } from 'react';
import axios from 'axios';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import LinearProgress from '@material-ui/core/LinearProgress';
import PlayArrowIcon from '@material-ui/icons/PlayArrow';
import AddIcon from '@material-ui/icons/Add';
import CloseIcon from '@material-ui/icons/Close';
import FileCopyIcon from '@material-ui/icons/FileCopy';

// 리소스별 지원 operation 목록
const RESOURCE_OPERATIONS = {
  CodeSystem:   ['$lookup', '$validate-code', '$subsumes'],
  ValueSet:     ['$expand', '$validate-code'],
  ConceptMap:   ['$translate'],
  NamingSystem: ['$preferred-id'],
};

const RESOURCE_TYPES = ['CodeSystem', 'ValueSet', 'ConceptMap', 'NamingSystem'];

/**
 * 현재 segments 기준으로 다음에 추가 가능한 선택지 반환
 * [{label, value, type}]  type: 'op' | 'id' | 'resource'
 */
function getNextOptions(segments) {
  if (!segments || segments.length === 0) {
    return RESOURCE_TYPES.map(r => ({ label: r, value: r, type: 'resource' }));
  }
  if (segments.some(isOp)) return []; // operation 이미 있으면 더 없음

  const rt = segments[0];
  const ops = RESOURCE_OPERATIONS[rt] || [];
  const hasId = segments.length >= 2 && !isOp(segments[1]);

  const options = [];
  if (!hasId) options.push({ label: '/{id}', value: '__id__', type: 'id' });
  ops.forEach(op => options.push({ label: op, value: op, type: 'op' }));
  return options;
}

// 세그먼트가 operation인지 판단
const isOp = (s) => typeof s === 'string' && s.startsWith('$');

// 현재 segments에서 resource type 추출 (첫 번째 세그먼트)
const getResourceType = (segments) => {
  if (!segments || segments.length === 0) return null;
  const first = segments[0];
  return RESOURCE_OPERATIONS[first] ? first : null;
};

// operation이 이미 있는지 확인
const hasOp = (segments) => segments.some(isOp);

// URL 조립
function buildUrl(base, segments, params) {
  const path = segments.filter(Boolean).join('/');
  const qs = params.filter(p => p.key).map(p =>
    `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value)}`
  ).join('&');
  return `${base}${path ? '/' + path : ''}${qs ? '?' + qs : ''}`;
}

// inline 스타일 helpers
const S = {
  root: {
    background: '#131e2e',
    borderBottom: '2px solid #0a1220',
    padding: '10px 14px 8px',
    flexShrink: 0,
  },
  row: {
    display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 3,
  },
  sep: {
    color: '#374151', fontSize: '0.85em', userSelect: 'none', padding: '0 1px',
  },
  baseInput: (focused) => ({
    background: '#0a1220',
    border: `1px solid ${focused ? '#3b82f6' : '#1e3050'}`,
    borderRadius: 5, color: '#64748b',
    fontSize: '0.77em', padding: '4px 8px',
    outline: 'none', flexShrink: 0, minWidth: 80,
    fontFamily: 'monospace',
  }),
  segInput: (focused) => ({
    background: '#0a1220',
    border: `1px solid ${focused ? '#3b82f6' : '#1e3050'}`,
    borderRadius: 5, color: '#e2e8f0',
    fontSize: '0.77em', padding: '4px 7px',
    outline: 'none', minWidth: 60,
    fontFamily: 'monospace',
  }),
  opSelect: {
    background: '#0d2b1e',
    border: '1px solid #14532d',
    borderRadius: 5, color: '#4ade80',
    fontSize: '0.77em', padding: '4px 24px 4px 7px',
    outline: 'none', cursor: 'pointer',
    appearance: 'none', WebkitAppearance: 'none',
    backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='6'%3E%3Cpath d='M0 0l5 6 5-6z' fill='%234ade80'/%3E%3C/svg%3E")`,
    backgroundRepeat: 'no-repeat', backgroundPosition: 'right 6px center',
    fontFamily: 'monospace',
  },
  paramKey: {
    background: 'transparent', border: 'none', outline: 'none',
    color: '#93c5fd', fontSize: '0.76em', padding: '0 2px',
    fontFamily: 'monospace', minWidth: 30,
  },
  paramEq: {
    color: '#374151', fontSize: '0.8em', padding: '0 1px', userSelect: 'none',
  },
  paramVal: {
    background: 'transparent', border: 'none', outline: 'none',
    color: '#d1d5db', fontSize: '0.76em', padding: '0 2px',
    fontFamily: 'monospace', minWidth: 50,
  },
  paramBox: {
    display: 'inline-flex', alignItems: 'center',
    background: '#0a1220', border: '1px solid #1e3050',
    borderRadius: 5, padding: '3px 5px', gap: 1,
  },
  preview: {
    marginTop: 5, color: '#1e3a5f', fontSize: '0.67em',
    fontFamily: 'monospace', overflow: 'hidden', textOverflow: 'ellipsis',
    whiteSpace: 'nowrap', display: 'flex', alignItems: 'center', gap: 4,
  },
};

/**
 * FhirRequestBar
 * Props:
 *   request: { base: string, segments: string[], params: [{key, value}] }
 *   onRequestChange: (req) => void
 *   onResult: ({ data, url, error } | null) => void
 */
export default function FhirRequestBar({ request, onRequestChange, onResult }) {
  const [loading, setLoading] = useState(false);
  const [baseFocused, setBaseFocused] = useState(false);
  const [segFocused, setSegFocused] = useState({});

  const { base, segments, params } = request;

  const update = (patch) => onRequestChange({ ...request, ...patch });

  // --- 세그먼트 조작 ---
  const setSeg = (i, v) => {
    const next = segments.map((s, idx) => idx === i ? v : s);
    update({ segments: next });
  };
  const handleNextSelect = (e) => {
    const val = e.target.value;
    if (!val) return;
    e.target.value = ''; // reset select
    if (val === '__id__') {
      update({ segments: [...segments, ''] });
    } else {
      update({ segments: [...segments, val] });
    }
  };
  const removeSeg = (i) => {
    update({ segments: segments.filter((_, idx) => idx !== i) });
  };

  // --- 파라미터 조작 ---
  const setParam = (i, field, v) => {
    const next = params.map((p, idx) => idx === i ? { ...p, [field]: v } : p);
    update({ params: next });
  };
  const addParam = () => update({ params: [...params, { key: '', value: '' }] });
  const delParam = (i) => update({ params: params.filter((_, idx) => idx !== i) });

  // --- 실행 ---
  const fullUrl = buildUrl(base, segments, params);

  const handleRun = () => {
    setLoading(true);
    onResult(null);
    axios.get(fullUrl)
      .then(res => {
        const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
        onResult({ data, url: fullUrl, error: null });
      })
      .catch(err => {
        const msg = err.response?.data
          ? JSON.stringify(err.response.data, null, 2)
          : err.message;
        onResult({ data: null, url: fullUrl, error: msg });
      })
      .finally(() => setLoading(false));
  };

  const handleKeyDown = (e) => { if (e.key === 'Enter') handleRun(); };

  const resourceType = getResourceType(segments);
  const opOptions = resourceType ? RESOURCE_OPERATIONS[resourceType] : [];
  const alreadyHasOp = hasOp(segments);
  const nextOptions = getNextOptions(segments);

  return (
    <div style={S.root}>
      {/* URL 조립 줄 */}
      <div style={S.row}>

        {/* ① Base URL */}
        <input
          style={{ ...S.baseInput(baseFocused), width: Math.max(base.length * 7.2, 180) }}
          value={base}
          onChange={e => update({ base: e.target.value })}
          onFocus={() => setBaseFocused(true)}
          onBlur={() => setBaseFocused(false)}
          onKeyDown={handleKeyDown}
          spellCheck={false}
          title="FHIR Base URL"
        />

        {/* ② Path 세그먼트들 */}
        {segments.map((seg, i) => (
          <React.Fragment key={i}>
            <span style={S.sep}>/</span>

            {/* operation 세그먼트 → select */}
            {isOp(seg) ? (
              <div style={{ position: 'relative', display: 'inline-flex', alignItems: 'center' }}>
                <select
                  style={S.opSelect}
                  value={seg}
                  onChange={e => setSeg(i, e.target.value)}
                  onKeyDown={handleKeyDown}
                >
                  {opOptions.map(op => (
                    <option key={op} value={op} style={{ background: '#0d2b1e' }}>{op}</option>
                  ))}
                  {/* resource type 없을 때도 자유 입력 가능하도록 */}
                  {!opOptions.includes(seg) && (
                    <option value={seg} style={{ background: '#0d2b1e' }}>{seg}</option>
                  )}
                </select>
                <IconButton
                  size="small"
                  style={{ color: '#374151', padding: 1, marginLeft: 2 }}
                  onClick={() => removeSeg(i)}
                >
                  <CloseIcon style={{ fontSize: 11 }} />
                </IconButton>
              </div>
            ) : (
              /* 일반 세그먼트 → input */
              <div style={{ display: 'inline-flex', alignItems: 'center', gap: 1 }}>
                <input
                  style={{
                    ...S.segInput(!!segFocused[i]),
                    width: Math.max((seg.length || 4) * 8 + 16, 60),
                    color: i === 0 ? '#e2e8f0' : '#fbbf24', // resource=white, id=yellow
                  }}
                  value={seg}
                  onChange={e => setSeg(i, e.target.value)}
                  onFocus={() => setSegFocused(f => ({ ...f, [i]: true }))}
                  onBlur={() => setSegFocused(f => ({ ...f, [i]: false }))}
                  onKeyDown={handleKeyDown}
                  placeholder={i === 0 ? 'ResourceType' : 'id'}
                  spellCheck={false}
                />
                {/* 마지막 비-op 세그먼트만 삭제 버튼 표시 */}
                {i === segments.length - 1 && i > 0 && (
                  <IconButton
                    size="small"
                    style={{ color: '#374151', padding: 1 }}
                    onClick={() => removeSeg(i)}
                  >
                    <CloseIcon style={{ fontSize: 11 }} />
                  </IconButton>
                )}
              </div>
            )}
          </React.Fragment>
        ))}

        {/* ③ Next-step dropdown — 현재 path 뒤에 추가 가능한 선택지 */}
        {nextOptions.length > 0 && (
          <>
            <span style={{ ...S.sep, color: '#1e3a5f' }}>/</span>
            <select
              defaultValue=""
              onChange={handleNextSelect}
              style={{
                background: '#0a1220',
                border: '1px dashed #1e3a5f',
                borderRadius: 5,
                color: '#4b5563',
                fontSize: '0.75em',
                padding: '3px 22px 3px 7px',
                outline: 'none',
                cursor: 'pointer',
                appearance: 'none', WebkitAppearance: 'none',
                backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='5'%3E%3Cpath d='M0 0l4 5 4-5z' fill='%234b5563'/%3E%3C/svg%3E")`,
                backgroundRepeat: 'no-repeat', backgroundPosition: 'right 5px center',
              }}
            >
              <option value="" disabled style={{ background: '#0a1220' }}>+ 추가</option>
              {nextOptions.map(opt => (
                <option
                  key={opt.value}
                  value={opt.value}
                  style={{
                    background: '#0a1220',
                    color: opt.type === 'op' ? '#4ade80' : opt.type === 'id' ? '#fbbf24' : '#e2e8f0',
                  }}
                >
                  {opt.label}
                </option>
              ))}
            </select>
          </>
        )}

        {/* ④ Query params */}
        {params.map((p, i) => (
          <React.Fragment key={i}>
            <span style={{ ...S.sep, color: '#f59e0b' }}>{i === 0 ? '?' : '&'}</span>
            <div style={S.paramBox}>
              <input
                style={{ ...S.paramKey, width: Math.max((p.key.length || 4) * 7.5, 30) }}
                value={p.key}
                onChange={e => setParam(i, 'key', e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="key"
                spellCheck={false}
              />
              <span style={S.paramEq}>=</span>
              <input
                style={{ ...S.paramVal, width: Math.max((p.value.length || 6) * 7.5, 50) }}
                value={p.value}
                onChange={e => setParam(i, 'value', e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="value"
                spellCheck={false}
              />
              <IconButton size="small" style={{ color: '#374151', padding: 0 }} onClick={() => delParam(i)}>
                <CloseIcon style={{ fontSize: 11 }} />
              </IconButton>
            </div>
          </React.Fragment>
        ))}

        {/* ⑤ 파라미터 추가 */}
        <Tooltip title="query parameter 추가 (?key=value)">
          <IconButton size="small" style={{ color: '#374151', padding: 2 }} onClick={addParam}>
            <span style={{ fontSize: '0.7em', fontFamily: 'monospace', lineHeight: 1, color: '#f59e0b' }}>?&amp;</span>
          </IconButton>
        </Tooltip>

        {/* ⑥ 실행 버튼 */}
        <Tooltip title="실행 (Enter)">
          <span>
            <IconButton
              size="small"
              style={{
                background: '#2563eb', color: '#fff',
                borderRadius: 6, padding: 5, marginLeft: 4,
              }}
              onClick={handleRun}
              disabled={loading}
            >
              <PlayArrowIcon style={{ fontSize: 17 }} />
            </IconButton>
          </span>
        </Tooltip>

        {/* URL 복사 */}
        <Tooltip title="URL 복사">
          <IconButton
            size="small"
            style={{ color: '#1e3a5f', padding: 2 }}
            onClick={() => navigator.clipboard.writeText(fullUrl)}
          >
            <FileCopyIcon style={{ fontSize: 13 }} />
          </IconButton>
        </Tooltip>
      </div>

      {/* URL 미리보기 */}
      <div style={S.preview}>
        <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {fullUrl}
        </span>
      </div>

      {loading && <LinearProgress style={{ marginTop: 4, borderRadius: 2 }} />}
    </div>
  );
}
