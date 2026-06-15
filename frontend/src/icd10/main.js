import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { makeStyles, withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Divider from '@material-ui/core/Divider';
import Breadcrumbs from '@material-ui/core/Breadcrumbs';
import Link from '@material-ui/core/Link';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import CircularProgress from '@material-ui/core/CircularProgress';
import Chip from '@material-ui/core/Chip';

const BASE = '';

const KcdFlag = () => (
  <span title="KCD-9 확장 코드" style={{ marginRight: 3 }}>🇰🇷</span>
);

const StyledTableCell = withStyles(() => ({
  head: { backgroundColor: '#f5f5f5', fontSize: 11, fontWeight: 'bold' },
  body: { fontSize: 11 },
}))(TableCell);

const useStyles = makeStyles(() => ({
  label: { fontSize: '0.82em' },
  codeChip: { fontWeight: 'bold', fontSize: '1em', marginBottom: '8px' },
  kindBadge: {
    display: 'inline-block',
    background: '#e3f2fd',
    color: '#1565c0',
    borderRadius: 3,
    padding: '1px 6px',
    fontSize: '0.72em',
    marginRight: 4,
  },
  breadcrumb: { fontSize: '0.78em', cursor: 'pointer', whiteSpace: 'nowrap' },
  divider: { borderBottom: '2px solid #1565c0', marginBottom: 12 },
  sectionTitle: { fontSize: '0.78em', color: '#888', marginTop: 12, marginBottom: 4 },
  rubricLabel: { fontSize: '0.82em' },
  codeLink: { fontSize: '0.78em', cursor: 'pointer', color: '#1565c0', '&:hover': { textDecoration: 'underline' } },
  placeholder: { color: '#aaa', marginTop: 60, textAlign: 'center' },
}));

export default function Icd10Main({ code, setSelectedCode }) {
  const classes = useStyles();
  const [entity, setEntity] = useState(null);
  const [rubric, setRubric] = useState(null);
  const [ancestors, setAncestors] = useState([]);
  const [children, setChildren] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!code) return;
    setLoading(true);
    setEntity(null);
    setRubric(null);
    setAncestors([]);
    setChildren([]);

    Promise.all([
      axios.get(`${BASE}/entity/ICD10/${code}`).catch(() => null),
      axios.get(`${BASE}/rubric/ICD10/${code}`).catch(() => null),
      axios.get(`${BASE}/ancestor/ICD10/${code}`).catch(() => null),
      axios.get(`${BASE}/children/ICD10/${code}`).catch(() => null),
    ]).then(([e, r, a, c]) => {
      if (e) setEntity(e.data);
      if (r) setRubric(r.data);
      if (a) setAncestors(a.data || []);
      if (c) setChildren(c.data || []);
    }).finally(() => setLoading(false));
  }, [code]);

  if (!code) {
    return (
      <Typography className={classes.placeholder}>
        KCD-9 코드를 트리에서 선택하거나 검색하세요
      </Typography>
    );
  }

  if (loading) {
    return <Box textAlign="center" mt={6}><CircularProgress /></Box>;
  }

  if (!entity) return null;

  const rubricOf = (kind) =>
    rubric?.kinds?.filter((k) => k.kind === kind) || [];

  const preferred = rubricOf('preferred');
  const inclusion = rubricOf('inclusion');
  const exclusion = rubricOf('exclusion');
  const note = rubricOf('note');

  return (
    <Box>
      {/* Breadcrumb ancestors */}
      {ancestors.length > 0 && (
        <Breadcrumbs separator="›" className={classes.label} style={{ marginBottom: 8, flexWrap: 'nowrap', overflowX: 'auto' }}>
          {ancestors.map((a) => (
            <Link
              key={a.code}
              className={classes.breadcrumb}
              onClick={() => setSelectedCode(a.code)}
              color="inherit"
            >
              {a.isKcdExt && <KcdFlag />}
              <span style={{ fontWeight: 'bold', marginRight: 3 }}>{a.code}</span>
              {a.koreanLabel || a.label}
            </Link>
          ))}
        </Breadcrumbs>
      )}

      {/* Code + Label */}
      <Box display="flex" alignItems="flex-start" mb={1}>
        <Box style={{ display: 'flex', alignItems: 'center', flexShrink: 0, marginRight: 8, marginTop: 2 }}>
          {entity.isKcdExt && <KcdFlag />}
          <Chip label={entity.code} color="primary" size="small" className={classes.codeChip} />
        </Box>
        <Box>
          {entity.koreanLabel ? (
            <>
              <Typography variant="h6" style={{ fontSize: '1.15em', fontWeight: 'bold', lineHeight: 1.3 }}>
                {entity.koreanLabel}
              </Typography>
              <Typography style={{ fontSize: '0.9em', color: '#666', lineHeight: 1.4 }}>
                {entity.label}
              </Typography>
            </>
          ) : (
            <Typography variant="h6" style={{ fontSize: '1.15em', fontWeight: 'bold', lineHeight: 1.3 }}>
              {entity.label}
            </Typography>
          )}
        </Box>
      </Box>
      <Box mb={1}>
        {entity.classKind && <span className={classes.kindBadge}>{entity.classKind}</span>}
        {entity.usageKind && <span className={classes.kindBadge}>{entity.usageKind}</span>}
      </Box>
      <Divider className={classes.divider} />

      {/* Rubrics (full width, no siblings column) */}
      {preferred.length > 0 && (
        <>
          <Typography className={classes.sectionTitle}>PREFERRED</Typography>
          {preferred.map((r, i) => (
            <Typography key={i} className={classes.rubricLabel}>{r.label}</Typography>
          ))}
        </>
      )}

      {inclusion.length > 0 && (
        <>
          <Typography className={classes.sectionTitle}>INCLUSION</Typography>
          {inclusion.map((r, i) => (
            <Typography key={i} className={classes.label}>• {r.label}</Typography>
          ))}
        </>
      )}

      {exclusion.length > 0 && (
        <>
          <Typography className={classes.sectionTitle}>EXCLUSION</Typography>
          {exclusion.map((r, i) => (
            <Typography key={i} className={classes.label}>• {r.label}</Typography>
          ))}
        </>
      )}

      {note.length > 0 && (
        <>
          <Typography className={classes.sectionTitle}>NOTE</Typography>
          {note.map((r, i) => (
            <Typography key={i} className={classes.label} style={{ color: '#555' }}>{r.label}</Typography>
          ))}
        </>
      )}

      {/* Children table */}
      {children.length > 0 && (
        <>
          <Typography className={classes.sectionTitle} style={{ marginTop: 16 }}>
            CHILDREN ({children.length})
          </Typography>
          <TableContainer component={Paper} variant="outlined" style={{ marginTop: 4 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <StyledTableCell style={{ width: 100 }}>Code</StyledTableCell>
                  <StyledTableCell>Label</StyledTableCell>
                  <StyledTableCell style={{ width: 60 }} align="right">Children</StyledTableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {children.map((c) => (
                  <TableRow key={c.code} hover>
                    <TableCell style={{ fontSize: 11, whiteSpace: 'nowrap' }}>
                      {c.isKcdExt && <KcdFlag />}
                      <Link
                        className={classes.codeLink}
                        onClick={() => setSelectedCode(c.code)}
                      >
                        {c.code}
                      </Link>
                    </TableCell>
                    <TableCell style={{ fontSize: 11 }}>
                      {c.koreanLabel || c.label}
                      {c.koreanLabel && (
                        <span style={{ color: '#aaa', marginLeft: 4, fontSize: '0.88em' }}>{c.label}</span>
                      )}
                    </TableCell>
                    <TableCell style={{ fontSize: 11 }} align="right">{c.childrenCount || ''}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}
    </Box>
  );
}
