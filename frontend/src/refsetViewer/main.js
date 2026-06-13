import React, { useState, useEffect, useRef, useCallback } from 'react';
import axios from 'axios';
import { withStyles, makeStyles } from '@material-ui/core/styles';

import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TablePagination from '@material-ui/core/TablePagination';
import TextField from '@material-ui/core/TextField';
import InputAdornment from '@material-ui/core/InputAdornment';
import IconButton from '@material-ui/core/IconButton';
import LinearProgress from '@material-ui/core/LinearProgress';
import Chip from '@material-ui/core/Chip';
import SearchIcon from '@material-ui/icons/Search';
import ClearIcon from '@material-ui/icons/Clear';

const StyledTableCell = withStyles(() => ({
  head: {
    backgroundColor: '#f0f4f8',
    color: '#37474f',
    fontSize: 12,
    fontWeight: 700,
    borderBottom: '2px solid #b0bec5',
    padding: '8px 12px',
  },
  body: {
    fontSize: 12,
    padding: '6px 12px',
  },
}))(TableCell);

const StyledTableRow = withStyles(() => ({
  root: {
    '&:nth-of-type(odd)': { backgroundColor: '#fafafa' },
    '&:hover': { backgroundColor: '#e8f0fe' },
  },
}))(TableRow);

const useStyles = makeStyles(() => ({
  header: {
    padding: '10px 14px 8px',
  },
  title: {
    fontWeight: 700,
    fontSize: '0.95em',
    color: '#1a237e',
  },
  divider: {
    height: 3,
    backgroundColor: '#1976d2',
    marginBottom: 10,
  },
  searchRow: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '0 14px 10px',
  },
  searchField: {
    flex: 1,
    maxWidth: 420,
    '& .MuiOutlinedInput-root': {
      borderRadius: 8,
      fontSize: '0.88em',
      height: 36,
    },
    '& .MuiOutlinedInput-input': {
      padding: '8px 10px',
    },
  },
  countChip: {
    fontSize: '0.78em',
    height: 24,
    backgroundColor: '#e3f2fd',
    color: '#1565c0',
    fontWeight: 700,
  },
  tableContainer: {
    border: '1px solid #e0e0e0',
    borderRadius: 6,
    overflow: 'hidden',
    margin: '0 14px',
  },
  idChip: {
    fontSize: '0.75em',
    height: 20,
    backgroundColor: '#ede7f6',
    color: '#4527a0',
    fontFamily: 'monospace',
    marginRight: 4,
    borderRadius: 3,
    padding: '0 4px',
    display: 'inline-block',
    verticalAlign: 'middle',
  },
  noData: {
    textAlign: 'center',
    color: '#90a4ae',
    padding: '32px 0',
    fontSize: '0.9em',
  },
  pagination: {
    fontSize: '0.8em',
    margin: '0 14px 0',
    borderTop: '1px solid #e0e0e0',
    '& .MuiTablePagination-caption': { fontSize: '0.8em' },
    '& .MuiTablePagination-select': { fontSize: '0.8em' },
  },
}));

// 300ms 디바운스 훅
function useDebounce(value, delay) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

const REFSET_DESCRIPTOR_ID = '900000000000456007';

export default function Main({ refset }) {
  const classes = useStyles();

  const [inputValue, setInputValue] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(25);
  const [loading, setLoading] = useState(false);
  const [member, setMember] = useState(null);   // raw axios response
  const [descript, setDescript] = useState(null);
  const [columns, setColumns] = useState([]);   // [{title}]
  const [rows, setRows] = useState([]);          // [[{id, name}]]
  const [total, setTotal] = useState(0);

  const q = useDebounce(inputValue, 300);
  const abortRef = useRef(null);

  // ── Refset 변경 시 초기화 ──────────────────────────────────────────────────
  useEffect(() => {
    setInputValue('');
    setPage(0);
    setSize(25);
    setMember(null);
    setDescript(null);
    setColumns([]);
    setRows([]);
    setTotal(0);

    // descriptor fetch
    axios
      .get(`http://api.infoclinic.co/members/SNOMEDCT?refcpntid=${refset.id}`)
      .then(res => setDescript(res))
      .catch(() => setDescript(null));
  }, [refset.id]);

  // ── Descriptor → columns 파싱 ─────────────────────────────────────────────
  useEffect(() => {
    if (!descript?.data) return;
    const ref = [];
    ref[REFSET_DESCRIPTOR_ID] = [];
    descript.data.forEach((item, index, desc) => {
      if (desc[index].extra?.['Attribute order']) {
        const order = desc[index].extra['Attribute order'].id;
        ref[REFSET_DESCRIPTOR_ID][order] = [];
        for (let key in desc[index].extra) {
          ref[REFSET_DESCRIPTOR_ID][order].push({
            title: key,
            id: desc[index].extra[key].id,
            name: desc[index].extra[key].name,
          });
        }
      }
    });
    // column 제목 추출 (Attribute order 컬럼 포함하여 순서대로)
    const cols = [];
    if (ref[REFSET_DESCRIPTOR_ID]?.['0']) {
      // 첫 번째 행의 컬럼명 = Referenced Component + 각 field
      ref[REFSET_DESCRIPTOR_ID]['0'].forEach((col, idx) => {
        if (idx === 0) cols.push('Referenced Component');
        else cols.push((col.name || '').split('(')[0].trim());
      });
    }
    setColumns(cols);
  }, [descript]);

  // ── Members fetch (refsetId / q / page / size 변경 시) ──────────────────────
  useEffect(() => {
    if (!refset?.id || refset.desc !== 0) return;

    // 이전 요청 취소
    if (abortRef.current) abortRef.current.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setLoading(true);
    const qParam = inputValue.trim() || '*';

    axios
      .get(
        `http://api.infoclinic.co/members/SNOMEDCT/${refset.id}?q=${encodeURIComponent(qParam)}&page=${page + 1}&size=${size}`,
        { signal: controller.signal }
      )
      .then(res => {
        setMember(res);
        setTotal(res.data?.totalElements || 0);
      })
      .catch(err => {
        if (axios.isCancel && axios.isCancel(err)) return;
        setMember(null);
        setTotal(0);
      })
      .finally(() => setLoading(false));
  }, [refset.id, q, page, size]); // eslint-disable-line

  // ── member → rows 빌드 ────────────────────────────────────────────────────
  useEffect(() => {
    if (!member?.data?.content || member.data.totalElements === 0) {
      setRows([]);
      return;
    }
    const built = member.data.content.map(item => {
      const cells = [];
      // col 0: Referenced Component
      cells.push({ id: item.referencedComponent?.id, name: item.referencedComponent?.name });
      // remaining fields
      for (let key in item.fields) {
        cells.push({ id: item.fields[key]?.id, name: item.fields[key]?.name });
      }
      return cells;
    });
    setRows(built);
  }, [member]);

  const handleClear = () => {
    setInputValue('');
    setPage(0);
  };

  const handleChangePage = (_, newPage) => setPage(newPage);
  const handleChangeSize = e => { setSize(+e.target.value); setPage(0); };

  const showTable = refset?.desc === 0;

  return (
    <div>
      {refset && (
        <Grid item md={12}>
          {/* Header */}
          <Box className={classes.header}>
            <Typography className={classes.title}>{refset.name}</Typography>
          </Box>
          <Divider className={classes.divider} />

          {showTable && (
            <>
              {/* Search bar */}
              <div className={classes.searchRow}>
                <TextField
                  className={classes.searchField}
                  variant="outlined"
                  size="small"
                  placeholder="검색어 입력 (비우면 전체 조회)..."
                  value={inputValue}
                  onChange={e => { setInputValue(e.target.value); setPage(0); }}
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <SearchIcon style={{ fontSize: 16, color: '#90a4ae' }} />
                      </InputAdornment>
                    ),
                    endAdornment: inputValue ? (
                      <InputAdornment position="end">
                        <IconButton size="small" onClick={handleClear}>
                          <ClearIcon style={{ fontSize: 14 }} />
                        </IconButton>
                      </InputAdornment>
                    ) : null,
                  }}
                />
                {!loading && total > 0 && (
                  <Chip
                    label={`${total.toLocaleString()} 건`}
                    className={classes.countChip}
                    size="small"
                  />
                )}
              </div>

              {/* Loading bar */}
              {loading && <LinearProgress style={{ margin: '0 14px 6px', borderRadius: 2 }} />}

              {/* Table */}
              {rows.length > 0 ? (
                <>
                  <TableContainer className={classes.tableContainer}>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          {columns.length > 0
                            ? columns.map((col, i) => (
                                <StyledTableCell key={i}>{col}</StyledTableCell>
                              ))
                            : rows[0].map((_, i) => (
                                <StyledTableCell key={i}>Column {i + 1}</StyledTableCell>
                              ))}
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {rows.map((row, ri) => (
                          <StyledTableRow key={ri}>
                            {row.map((cell, ci) => (
                              <StyledTableCell key={ci}>
                                {cell.id && (
                                  <span className={classes.idChip}>{cell.id}</span>
                                )}
                                {cell.name || ''}
                              </StyledTableCell>
                            ))}
                          </StyledTableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>

                  <TablePagination
                    className={classes.pagination}
                    rowsPerPageOptions={[10, 25, 50, 100]}
                    component="div"
                    count={total}
                    page={page}
                    rowsPerPage={size}
                    onChangePage={handleChangePage}
                    onChangeRowsPerPage={handleChangeSize}
                    labelRowsPerPage="페이지당:"
                  />
                </>
              ) : (
                !loading && (
                  <Typography className={classes.noData}>
                    {inputValue ? `"${inputValue}" 에 해당하는 멤버가 없습니다` : '멤버가 없습니다'}
                  </Typography>
                )
              )}
            </>
          )}
        </Grid>
      )}
    </div>
  );
}
