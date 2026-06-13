import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { makeStyles, withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Divider from '@material-ui/core/Divider';
import Breadcrumbs from '@material-ui/core/Breadcrumbs';
import Link from '@material-ui/core/Link';
import Grid from '@material-ui/core/Grid';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import CircularProgress from '@material-ui/core/CircularProgress';
import Chip from '@material-ui/core/Chip';

const BASE = window.location.hostname === 'localhost' ? '' : 'http://api.infoclinic.co';

const KR_FLAG = ({ title }) => (
  <span title={title || 'KCD-9 확장 코드'} style={{ marginRight: 3, fontSize: '0.9em' }}>🇰🇷</span>
);

const StyledTableCell = withStyles(() => ({
  head: { backgroundColor: '#f5f5f5', fontSize: 14, fontWeight: 'bold' },
  body: { fontSize: 14 },
}))(TableCell);

const useStyles = makeStyles(() => ({
  label:       { fontSize: '0.93em' },
  codeChip:    { fontWeight: 'bold', fontSize: '1em', marginBottom: '6px' },
  kindBadge: {
    display: 'inline-block',
    background: '#e3f2fd',
    color: '#1565c0',
    borderRadius: 3,
    padding: '1px 6px',
    fontSize: '0.75em',
    marginRight: 4,
  },
  breadcrumb:   { fontSize: '0.88em', cursor: 'pointer' },
  divider:      { borderBottom: '2px solid #1565c0', marginBottom: 14 },
  sectionTitle: { fontSize: '0.82em', color: '#888', fontWeight: 'bold', marginTop: 14, marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.05em' },
  rubricLabel:  { fontSize: '0.93em' },
  siblingLink:  { fontSize: '0.88em', cursor: 'pointer', color: '#1565c0', '&:hover': { textDecoration: 'underline' } },
  placeholder:  { color: '#aaa', marginTop: 60, textAlign: 'center' },
}));

export default function Icd10Main({ code, setSelectedCode }) {
  const classes = useStyles();
  const [entity, setEntity] = useState(null);
  const [rubric, setRubric] = useState(null);
  const [ancestors, setAncestors] = useState([]);
  const [siblings, setSiblings] = useState([]);
  const [children, setChildren] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!code) return;
    setLoading(true);
    setEntity(null); setRubric(null);
    setAncestors([]); setSiblings([]); setChildren([]);

    Promise.all([
      axios.get(`${BASE}/entity/ICD10/${code}`).catch(() => null),
      axios.get(`${BASE}/rubric/ICD10/${code}`).catch(() => null),
      axios.get(`${BASE}/ancestor/ICD10/${code}`).catch(() => null),
      axios.get(`${BASE}/sibling/ICD10/${code}`).catch(() => null),
      axios.get(`${BASE}/children/ICD10/${code}`).catch(() => null),
    ]).then(([e, r, a, s, c]) => {
      if (e) setEntity(e.data);
      if (r) setRubric(r.data);
      if (a) setAncestors(a.data || []);
      if (s) setSiblings(s.data?.siblings || []);
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

  if (loading) return <Box textAlign="center" mt={6}><CircularProgress /></Box>;
  if (!entity) return null;

  const rubricOf = (kind) => rubric?.kinds?.filter((k) => k.kind === kind) || [];
  const preferred = rubricOf('preferred');
  const inclusion = rubricOf('inclusion');
  const exclusion = rubricOf('exclusion');
  const note      = rubricOf('note');

  return (
    <Box>
      {/* Breadcrumb ancestors */}
      {ancestors.length > 0 && (
        <Breadcrumbs separator="›" style={{ marginBottom: 10, fontSize: '0.88em' }}>
          {ancestors.map((a) => (
            <Link key={a.code} className={classes.breadcrumb} onClick={() => setSelectedCode(a.code)} color="inherit">
              {a.isKcdExt && <KR_FLAG />}
              <span style={{ fontWeight: 'bold', marginRight: 3 }}>{a.code}</span>
              {a.koreanLabel || a.label}
            </Link>
          ))}
        </Breadcrumbs>
      )}

      {/* Code + Korean title + English title */}
      <Box display="flex" alignItems="flex-start" mb={1}>
        <Chip label={entity.code} color="primary" size="small" className={classes.codeChip} style={{ marginRight: 10, marginTop: 2, flexShrink: 0 }} />
        <Box>
          {entity.koreanLabel ? (
            <>
              <Typography variant="h6" style={{ fontSize: '1.15em', fontWeight: 'bold', lineHeight: 1.3 }}>
                {entity.isKcdExt && <KR_FLAG />}
                {entity.koreanLabel}
              </Typography>
              <Typography style={{ fontSize: '0.9em', color: '#666', lineHeight: 1.4 }}>
                {entity.label}
              </Typography>
            </>
          ) : (
            <Typography variant="h6" style={{ fontSize: '1.15em', fontWeight: 'bold', lineHeight: 1.3 }}>
              {entity.isKcdExt && <KR_FLAG />}
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

      <Grid container spacing={2}>
        {/* Left: rubrics */}
        <Grid item md={7}>
          {preferred.length > 0 && (
            <>
              <Typography className={classes.sectionTitle}>Preferred</Typography>
              {preferred.map((r, i) => (
                <Typography key={i} className={classes.rubricLabel}>{r.label}</Typography>
              ))}
            </>
          )}
          {inclusion.length > 0 && (
            <>
              <Typography className={classes.sectionTitle}>Inclusion</Typography>
              {inclusion.map((r, i) => (
                <Typography key={i} className={classes.label}>• {r.label}</Typography>
              ))}
            </>
          )}
          {exclusion.length > 0 && (
            <>
              <Typography className={classes.sectionTitle}>Exclusion</Typography>
              {exclusion.map((r, i) => (
                <Typography key={i} className={classes.label}>• {r.label}</Typography>
              ))}
            </>
          )}
          {note.length > 0 && (
            <>
              <Typography className={classes.sectionTitle}>Note</Typography>
              {note.map((r, i) => (
                <Typography key={i} className={classes.label} style={{ color: '#555' }}>{r.label}</Typography>
              ))}
            </>
          )}
        </Grid>

        {/* Right: siblings */}
        <Grid item md={5}>
          {siblings.length > 0 && (
            <>
              <Typography className={classes.sectionTitle}>Siblings</Typography>
              {siblings.map((s) => (
                <Box key={s.code} mb={0.5}>
                  {s.isKcdExt && <KR_FLAG />}
                  <span className={classes.siblingLink} onClick={() => setSelectedCode(s.code)}>
                    {s.code}
                  </span>
                  <Typography component="span" className={classes.label} style={{ marginLeft: 5 }}>
                    {s.koreanLabel || s.label}
                  </Typography>
                  {s.koreanLabel && s.label && (
                    <Typography component="span" style={{ marginLeft: 5, fontSize: '0.8em', color: '#bbb' }}>
                      {s.label}
                    </Typography>
                  )}
                </Box>
              ))}
            </>
          )}
        </Grid>
      </Grid>

      {/* Children table */}
      {children.length > 0 && (
        <>
          <Typography className={classes.sectionTitle} style={{ marginTop: 18 }}>
            Children ({children.length})
          </Typography>
          <TableContainer component={Paper} variant="outlined" style={{ marginTop: 4 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <StyledTableCell style={{ width: 100 }}>Code</StyledTableCell>
                  <StyledTableCell>Label</StyledTableCell>
                  <StyledTableCell style={{ width: 60 }} align="right">Sub</StyledTableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {children.map((c) => (
                  <TableRow key={c.code} hover>
                    <TableCell style={{ fontSize: 14, verticalAlign: 'top' }}>
                      {c.isKcdExt && <KR_FLAG />}
                      <Link className={classes.siblingLink} onClick={() => setSelectedCode(c.code)}>
                        {c.code}
                      </Link>
                    </TableCell>
                    <TableCell style={{ fontSize: 14 }}>
                      {c.koreanLabel && (
                        <span style={{ display: 'block', fontWeight: 500 }}>{c.koreanLabel}</span>
                      )}
                      <span style={{ color: c.koreanLabel ? '#999' : 'inherit', fontSize: c.koreanLabel ? '0.9em' : 'inherit' }}>
                        {c.label}
                      </span>
                    </TableCell>
                    <TableCell style={{ fontSize: 14 }} align="right">{c.childrenCount || ''}</TableCell>
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
