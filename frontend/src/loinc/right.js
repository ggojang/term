import React, { useState, useEffect} from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import Grid from "@material-ui/core/Grid";
import TreeView from '@material-ui/lab/TreeView';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import RemoveIcon from '@material-ui/icons/Remove';
import TreeItem from '@material-ui/lab/TreeItem';
import Typography from '@material-ui/core/Typography';

const useStyles = makeStyles((theme) => ({
  container: {
    '-ms-overflow-style': 'none', /* IE and Edge */
    scrollbarWidth: 'none', /* Firefox */
    '&::-webkit-scrollbar': {
        display: 'none', /* Chrome, Safari, Opera*/
    },
  },
  treeItemLabel: {
    fontSize: "0.9em",
  },
  treeItemSelected: {
    backgroundColor: "#fff",
  },
  link: {
    textDecoration: "none",
    color: '#000',
    /* '&:hover': {
      color: '#3a87ad',
    }, */
  },
  label: {
    fontSize: '0.9em',
  },
}));



export default function PanelTree(props) {

  const classes = useStyles();

  const [result, setResult] = useState([]);
  const [tmp, setTmp] = useState(0);

  useEffect(() => {
    if (props.loincId) {
      setResult([]);
      axios
      .get(`http://api.infoclinic.co/panel/LOINC/${props.loincId}`)
      .then(result =>
        setResult(result)
      );
    }
  }, [props.loincId]);

  const makeTree = (children) => (
    <>
    { children !== null &&
      <>
      { children.map((ch, index) => (
        <div key={index}>
        { ch.children !== null
          ? (
            <TreeItem
              classes={{label:classes.treeItemLabel}}
              nodeId={ch.code}
              label={`${ch.code} ${ch.name}`}>
                {makeTree(ch.children)}
            </TreeItem>
          ) : (
            <TreeItem
              endIcon={<RemoveIcon style={{ fontSize: 15 }}/>}
              onLabelClick={(e)=> {props.setLoincId(ch.code);} }
              classes={{label:classes.treeItemLabel}}
              nodeId={ch.code}
              label={
                <Grid container>
                  <Grid item md={3}>
                    {ch.code}
                  </Grid>
                  <Grid item md={9}>
                    {ch.name}
                  </Grid>
                </Grid>
              }
            />
          )
        }
        </div>
      ))}
      </>
    }
    </>
  )

  return (
    <>
    { result.data && result.data.panel && result.data.panel.children &&
      <TreeView
        style={{fontSize: "small", margin:"0 0 1px 0"}}
        defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 15 }}/>}
        defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 15 }}/>}
        defaultExpanded={[ props.loincId]}
      >
        <TreeItem
          classes={{label:classes.treeItemLabel}}
          nodeId={result.data.panel.code}
          label={`${result.data.panel.code} ${result.data.panel.name}`}
        >
          {makeTree(result.data.panel.children) }
        </TreeItem>
      </TreeView>
    }
    </>
  );
}

