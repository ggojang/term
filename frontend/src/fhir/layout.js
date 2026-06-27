import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Chip from '@material-ui/core/Chip';
import NavigateNextIcon from '@material-ui/icons/NavigateNext';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import ExitToAppIcon from '@material-ui/icons/ExitToApp';

import Tab from '@material-ui/core/Tab';
import Tabs from '@material-ui/core/Tabs';
import FhirTree from './FhirTree';
import ResourceList from './ResourceList';
import ResourceDetail from './ResourceDetail';
import ResourceEditor from './ResourceEditor';
import LoginDialog from './LoginDialog';
import ActivityPanel from './ActivityPanel';
import FhirRequestBar from './FhirRequestBar';

const useStyles = makeStyles(() => ({
  root: { display: 'flex', flexDirection: 'column', height: 'calc(100vh - 48px)', background: '#f3f4f6' },
  subheader: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '0 20px', height: 40,
    background: '#1e2d40', borderBottom: '1px solid #162030', flexShrink: 0,
  },
  breadcrumb: { display: 'flex', alignItems: 'center', gap: 4 },
  crumb: { fontSize: '0.78em', color: '#94a3b8', cursor: 'pointer', '&:hover': { color: '#e2e8f0' } },
  crumbActive: { fontSize: '0.78em', color: '#e2e8f0', fontWeight: 500 },
  crumbSep: { color: '#475569', fontSize: '0.75em' },
  adminChip: { background: '#7c3aed22', color: '#a78bfa', border: '1px solid #7c3aed44', height: 24, fontSize: '0.72em' },
  loginBtn: {
    color: '#94a3b8', fontSize: '0.75em', padding: '2px 10px', border: '1px solid #334155', minHeight: 0,
    '&:hover': { color: '#e2e8f0', borderColor: '#475569', background: '#1e293b' },
  },
  body: { display: 'flex', flex: 1, overflow: 'hidden' },
  content: { flex: 1, display: 'flex', overflow: 'hidden', background: '#fff' },
  listPane: { width: 420, minWidth: 360, borderRight: '1px solid #e5e7eb', overflow: 'hidden', display: 'flex', flexDirection: 'column' },
  detailPane: { flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' },
  placeholder: { flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 12, color: '#d1d5db' },
  placeholderIcon: { fontSize: 48 },
  placeholderText: { fontSize: '0.85em' },
}));

function colorizeJson(json) {
  return json.replace(
    /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+-]?\d+)?)/g,
    match => {
      if (/^"/.test(match)) {
        if (/:$/.test(match)) return `<span style="color:#79c0ff">${match}</span>`;
        return `<span style="color:#a5d6ff">${match}</span>`;
      }
      if (/true|false/.test(match)) return `<span style="color:#ff7b72">${match}</span>`;
      if (/null/.test(match)) return `<span style="color:#6e7681">${match}</span>`;
      return `<span style="color:#f2cc60">${match}</span>`;
    }
  );
}

function FhirRawResult({ result, onClear }) {
  const pretty = result.data ? JSON.stringify(result.data, null, 2) : result.error || '';
  const isError = !!result.error;
  return (
    <div style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 16px', background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
        <span style={{ fontSize: '0.75em', color: isError ? '#dc2626' : '#374151', fontFamily: 'monospace', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {isError ? '오류: ' : ''}{result.url}
        </span>
        <div style={{ display: 'flex', gap: 8, flexShrink: 0, marginLeft: 12 }}>
          <Button size="small" style={{ fontSize: '0.72em', color: '#6b7280', border: '1px solid #e5e7eb', padding: '2px 10px' }}
            onClick={() => navigator.clipboard.writeText(pretty)}>Copy</Button>
          <Button size="small" style={{ fontSize: '0.72em', color: '#6b7280', border: '1px solid #e5e7eb', padding: '2px 10px' }}
            onClick={onClear}>닫기</Button>
        </div>
      </div>
      <div style={{ flex: 1, overflow: 'auto', background: '#0d1117', padding: '16px 20px' }}>
        <pre style={{ fontFamily: "'JetBrains Mono','Fira Code','Consolas',monospace", fontSize: '0.8em', lineHeight: 1.7, color: isError ? '#f87171' : '#e6edf3', whiteSpace: 'pre', margin: 0 }}
          dangerouslySetInnerHTML={{ __html: isError ? result.error : colorizeJson(pretty) }} />
      </div>
    </div>
  );
}

export default function FhirLayout() {
  const classes = useStyles();

  const [selectedType, setSelectedType] = useState('CodeSystem');
  const [selectedIg, setSelectedIg]     = useState(null); // igId or null
  const [selectedId, setSelectedId]     = useState(null);
  const [editData, setEditData]         = useState(null);
  const [isNew, setIsNew]               = useState(false);
  const [isAdmin, setIsAdmin]           = useState(false);
  const [loginOpen, setLoginOpen]       = useState(false);
  const [mainTab, setMainTab]           = useState(0); // 0=Terminology, 1=Activity

  const FHIR_BASE = window.location.origin + '/fhir';
  const [fhirRequest, setFhirRequest] = useState({ base: FHIR_BASE, segments: ['CodeSystem'], params: [] });
  const [fhirResult, setFhirResult]   = useState(null); // { data, url, error } | null

  // 리소스 선택 시 RequestBar 자동 채우기
  useEffect(() => {
    if (selectedId) {
      setFhirRequest(prev => ({ ...prev, segments: [selectedType, selectedId], params: [] }));
      setFhirResult(null);
    } else if (selectedType) {
      setFhirRequest(prev => ({ ...prev, segments: [selectedType], params: [] }));
      setFhirResult(null);
    }
  }, [selectedType, selectedId]); // eslint-disable-line

  const isEditing = editData !== null || isNew;

  const handleSelectType = (type) => {
    setSelectedType(type); setSelectedIg(null);
    setSelectedId(null); setEditData(null); setIsNew(false);
  };

  const handleSelectIg = (igId, type) => {
    setSelectedIg(igId); setSelectedType(type);
    setSelectedId(null); setEditData(null); setIsNew(false);
  };

  const handleSelectId = (id) => {
    setSelectedId(id); setEditData(null); setIsNew(false);
  };

  const handleNew    = () => { setSelectedId(null); setEditData(null); setIsNew(true); };
  const handleEdit   = (data) => { setEditData(data); setIsNew(false); };
  const handleSaved  = () => { setEditData(null); setIsNew(false); setSelectedId(null); };
  const handleDeleted = () => { setSelectedId(null); };
  const handleCancelEdit = () => { setEditData(null); setIsNew(false); };

  // Breadcrumb
  const crumbs = [{ label: 'FHIR R4', onClick: () => handleSelectType(selectedType) }];
  if (selectedIg) crumbs.push({ label: selectedIg.split('#')[0], onClick: () => {} });
  if (selectedType) crumbs.push({ label: selectedType, onClick: () => { setSelectedId(null); setEditData(null); setIsNew(false); } });
  if (selectedId && !isEditing) crumbs.push({ label: selectedId });
  if (isEditing) crumbs.push({ label: isNew ? 'New' : 'Edit' });

  return (
    <div className={classes.root}>
      <div className={classes.subheader}>
        <div className={classes.breadcrumb}>
          {crumbs.map((c, i) => (
            <React.Fragment key={i}>
              {i > 0 && <NavigateNextIcon className={classes.crumbSep} style={{ fontSize: 14 }} />}
              {c.onClick
                ? <span className={classes.crumb} onClick={c.onClick}>{c.label}</span>
                : <span className={classes.crumbActive}>{c.label}</span>}
            </React.Fragment>
          ))}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          {isAdmin && (
            <Tabs value={mainTab} onChange={(_, v) => setMainTab(v)}
              style={{ minHeight: 0 }}
              TabIndicatorProps={{ style: { background: '#60a5fa', height: 2 } }}>
              <Tab label="Terminology" style={{ color: mainTab === 0 ? '#e2e8f0' : '#94a3b8', minHeight: 0, padding: '0 12px', fontSize: '0.75em' }} />
              <Tab label="Activity" style={{ color: mainTab === 1 ? '#e2e8f0' : '#94a3b8', minHeight: 0, padding: '0 12px', fontSize: '0.75em' }} />
            </Tabs>
          )}
          {isAdmin
            ? <>
                <Chip className={classes.adminChip} label="Admin" size="small" />
                <Button className={classes.loginBtn} variant="outlined" size="small"
                  startIcon={<ExitToAppIcon style={{ fontSize: 14 }} />}
                  onClick={() => { setIsAdmin(false); setMainTab(0); }}>로그아웃</Button>
              </>
            : <Button className={classes.loginBtn} variant="outlined" size="small"
                startIcon={<LockOutlinedIcon style={{ fontSize: 14 }} />}
                onClick={() => setLoginOpen(true)}>Admin 로그인</Button>
          }
        </div>
      </div>

      {isAdmin && mainTab === 1 ? (
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <ActivityPanel />
        </div>
      ) : null}

      <div className={classes.body} style={{ display: isAdmin && mainTab === 1 ? 'none' : 'flex' }}>
        <FhirTree
          selected={selectedType}
          selectedIg={selectedIg}
          onSelect={handleSelectType}
          onSelectIg={handleSelectIg}
          isAdmin={isAdmin}
        />

        <div className={classes.content}>
          {/* 목록 패널: 항상 표시 */}
          <div className={classes.listPane}>
            <ResourceList
              resourceType={selectedType}
              igId={selectedIg}
              isAdmin={isAdmin}
              onSelect={handleSelectId}
              onNew={handleNew}
            />
          </div>

          {/* 오른쪽 패널: 편집 or 상세 or 안내 */}
          <div className={classes.detailPane}>
            {/* FHIR Request Bar — 편집 중에는 숨김 */}
            {!isEditing && (
              <FhirRequestBar
                request={fhirRequest}
                onRequestChange={setFhirRequest}
                onResult={(r) => { setFhirResult(r); if (r) setSelectedId(null); }}
              />
            )}

            {isEditing ? (
              <ResourceEditor
                resourceType={selectedType}
                initialData={isNew ? null : editData}
                onSaved={handleSaved}
                onCancel={handleCancelEdit}
              />
            ) : fhirResult ? (
              /* RequestBar 실행 결과 raw viewer */
              <FhirRawResult result={fhirResult} onClear={() => setFhirResult(null)} />
            ) : selectedId ? (
              <ResourceDetail
                resourceType={selectedType}
                resourceId={selectedId}
                isAdmin={isAdmin}
                onEdit={handleEdit}
                onDeleted={handleDeleted}
                onBack={() => setSelectedId(null)}
              />
            ) : (
              <div className={classes.placeholder}>
                <span className={classes.placeholderIcon}>◈</span>
                <Typography className={classes.placeholderText}>
                  목록에서 리소스를 선택하거나 위에서 FHIR 명령어를 실행하세요
                </Typography>
              </div>
            )}
          </div>
        </div>
      </div>

      <LoginDialog open={loginOpen} onClose={() => setLoginOpen(false)} onLogin={() => { setIsAdmin(true); setLoginOpen(false); }} />
    </div>
  );
}
