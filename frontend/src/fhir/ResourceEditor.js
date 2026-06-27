import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import SaveIcon from '@material-ui/icons/Save';
import CloseIcon from '@material-ui/icons/Close';

// 리소스 타입별 기본 템플릿
const TEMPLATES = {
  CodeSystem: {
    resourceType: 'CodeSystem',
    id: '',
    url: 'http://example.org/fhir/CodeSystem/',
    name: '',
    title: '',
    status: 'draft',
    experimental: true,
    date: new Date().toISOString().slice(0, 10),
    publisher: '',
    description: '',
    content: 'complete',
    concept: [
      { code: 'example', display: 'Example', definition: 'An example code.' }
    ]
  },
  ValueSet: {
    resourceType: 'ValueSet',
    id: '',
    url: 'http://example.org/fhir/ValueSet/',
    name: '',
    title: '',
    status: 'draft',
    date: new Date().toISOString().slice(0, 10),
    publisher: '',
    description: '',
    compose: {
      include: [{ system: 'http://example.org/fhir/CodeSystem/' }]
    }
  },
  ConceptMap: {
    resourceType: 'ConceptMap',
    id: '',
    url: 'http://example.org/fhir/ConceptMap/',
    name: '',
    title: '',
    status: 'draft',
    date: new Date().toISOString().slice(0, 10),
    publisher: '',
    description: '',
    group: [{
      source: '',
      target: '',
      element: [{ code: '', target: [{ code: '', equivalence: 'equivalent' }] }]
    }]
  },
  NamingSystem: {
    resourceType: 'NamingSystem',
    id: '',
    name: '',
    status: 'draft',
    kind: 'codesystem',
    date: new Date().toISOString().slice(0, 10),
    publisher: '',
    description: '',
    uniqueId: [{ type: 'uri', value: '' }]
  },
};

const useStyles = makeStyles(() => ({
  root: { display: 'flex', flexDirection: 'column', height: '100%', background: '#0d1117' },
  header: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '14px 20px', background: '#161b22', borderBottom: '1px solid #21262d',
  },
  title: { fontSize: '0.95em', fontWeight: 600, color: '#e6edf3' },
  actions: { display: 'flex', gap: 8 },
  saveBtn: {
    background: '#238636', color: '#fff', fontSize: '0.78em', padding: '5px 14px',
    '&:hover': { background: '#2ea043' },
  },
  cancelBtn: {
    background: 'transparent', color: '#8b949e', border: '1px solid #30363d', fontSize: '0.78em', padding: '5px 12px',
    '&:hover': { background: '#21262d', color: '#e6edf3' },
  },
  hint: {
    padding: '6px 20px', background: '#161b22', borderBottom: '1px solid #21262d',
    fontSize: '0.72em', color: '#8b949e',
  },
  editorWrap: { flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' },
  textarea: {
    flex: 1, width: '100%', border: 'none', outline: 'none', resize: 'none',
    background: '#0d1117', color: '#e6edf3', padding: '20px 24px',
    fontFamily: "'JetBrains Mono', 'Fira Code', 'Consolas', monospace",
    fontSize: '0.82em', lineHeight: 1.7, boxSizing: 'border-box',
  },
  error: {
    padding: '8px 20px', background: '#3d1515', borderTop: '1px solid #6e1a1a',
    fontSize: '0.75em', color: '#f87171',
  },
}));

export default function ResourceEditor({ resourceType, initialData, onSaved, onCancel }) {
  const classes = useStyles();
  const isNew = !initialData;

  const defaultJson = JSON.stringify(
    initialData || TEMPLATES[resourceType] || { resourceType },
    null, 2
  );

  const [json, setJson] = useState(defaultJson);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setJson(initialData ? JSON.stringify(initialData, null, 2) : JSON.stringify(TEMPLATES[resourceType] || { resourceType }, null, 2));
    setError('');
  }, [resourceType, initialData]);

  const handleSave = () => {
    let parsed;
    try {
      parsed = JSON.parse(json);
    } catch (e) {
      setError('JSON 문법 오류: ' + e.message);
      return;
    }
    setError('');
    setSaving(true);

    const id = parsed.id;
    const req = isNew || !id
      ? axios.post(`/fhir/${resourceType}`, json, { headers: { 'Content-Type': 'application/fhir+json' } })
      : axios.put(`/fhir/${resourceType}/${id}`, json, { headers: { 'Content-Type': 'application/fhir+json' } });

    req
      .then(() => onSaved())
      .catch(e => setError('저장 실패: ' + (e.response?.data || e.message)))
      .finally(() => setSaving(false));
  };

  const handleFormat = () => {
    try {
      setJson(JSON.stringify(JSON.parse(json), null, 2));
      setError('');
    } catch (e) {
      setError('JSON 문법 오류: ' + e.message);
    }
  };

  return (
    <div className={classes.root}>
      <div className={classes.header}>
        <Typography className={classes.title}>
          {isNew ? `New ${resourceType}` : `Edit: ${initialData?.name || initialData?.id}`}
        </Typography>
        <div className={classes.actions}>
          <Button className={classes.cancelBtn} variant="outlined" size="small" startIcon={<CloseIcon />} onClick={onCancel}>
            취소
          </Button>
          <Button className={classes.saveBtn} variant="contained" size="small" startIcon={<SaveIcon />}
            onClick={handleSave} disabled={saving}>
            {saving ? '저장 중...' : '저장'}
          </Button>
        </div>
      </div>
      <div className={classes.hint}>
        Ctrl+Shift+F: JSON 포맷 정렬 &nbsp;|&nbsp; {resourceType} JSON을 직접 입력하세요
      </div>
      <div className={classes.editorWrap}>
        <textarea
          className={classes.textarea}
          value={json}
          onChange={e => setJson(e.target.value)}
          onKeyDown={e => {
            if (e.ctrlKey && e.shiftKey && e.key === 'F') { e.preventDefault(); handleFormat(); }
            // Tab 입력 지원
            if (e.key === 'Tab') {
              e.preventDefault();
              const s = e.target.selectionStart;
              const newVal = json.substring(0, s) + '  ' + json.substring(e.target.selectionEnd);
              setJson(newVal);
              setTimeout(() => e.target.setSelectionRange(s + 2, s + 2), 0);
            }
          }}
          spellCheck={false}
        />
      </div>
      {error && <div className={classes.error}>{error}</div>}
    </div>
  );
}
