import React, { useState, useEffect} from 'react';
import Main from './main.js';
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
  border: {
    borderRight: "1px solid text.secondary",
  },
  borderright: {
    borderRight: "1px solid #ffffff",
  },
}));

/*export const AppContext = createContext();
*/
export default function MapLayout(props) {

  const classes = useStyles();

  const [semanTag, setSemanTag] = useState([]); // Referense set

  return (
    <>

      <Grid container
        className={clsx(classes.gridcontainer, classes.border)}
      >
          <Grid item md={2}>
            <Container
              className={classes.container} /*ref={setRef}*/
              style={{
                margin : "0 0 0 0",
                padding: "4px 0 0 4px",
                height: "90vh",
                overflow: "scroll"}}>
              <Left
                setSemanTag={setSemanTag}
              />
            </Container>
          </Grid>
          <Grid item md={10}>
            <Main
              semanTag={semanTag}
            />
          </Grid>
      </Grid>
    </>
  )
}

