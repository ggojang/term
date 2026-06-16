import React, { useState } from 'react';
import Left from './left.js';
import Main from './main.js';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Container from '@material-ui/core/Container';

const useStyles = makeStyles(() => ({
  gridBorder: {
    borderRight: 'dotted 1px lightGray',
  },
}));

export default function Icd10Layout({ selectedCode, setSelectedCode }) {
  const classes = useStyles();

  return (
    <Grid container>
      <Grid item md={3} className={classes.gridBorder}>
        <Left setSelectedCode={setSelectedCode} />
      </Grid>
      <Grid item md={9}>
        <Container style={{ margin: 0, padding: '12px 0 0 12px', height: '90vh', overflow: 'scroll' }}>
          <Main code={selectedCode} setSelectedCode={setSelectedCode} />
        </Container>
      </Grid>
    </Grid>
  );
}
