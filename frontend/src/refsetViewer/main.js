import React, { useState, useEffect, useRef } from 'react';
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
    padding: '6px 10px',
    whiteSpace: 'nowrap',
  },
  body: {
    fontSize: 12,
    padding: '4px 10px',
    verticalAlign: 'middle',
  },
}))(TableCell);

const StyledTableRow = withStyles(() => ({
  root: {
    '&:nth-of-type(odd)': { backgroundColor: '#fafafa' },
    '&:hover': { backgroundColor: '#e8f0fe' },
  },
}))(TableRow);

const useStyles = makeStyles(() => ({
  root: {
    display: 'flex',
    flexDirection: 'column',
    height: '85vh',
  },
  header: {
    padding: '8px 14px 6px',
    flexShrink: 0,
  },
  title: {
    fontWeight: 700,
    fontSize: '0.93em',
    color: '#1a237e',
  },
  divider: {
    height: 3,
    backgroundColor: '#1976d2',
    flexShrink: 0,
  },
  searchRow: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '8px 14px',
    flexShrink: 0,
  },
  searchField: {
    flex: 1,
    maxWidth: 400,
    '& .MuiOutlinedInput-root': {
      borderRadius: 8,
      fontSize: '0.85em',
      height: 34,
    },
    '& .MuiOutlinedInput-input': {
      padding: '7px 10px',
    },
  },
  countChip: {
    fontSize: '0.78em',
    height: 22,
    backgroundColor: '#e3f2fd',
    color: '#1565c0',
    fontWeight: 700,
  },
  tableWrapper: {
    flex: 1,
    overflow: 'hidden',
    margin: '0 14px',
    display: 'flex',
    flexDirection: 'column',
    border: '1px solid #e0e0e0',
    borderRadius: 6,
  },
  tableContainer: {
    flex: 1,
    overflow: 'auto',
  },
  noData: {
    textAlign: 'center',
    color: '#90a4ae',
    padding: '32px 0',
    fontSize: '0.88em',
  },
  pagination: {
    flexShrink: 0,
    borderTop: '1px solid #e0e0e0',
    '& .MuiTablePagination-caption': { fontSize: '0.78em' },
    '& .MuiTablePagination-select': { fontSize: '0.78em' },
    '& .MuiTablePagination-toolbar': { minHeight: 40 },
  },
  cellText: {
    fontSize: '0.82em',
    color: '#212121',
  },
  cellId: {
    fontSize: '0.75em',
    color: '#78909c',
    fontFamily: 'monospace',
  },
}));

function useDebounce(value, delay) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

export default function Main({ refset }) {
  const classes = useStyles();

  const [inputValue, setInputValue] = useState('');
  const [page, setPage]   = useState(0);
  const [size, setSize]   = useState(25);
  const [loading, setLoading] = useState(false);
  const [columns, setColumns] = useState([]);
  const [rows, setRows]   = useState([]);
  const [total, setTotal] = useState(0);

  const q = useDebounce(inputValue, 300);
  const abortRef = useRef(null);

  // refset 변경 시 초기화 + descriptor 로드
  useEffect(() => {
    setInputValue('');
    setPage(0);
    setRows([]);
    setTotal(0);
    setColumns([]);

    if (!refset?.id) return;

    axios.get(`http://api.infoclinic.co/members/SNOMEDCT?refcpntid=${refset.id}`)
      .then(res => {
        if (!res.data?.length) return;

        // fields[0].name = attribute label (column name)
        // fields[2].name = attribute order (numeric string "0","1","2"...)
        const colMap = {};
        res.data.forEach(item => {
          const label = item.fields?.[0]?.name;
          const orderStr = item.fields?.[2]?.name;
          const order = parseInt(orderStr, 10);
          if (label && !isNaN(order)) colMap[order] = label;
        });

        if (Object.keys(colMap).length > 0) {
          const maxOrder = Math.max(...Object.keys(colMap).map(Number));
          const cols = [];
          for (let i = 0; i <= maxOrder; i++) {
            cols.push(colMap[i] || `Column ${i + 1}`);
          }
          setColumns(cols);
        }
      })
      .catch(() => {});
  }, [refset?.id]);

  // 멤버 검색
  useEffect(() => {
    if (!refset?.id || refset.desc !== 0) return;

    if (abortRef.current) abortRef.current.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setLoading(true);
    const qParam = inputValue.trim() || '*';

    axios.get(
      `http://api.infoclinic.co/members/SNOMEDCT/${refset.id}?q=${encodeURIComponent(qParam)}&page=${page + 1}&size=${size}`,
      { signal: controller.signal }
    )
      .then(res => {
        setTotal(res.data?.totalElements || 0);
        const content = res.data?.content || [];
        setRows(content.map(item => {
          const cells = [];
          // col 0: referencedComponent
          const rc = item.referencedComponent;
          cells.push({ id: rc?.id, name: rc?.name });
          // 추가 필드
          (item.fields || []).forEach(f => {
            cells.push({ id: f?.id, name: f?.name });
          });
          return cells;
        }));
      })
      .catch(err => {
        if (!axios.isCancel?.(err)) { setRows([]); setTotal(0); }
      })
      .finally(() => setLoading(false));
  }, [refset?.id, q, page, size]); // eslint-disable-line

  const handleClear = () => { setInputValue(''); setPage(0); };
  const handleChangePage = (_, p) => setPage(p);
  const handleChangeSize = e => { setSize(+e.target.value); setPage(0); };

  const showTable = refset?.desc === 0;

  // 컬럼 헤더: descriptor로부터 얻거나, 첫 행 기준으로 fallback
  function getHeaders() {
    if (columns.length > 0) return columns;
    if (rows.length > 0) {
      return rows[0].map((_, i) => i === 0 ? 'Referenced Component' : `Field ${i}`);
    }
    return [];
  }

  // 셀 표시: name 우선, 없으면 id를 회색 소자체로
  function renderCell(cell) {
    if (cell.name) return <span className={classes.cellText}>{cell.name}</span>;
    if (cell.id)   return <span className={classes.cellId}>{cell.id}</span>;
    return null;
  }

  return (
    <div className={classes.root}>
      {refset && (
        <>
          <Box className={classes.header}>
            <Typography className={classes.title}>{refset.name}</Typography>
          </Box>
          <Divider className={classes.divider} />

          {showTable && (
            <>
              {/* 검색 바 */}
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
                        <SearchIcon style={{ fontSize: 15, color: '#90a4ae' }} />
                      </InputAdornment>
                    ),
                    endAdornment: inputValue ? (
                      <InputAdornment position="end">
                        <IconButton size="small" onClick={handleClear}>
                          <ClearIcon style={{ fontSize: 13 }} />
                        </IconButton>
                      </InputAdornment>
                    ) : null,
                  }}
                />
                {!loading && total > 0 && (
                  <Chip label={`${total.toLocaleString()} 건`} className={classes.countChip} size="small" />
                )}
              </div>

              {loading && <LinearProgress style={{ margin: '0 14px 4px', borderRadius: 2 }} />}

              {/* 테이블 + 페이지네이션 (함께 flex 박스 안에서 고정) */}
              {rows.length > 0 ? (
                <div className={classes.tableWrapper}>
                  <TableContainer className={classes.tableContainer}>
                    <Table size="small" stickyHeader>
                      <TableHead>
                        <TableRow>
                          {getHeaders().map((col, i) => (
                            <StyledTableCell key={i}>{col}</StyledTableCell>
                          ))}
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {rows.map((row, ri) => (
                          <StyledTableRow key={ri}>
                            {row.map((cell, ci) => (
                              <StyledTableCell key={ci}>{renderCell(cell)}</StyledTableCell>
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
                </div>
              ) : (
                !loading && (
                  <Typography className={classes.noData}>
                    {inputValue ? `"${inputValue}" 에 해당하는 멤버가 없습니다` : '멤버가 없습니다'}
                  </Typography>
                )
              )}
            </>
          )}
        </>
      )}
    </div>
  );
}
