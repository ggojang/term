import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { makeStyles, withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Chip from '@material-ui/core/Chip';
import Divider from '@material-ui/core/Divider';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import CircularProgress from '@material-ui/core/CircularProgress';

const BASE = '';

const StyledTableCell = withStyles(() => ({
  head: { backgroundColor: '#f5f5f5', fontSize: 13, fontWeight: 'bold', padding: '6px 12px' },
  body: { fontSize: 13, padding: '5px 12px' },
}))(TableCell);

const useStyles = makeStyles(() => ({
  placeholder: { color: '#aaa', marginTop: 80, textAlign: 'center' },
  section: { fontSize: '0.78em', color: '#888', fontWeight: 'bold', marginTop: 16, marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.05em' },
  divider: { borderBottom: '2px solid #1565c0', marginBottom: 12 },
  chip: { fontWeight: 'bold', fontSize: '1em', marginBottom: 6 },
  badge: {
    display: 'inline-block', background: '#e3f2fd', color: '#1565c0',
    borderRadius: 3, padding: '1px 7px', fontSize: '0.75em', marginRight: 4,
  },
  label: { fontSize: '0.93em' },
  price: { color: '#4caf50', fontWeight: 'bold' },
  histRow: { '&:hover': { background: '#f9f9f9' } },
}));

function Field({ label, value, classes }) {
  if (value == null || value === '') return null;
  return (
    <Box display="flex" mb={0.5}>
      <Typography style={{ width: 160, flexShrink: 0, color: '#888', fontSize: '0.85em' }}>{label}</Typography>
      <Typography className={classes.label}>{value}</Typography>
    </Box>
  );
}

function PriceField({ label, value, classes }) {
  if (value == null) return null;
  return (
    <Box display="flex" mb={0.5}>
      <Typography style={{ width: 160, flexShrink: 0, color: '#888', fontSize: '0.85em' }}>{label}</Typography>
      <Typography className={classes.price}>{Number(value).toLocaleString()} 원</Typography>
    </Box>
  );
}

// ─── 행위 상세 ──────────────────────────────────────────────────────────────────
function 행위Detail({ code, classes }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!code) return;
    setLoading(true);
    axios.get(`${BASE}/hira/행위/${encodeURIComponent(code)}`)
      .then(r => setData(r.data))
      .catch(() => setData(null))
      .finally(() => setLoading(false));
  }, [code]);

  if (loading) return <Box textAlign="center" mt={6}><CircularProgress /></Box>;
  if (!data || !data.code) return <Typography className={classes.placeholder}>코드를 선택하세요</Typography>;

  const sheetBadge = (data.sheetType || '').replace('_', ' ');

  return (
    <Box>
      <Chip className={classes.chip} label={data.code} color="primary" variant="outlined" />
      <span className={classes.badge}>{sheetBadge}</span>
      {data.surgery === '1' && <span className={classes.badge} style={{ background: '#fff3e0', color: '#e65100' }}>수술</span>}

      <Divider className={classes.divider} style={{ marginTop: 8 }} />

      <Typography className={classes.section}>기본 정보</Typography>
      <Field label="수가코드" value={data.code} classes={classes} />
      <Field label="한글명" value={data.koreanLabel} classes={classes} />
      <Field label="영문명" value={data.englishLabel} classes={classes} />
      <Field label="산정명칭" value={data.calcName} classes={classes} />
      <Field label="분류번호" value={data.classNo} classes={classes} />
      <Field label="적용일자" value={data.applyDate} classes={classes} />
      <Field label="장구분" value={data.chapter} classes={classes} />
      <Field label="절구분" value={data.section} classes={classes} />
      <Field label="구분" value={data.division === '1' ? '1종' : data.division === '2' ? '2종' : data.division} classes={classes} />

      <Typography className={classes.section} style={{ marginTop: 16 }}>단가 (점)</Typography>
      <PriceField label="의원 단가" value={data.clinicPrice} classes={classes} />
      <PriceField label="병원급 이상 단가" value={data.hospitalPrice} classes={classes} />
      <Field label="상대가치점수" value={data.rvuPoint != null ? data.rvuPoint + ' 점' : null} classes={classes} />
    </Box>
  );
}

// ─── 약제 상세 ──────────────────────────────────────────────────────────────────
function 약제Detail({ code, classes }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!code) return;
    setLoading(true);
    axios.get(`${BASE}/hira/약제/${encodeURIComponent(code)}`)
      .then(r => setData(r.data))
      .catch(() => setData(null))
      .finally(() => setLoading(false));
  }, [code]);

  if (loading) return <Box textAlign="center" mt={6}><CircularProgress /></Box>;
  if (!data || !data.code) return <Typography className={classes.placeholder}>코드를 선택하세요</Typography>;

  return (
    <Box>
      <Chip className={classes.chip} label={data.code} color="primary" variant="outlined" />
      <span className={classes.badge}>{data.type}</span>
      <span className={classes.badge} style={{ background: '#e8f5e9', color: '#2e7d32' }}>{data.benefit}</span>

      <Divider className={classes.divider} style={{ marginTop: 8 }} />

      <Typography className={classes.section}>기본 정보</Typography>
      <Field label="제품코드" value={data.code} classes={classes} />
      <Field label="제품명" value={data.name} classes={classes} />
      <Field label="규격" value={data.spec} classes={classes} />
      <Field label="단위" value={data.unit} classes={classes} />
      <Field label="업체명" value={data.company} classes={classes} />
      <Field label="약효분류번호" value={data.classNo} classes={classes} />
      <Field label="주성분코드" value={data.ingredient} classes={classes} />
      <Field label="투여경로" value={data.route} classes={classes} />
      <Field label="최신 적용일자" value={data.applyDate} classes={classes} />
      <PriceField label="상한가" value={data.price} classes={classes} />

      {data.priceHistory && data.priceHistory.length > 1 && (
        <>
          <Typography className={classes.section} style={{ marginTop: 16 }}>가격 이력</Typography>
          <TableContainer component={Paper} variant="outlined" style={{ marginTop: 4 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <StyledTableCell>적용일자</StyledTableCell>
                  <StyledTableCell>상한가</StyledTableCell>
                  <StyledTableCell>급여기준</StyledTableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.priceHistory.map((h, i) => (
                  <TableRow key={i} className={classes.histRow}>
                    <StyledTableCell>{h.applyDate}</StyledTableCell>
                    <StyledTableCell>{h.price != null ? Number(h.price).toLocaleString() + ' 원' : '-'}</StyledTableCell>
                    <StyledTableCell>{h.benefit}</StyledTableCell>
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

// ─── 치료재료 상세 ─────────────────────────────────────────────────────────────
function 치료재료Detail({ code, classes }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!code) return;
    setLoading(true);
    axios.get(`${BASE}/hira/치료재료/${encodeURIComponent(code)}`)
      .then(r => setData(r.data))
      .catch(() => setData(null))
      .finally(() => setLoading(false));
  }, [code]);

  if (loading) return <Box textAlign="center" mt={6}><CircularProgress /></Box>;
  if (!data || !data.code) return <Typography className={classes.placeholder}>코드를 선택하세요</Typography>;

  return (
    <Box>
      <Chip className={classes.chip} label={data.code} color="primary" variant="outlined" />
      <span className={classes.badge}
        style={{ background: data.benefit === '급여' ? '#e8f5e9' : '#fce4ec', color: data.benefit === '급여' ? '#2e7d32' : '#c62828' }}>
        {data.benefit}
      </span>

      <Divider className={classes.divider} style={{ marginTop: 8 }} />

      <Typography className={classes.section}>기본 정보</Typography>
      <Field label="코드" value={data.code} classes={classes} />
      <Field label="품명" value={data.name} classes={classes} />
      <Field label="규격" value={data.spec} classes={classes} />
      <Field label="단위" value={data.unit} classes={classes} />
      <Field label="중분류" value={data.midClass} classes={classes} />
      <Field label="중분류코드" value={data.midCode} classes={classes} />
      <Field label="제조회사" value={data.manufacturer} classes={classes} />
      <Field label="재질" value={data.material} classes={classes} />
      <Field label="최초등재일" value={data.firstDate} classes={classes} />
      <Field label="적용일자" value={data.applyDate} classes={classes} />
      <PriceField label="상한금액" value={data.price} classes={classes} />
    </Box>
  );
}

// ─── 메인 패널 ─────────────────────────────────────────────────────────────────
export default function HiraMain({ category, code }) {
  const classes = useStyles();

  if (!code) {
    return (
      <Box mt={10} textAlign="center">
        <Typography className={classes.placeholder} style={{ fontSize: '1em' }}>
          좌측에서 코드를 선택하세요
        </Typography>
      </Box>
    );
  }

  if (category === '행위') return <Box p={2}><행위Detail code={code} classes={classes} /></Box>;
  if (category === '약제') return <Box p={2}><약제Detail code={code} classes={classes} /></Box>;
  if (category === '치료재료') return <Box p={2}><치료재료Detail code={code} classes={classes} /></Box>;
  return null;
}
