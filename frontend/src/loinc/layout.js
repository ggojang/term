import React, {useState, useEffect} from 'react';
import Main from './main.js';
import Left from './left.js';
import PanelTree from './right.js';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import Container from '@material-ui/core/Container';
import Grid from "@material-ui/core/Grid";

const useStyles = makeStyles((theme) => ({
  label: {
    fontSize: '0.9em',
  },
  gridcontainer: {
    position:"relative",
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
export default function LoincLayout(props) {

  const classes = useStyles();

  const [panelType, setPanelType] = useState('');

  return (
    <>
      <Grid container
        className={clsx(classes.gridcontainer, classes.border)}
      >
        <Grid item md={3} className={classes.gridBorder}>
          <Left
            setLoincId={props.setLoincId}
            setPanelType={setPanelType}

          />
        </Grid>
      { !panelType
      ? (
        <Grid item md={9}>
          <Container
            className={classes.container}
            style={{
              margin : "0 0 0 0",
              padding: "12px 0 0 12px",
              height: "90vh",
              overflow: "scroll"}}>
            <Main
              loincId={props.loincId}
              setLoincId={props.setLoincId}
              setPanelType={setPanelType}
            />
          </Container>
        </Grid>
      ) : (
        <>
        <Grid item md={6}>
          <Container
            className={classes.container}
            style={{
              margin : "0 0 0 0",
              padding: "12px 0 0 12px",
              height: "90vh",
              overflow: "scroll"}}>
            <Main
              loincId={props.loincId}
              setLoincId={props.setLoincId}
              setPanelType={setPanelType}
            />
          </Container>
        </Grid>
        <Grid item md={3}>
        <Container
          className={classes.container}
          style={{
            margin : "0 0 0 0",
            padding: "12px 0 0 12px",
            height: "90vh",
            overflow: "scroll"}}>
          <PanelTree
            loincId={props.loincId}
            setLoincId={props.setLoincId}
          />
        </Container>
        </Grid>
        </>
      )
      }
      </Grid>
    </>
  )
}

/*
<Grid item md={6}>
  <Main
    loincId={props.loincId}
    setLoincId={props.setLoincId}
    idFromSearchToMain={props.idFromSearchToMain}
    idFromTreeToMain={props.idFromTreeToMain}
    idFromPanelToMain={props.idFromPanelToMain}
    setIdFromMainToPanel={props.setIdFromMainToPanel }
  />
</Grid>
<Grid item className={clsx(classes.borderright)} md={3}>
  <Right
    loincId={props.loincId}
    setLoincId={props.setLoincId}
    idFromSearchToPanel={props.idFromSearchToPanel}
    idFromTreeToPanel={props.idFromTreeToPanel}
    idFromMainToPanel={props.idFromMainToPanel}
    setIdFromPanelToMain={props.setIdFromPanelToMain}
  />
</Grid>
*/
