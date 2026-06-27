import React, { useState } from 'react';
import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';

const useStyles = makeStyles(() => ({
  title: {
    background: '#1e2d40',
    color: '#fff',
    padding: '20px 24px 16px',
  },
  icon: {
    verticalAlign: 'middle',
    marginRight: 8,
    fontSize: 20,
  },
  field: {
    marginBottom: 16,
  },
  error: {
    color: '#e53e3e',
    fontSize: '0.8em',
    marginTop: -8,
    marginBottom: 8,
  },
  loginBtn: {
    background: '#2563eb',
    color: '#fff',
    '&:hover': { background: '#1d4ed8' },
  },
}));

export default function LoginDialog({ open, onClose, onLogin }) {
  const classes = useStyles();
  const [id, setId] = useState('');
  const [pw, setPw] = useState('');
  const [error, setError] = useState('');

  const handleLogin = () => {
    if (id === 'admin' && pw === 'openehr123!') {
      onLogin();
      setId(''); setPw(''); setError('');
    } else {
      setError('아이디 또는 비밀번호가 올바르지 않습니다.');
    }
  };

  const handleKey = (e) => { if (e.key === 'Enter') handleLogin(); };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle disableTypography className={classes.title}>
        <Typography variant="h6">
          <LockOutlinedIcon className={classes.icon} />
          Admin 로그인
        </Typography>
      </DialogTitle>
      <DialogContent style={{ paddingTop: 24 }}>
        <TextField
          className={classes.field}
          label="아이디" variant="outlined" size="small" fullWidth
          value={id} onChange={e => setId(e.target.value)} onKeyPress={handleKey}
          autoFocus
        />
        <TextField
          className={classes.field}
          label="비밀번호" type="password" variant="outlined" size="small" fullWidth
          value={pw} onChange={e => setPw(e.target.value)} onKeyPress={handleKey}
        />
        {error && <Typography className={classes.error}>{error}</Typography>}
      </DialogContent>
      <DialogActions style={{ padding: '8px 24px 16px' }}>
        <Button onClick={onClose} size="small">취소</Button>
        <Button className={classes.loginBtn} variant="contained" size="small" onClick={handleLogin}>
          로그인
        </Button>
      </DialogActions>
    </Dialog>
  );
}
