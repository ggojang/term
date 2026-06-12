import React, { useState, useEffect, createContext } from 'react';
import TableOfContents from './ehrLeft.js';
import SearchByText from './searchByText.js';
import SearchById from './searchById.js';
import SearchByWordEquivalents from './searchByWordEquivalents.js';
import SearchBySynonym from './searchBySynonym.js';
import SearchByPostcoordination from './searchByPostcoordination.js';
import SearchByStatus from './searchByStatus.js';
import SearchBySupertypeAncestors from './searchBySupertypeAncestors.js';
import SearchBySemanticTag from './searchBySemanticTag.js';
import SearchByOrderPreferred from './searchByOrderPreferred.js';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import Container from '@material-ui/core/Container';
import Grid from "@material-ui/core/Grid";
import { BrowserRouter as Router, Route, Switch, Redirect } from "react-router-dom";

const useStyles = makeStyles((theme) => ({
  label: {
    fontSize: '0.9em',
  },
  container: {
    '-ms-overflow-style': 'none', /* IE and Edge */
    scrollbarWidth: 'none', /* Firefox */
    '&::-webkit-scrollbar': {
        display: 'none', /* Chrome, Safari, Opera*/
    },
  },
  gridcontainer: {
    position:"relative",
    padding:"4px 0px 0px 0px",
    margin:"0px 0px 0px 0px"
  }
}));


export default function EHRLayout(props) {

  const [fromEHRLeft, setFromEHRLeft] = useState('41');
  const [toEHRMain, settoEHRMain] = useState('root');

  const classes = useStyles();

  //console.log(fromEHRLeft);

  return (

      <Grid container className={clsx(classes.gridcontainer)}>
          <Grid item md={3}>
          <Container
            className={classes.container} /*ref={setRef}*/
            style={{
              margin : "0 0 0 0",
              padding: "12px 0 0 12px",
              height: "90vh",
              overflow: "scroll"}}>
              <TableOfContents toEHRMain={toEHRMain} setFromEHRLeft={setFromEHRLeft}/>
            </Container>
          </Grid>
          <Grid item md={1}>
          </Grid>
          <Grid item md={7} >
              { fromEHRLeft === '41' &&
                <SearchByText />
              }
              { fromEHRLeft === '42' &&
                <SearchById />
              }
              { fromEHRLeft === '431' &&
                <SearchByWordEquivalents />
              }
              { fromEHRLeft === '4311' &&
                <SearchBySynonym />
              }
              { fromEHRLeft === '432' &&
                <SearchByPostcoordination />
              }
              { fromEHRLeft === '441' &&
                <SearchByStatus />
              }
              { fromEHRLeft === '442' &&
                <SearchBySupertypeAncestors />
              }
              { fromEHRLeft === '4421' &&
                <SearchBySemanticTag />
              }
              { fromEHRLeft === '512' &&
                <SearchByOrderPreferred />
              }
          </Grid>
          <Grid item md={1}>
          </Grid>
      </Grid>

  )
}
