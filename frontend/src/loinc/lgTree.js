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
  alertWarning: {
    backgroundImage: 'linear-gradient(to bottom,#f7edb5 0,#f5e79e 100%)',
    backgroundRepeat: 'repeat-x',
    color: '#8a6d3b',
    backgroundColor: '#fcf8e3',
    borderColor: '#f5e79e',
    /*'&::after' : {
      content: '"F"',
    }*/
  },
  badge: {
    display: 'inline-block',
    minWidth: '10px',
    padding: '3px 7px',
    fontSize: '12px',
    fontWeight: 'bold',
    lineHeight: '1',
    textAlign: 'center',
    whiteSpace: 'nowrap',
    verticalAlign: 'baseLine',
    backgroundColor: '#999',
    borderRadius: '10px',
  },
  label: {
    fontSize: '0.9em',
  },
}));



export default function LGTree(props) {

  const classes = useStyles();

  const [childNodes, setChildNodes] = useState(null);
  const [expanded, setExpanded] = useState([]);

  const handleChange = (event, nodes) => {
    if (nodes.length !== 0) {
      /*console.log("handleChange (nodes) => " + nodes + ", length : " + nodes.length);*/
      setChildNodes(null);
      const expandingNodes = nodes.filter(x => !expanded.includes(x));
      setExpanded(nodes);
      if (expandingNodes[0]) {
        const childId = expandingNodes[0];
        setTimeout(() => {
          axios
            .get(`/children/LOINC/${childId}`)
            .then(result =>
              setChildNodes(
                result.data
                .sort((a,b) => a.name > b.name?1:-1)
                .map( (node, index) => (
                  <LGTree setLoincId={props.setLoincId} classes={{label:classes.treeItemLabel}} key={index} nodeId={node.code} label={renderLabel(node)} count={node.desCnt}/>
                ))
              )
            );
        }, 50);
      }
    }
  };

  const renderLabel = item => (

      <Grid container wrap="nowrap" style={{padding:"0", margin:"0"}}>
        <Grid item style={{padding:"0 0 0 0px", margin:"0"}}>
        { (item.desCnt === 0) ? (
            <Typography className={classes.label}>{item.prefName}</Typography>
          ) : (
            <Typography className={classes.label}>{item.prefName} ({item.desCnt})</Typography>
          )
        }
        </Grid>
      </Grid>

  );

  useEffect(() => {
    if (props.nodeId === undefined) {
    setTimeout(() => {
      axios
        .get(`/children/LOINC/group`)
        .then(result =>
          setChildNodes(
            result.data
            .sort((a,b) => a.name > b.name?1:-1)
            .map((node,index) => (
              <LGTree setLoincId={props.setLoincId} classes={{label:classes.treeItemLabel}} key={index} nodeId={node.code} label={renderLabel(node)} count={node.desCnt}/>
            ))
          )
        );
    }, 50);
  }
  }, [props.nodeId]);

  return (

    <TreeView
      style={{fontSize: "small", margin:"0 0 1px 0"}}
      defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 15 }}/>}
      defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 15 }}/>}
      defaultExpanded={['Group']}
      onNodeToggle={handleChange}

    >
      { (props.nodeId === undefined)
        ? (
          <TreeItem
            onLabelClick={(e)=> {props.setLoincId('Group'); e.preventDefault();} }
            onIconClick={event => {event.preventDefault();}}
            classes={{label:classes.treeItemLabel}} nodeId={'Group'}
            label={"Group"}
          >
            {childNodes || [<div key="stub" />]}
          </TreeItem>
        ) : (
          <>
            { (props.count === 0)
              ? (
                <>
                { (props.nodeId === 'Group')
                ? (
                  <TreeItem
                    onLabelClick={(e)=> {props.setLoincId('Group'); e.preventDefault();} }
                    onIconClick={event => {event.preventDefault();}}
                    classes={{label:classes.treeItemLabel}}
                    nodeId={props.nodeId}
                    label={props.label} />
                ) : (
                  <TreeItem
                    endIcon={<RemoveIcon style={{ fontSize: 15 }}/>}
                    onLabelClick={(e)=> {props.setLoincId(props.nodeId); e.preventDefault();} }
                    classes={{label:classes.treeItemLabel}}
                    nodeId={props.nodeId}
                    label={props.label} />
                )
                }
                </>
              )
              : (
                <>
                { (props.nodeId === 'Group')
                ? (
                  <TreeItem onLabelClick={(e)=> {props.setLoincId('Group'); e.preventDefault();} } onIconClick={event => {event.preventDefault();}} classes={{label:classes.treeItemLabel}} nodeId={props.nodeId} label={props.label}>
                    {childNodes || [<div key="stub" />]}
                  </TreeItem>
                ) : (
                  <TreeItem onLabelClick={(e)=> {props.setLoincId(props.nodeId); e.preventDefault();} } classes={{label:classes.treeItemLabel}} nodeId={props.nodeId} label={props.label}>
                    {childNodes || [<div key="stub" />]}
                  </TreeItem>
                )
                }
                </>
              )
            }
          </>
        )
      }
    </TreeView>

  );
}
