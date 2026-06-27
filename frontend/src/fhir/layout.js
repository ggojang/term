import React, { useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Chip from '@material-ui/core/Chip';
import NavigateNextIcon from '@material-ui/icons/NavigateNext';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import ExitToAppIcon from '@material-ui/icons/ExitToApp';

import FhirTree from './FhirTree';
import ResourceList from './ResourceList';
import ResourceDetail from './ResourceDetail';
import ResourceEditor from './ResourceEditor';
import LoginDialog from './LoginDialog';

const useStyles = makeStyles(() => ({
  root: {
    display: 'flex',
    flexDirection: 'column',
    height: 'calc(100vh - 48px)',
    background: '#f3f4f6',
  },
  // 상단 서브헤더 (breadcrumb + 로그인)
  subheader: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '0 20px',
    height: 40,
    background: '#1e2d40',
    borderBottom: '1px solid #162030',
    flexShrink: 0,
  },
  breadcrumb: {
    display: 'flex', alignItems: 'center', gap: 4,
  },
  crumb: {
    fontSize: '0.78em', color: '#94a3b8', cursor: 'pointer',
    '&:hover': { color: '#e2e8f0' },
  },
  crumbActive: {
    fontSize: '0.78em', color: '#e2e8f0', fontWeight: 500,
  },
  crumbSep: { color: '#475569', fontSize: '0.75em' },
  adminChip: {
    background: '#7c3aed22', color: '#a78bfa', border: '1px solid #7c3aed44',
    height: 24, fontSize: '0.72em',
  },
  loginBtn: {
    color: '#94a3b8', fontSize: '0.75em', padding: '2px 10px',
    border: '1px solid #334155', minHeight: 0,
    '&:hover': { color: '#e2e8f0', borderColor: '#475569', background: '#1e293b' },
  },
  // 메인 영역
  body: {
    display: 'flex',
    flex: 1,
    overflow: 'hidden',
  },
  // 콘텐츠 영역
  content: {
    flex: 1,
    display: 'flex',
    overflow: 'hidden',
    background: '#fff',
  },
  // 목록 + 상세 분할
  listPane: {
    width: 420,
    minWidth: 360,
    borderRight: '1px solid #e5e7eb',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
  },
  detailPane: {
    flex: 1,
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
  },
  placeholder: {
    flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center',
    flexDirection: 'column', gap: 12, color: '#d1d5db',
  },
  placeholderIcon: { fontSize: 48 },
  placeholderText: { fontSize: '0.85em' },
}));

export default function FhirLayout() {
  const classes = useStyles();

  const [selectedType, setSelectedType] = useState('CodeSystem');
  const [selectedId, setSelectedId]     = useState(null);
  const [editData, setEditData]         = useState(null); // null=목록/상세, object=편집모드
  const [isNew, setIsNew]               = useState(false);
  const [isAdmin, setIsAdmin]           = useState(false);
  const [loginOpen, setLoginOpen]       = useState(false);

  // 뷰 모드: 'list' | 'detail' | 'edit'
  const view = editData !== null || isNew ? 'edit' : selectedId ? 'detail' : 'list';

  const handleSelectType = (type) => {
    setSelectedType(type);
    setSelectedId(null);
    setEditData(null);
    setIsNew(false);
  };

  const handleSelectId = (id) => {
    setSelectedId(id);
    setEditData(null);
    setIsNew(false);
  };

  const handleNew = () => {
    setSelectedId(null);
    setEditData(null);
    setIsNew(true);
  };

  const handleEdit = (data) => {
    setEditData(data);
    setIsNew(false);
  };

  const handleSaved = () => {
    setEditData(null);
    setIsNew(false);
    // 목록으로 돌아가서 갱신 트리거 (key 변경)
    setSelectedId(null);
    setSelectedType(t => t); // force re-render
  };

  const handleDeleted = () => {
    setSelectedId(null);
  };

  const handleLoginSuccess = () => {
    setIsAdmin(true);
    setLoginOpen(false);
  };

  // Breadcrumb
  const crumbs = [{ label: 'FHIR R4', onClick: () => handleSelectType(selectedType) }];
  if (selectedType) crumbs.push({ label: selectedType, onClick: () => { setSelectedId(null); setEditData(null); setIsNew(false); } });
  if (selectedId && view === 'detail') crumbs.push({ label: selectedId });
  if (view === 'edit') crumbs.push({ label: isNew ? 'New' : 'Edit' });

  return (
    <div className={classes.root}>
      {/* 서브헤더 */}
      <div className={classes.subheader}>
        <div className={classes.breadcrumb}>
          {crumbs.map((c, i) => (
            <React.Fragment key={i}>
              {i > 0 && <NavigateNextIcon className={classes.crumbSep} style={{ fontSize: 14 }} />}
              {c.onClick ? (
                <span className={classes.crumb} onClick={c.onClick}>{c.label}</span>
              ) : (
                <span className={classes.crumbActive}>{c.label}</span>
              )}
            </React.Fragment>
          ))}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          {isAdmin
            ? <>
                <Chip className={classes.adminChip} label="Admin" size="small" />
                <Button className={classes.loginBtn} variant="outlined" size="small"
                  startIcon={<ExitToAppIcon style={{ fontSize: 14 }} />}
                  onClick={() => setIsAdmin(false)}>
                  로그아웃
                </Button>
              </>
            : <Button className={classes.loginBtn} variant="outlined" size="small"
                startIcon={<LockOutlinedIcon style={{ fontSize: 14 }} />}
                onClick={() => setLoginOpen(true)}>
                Admin 로그인
              </Button>
          }
        </div>
      </div>

      {/* 본문 */}
      <div className={classes.body}>
        <FhirTree selected={selectedType} onSelect={handleSelectType} />

        <div className={classes.content}>
          {/* 편집 모드: 전체 너비 */}
          {view === 'edit' ? (
            <ResourceEditor
              resourceType={selectedType}
              initialData={isNew ? null : editData}
              onSaved={handleSaved}
              onCancel={() => { setEditData(null); setIsNew(false); }}
            />
          ) : (
            <>
              {/* 목록 패널 */}
              <div className={classes.listPane}>
                <ResourceList
                  resourceType={selectedType}
                  isAdmin={isAdmin}
                  onSelect={handleSelectId}
                  onNew={handleNew}
                />
              </div>

              {/* 상세 패널 */}
              <div className={classes.detailPane}>
                {selectedId ? (
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
                      목록에서 리소스를 선택하세요
                    </Typography>
                  </div>
                )}
              </div>
            </>
          )}
        </div>
      </div>

      <LoginDialog open={loginOpen} onClose={() => setLoginOpen(false)} onLogin={handleLoginSuccess} />
    </div>
  );
}
