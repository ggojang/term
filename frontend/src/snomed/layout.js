import React, { useState, useEffect, createContext } from 'react';
import Main from './main.js';
import Left from './left.js';
import Right from './right.js';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import Grid from "@material-ui/core/Grid";
import { BrowserRouter as Router, Route, Switch, Redirect, Link } from "react-router-dom";

const useStyles = makeStyles((theme) => ({
  label: {
    fontSize: '0.9em',
  },
  gridcontainer: {
    position:"relative",
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
export default function Snomed(props) {

  const classes = useStyles();

  return (
    <>

      <Grid container
        className={clsx(classes.gridcontainer, classes.border)}
      >
          <Grid item md={3}>
            <Left setFromId={props.setFromId} setMrcmFromMain={props.setMrcmFromMain} setMrcmFromSearch={props.setMrcmFromSearch} />
          </Grid>
          <Grid item md={6}>
            <Main
              id={props.fromId}
              setFromId={props.setFromId}
              setMrcmFromMain={props.setMrcmFromMain }
            />
          </Grid>
          <Grid item className={clsx(classes.borderright)} md={3}>
            <Right id={props.fromId} setFromId={props.setFromId} mrcmFromMain={props.mrcmFromMain} mrcmFromSearch={props.mrcmFromSearch}/>
          </Grid>
      </Grid>
    </>
  )
}
