import React, { useState, useEffect, createContext } from 'react';
import Middle from './middle.js';
import Left from './left.js';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import Container from '@material-ui/core/Container';
import Grid from "@material-ui/core/Grid";

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
  },
  gridBorder: {
    borderRight: "dotted 1px lightGray",
  },
  border: {
    borderRight: "1px solid text.secondary",
  },
  borderright: {
    borderRight: "1px solid #ffffff",
  },
}));

/*export const AppContext = createContext();
*/
export default function RefsetLayout(props) {

  const classes = useStyles();

  const [refset, setRefset] = useState({name: 'Reference set', id: '900000000000455006', desc:1}); // Referense set

  return (
    <>

      <Grid container
        className={clsx(classes.gridcontainer, classes.border)}
      >
          <Grid item md={3} className={classes.gridBorder}>
            <Container
              className={classes.container} /*ref={setRef}*/
              style={{
                margin : "0 0 0 0",
                padding: "12px 0 0 12px",
                height: "90vh",
                overflow: "scroll"}}>
              <Left
                setRefset={setRefset}
              />
            </Container>
          </Grid>
          <Grid item md={9}>
            <Middle
              refset={refset}
            />
          </Grid>
      </Grid>
    </>
  )
}

