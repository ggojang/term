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
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import { Link } from "react-router-dom";

import AddCircleOutlineIcon from '@material-ui/icons/AddCircleOutline';
import RemoveCircleOutlineIcon from '@material-ui/icons/RemoveCircleOutline';

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
  treeItemLabelBold: {
    fontSize: "0.9em",
    fontWeight: 'bold',
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
  gridBorder: {
    borderRight: "dotted 1px lightGray",
  },
  label: {
    fontSize: '0.9em',
  },
  lineheight: {
    lineHeight: 2,
  },
}));



export default function Left(props) {

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
            .get(`http://api.infoclinic.co/children/SNOMEDCT/${childId}`)
            .then(result =>
              setChildNodes(
                result.data
                .sort((a,b) => a.term > b.term?1:-1)
                .map( (node, index) => (
                  <Left setRefset={props.setRefset} classes={{label:classes.treeItemLabel}} key={index} nodeId={node.conceptId} nodeName={node.term} label={renderLabel(node)} count={node.descendantCount} date={node.dateCreated} />
                ))
              )
            );
        }, 50);
      }
    }
  };

  const handleAddButtonChange = (event) => {
    console.log("Add");
  };

  const handleRemoveButtonChange = (event) => {
    console.log("Remove");
  };

  const renderLabel = item => (
    <Grid
      container
      direction="row"
      justify="space-between"
      alignItems="center"
    >
      <Grid item md={12}>
        <Grid container wrap="nowrap" style={{padding:"0", margin:"0"}}>
          <Grid item style={{padding:"0 0 0 0", margin:"0"}}>
          { (item.descendantCount === 0)
            ? (
              <Typography className={classes.label} style={{ fontWeight: 'bold' }}
                onClick={(e)=> {props.setRefset({name:item.term, id:item.conceptId, desc:0});} }
              >{item.term}</Typography>
            ) : (
              <Typography className={classes.label}
                onClick={(e)=> {props.setRefset({name:item.term, id:item.conceptId, desc:1});} }
              >{item.term} ({item.descendantCount})</Typography>
            )
          }
          </Grid>
        </Grid>
      </Grid>
    </Grid>
  );

  useEffect(() => {
    if (props.nodeId === undefined) {
    setTimeout(() => {
      axios
        .get(`http://api.infoclinic.co/children/SNOMEDCT/900000000000455006`)
        .then(result =>
          setChildNodes(
            result.data
            .sort((a,b) => a.term > b.term?1:-1)
            .map((node,index) => (
              <Left setRefset={props.setRefset} classes={{label:classes.treeItemLabel}} key={index} nodeId={node.conceptId} nodeName={node.term} label={renderLabel(node)} count={node.descendantCount} date={node.dateCreated}/>
            ))
          )
        );
    }, 50);
  }
  }, [props.nodeId]);

  return (
    <>
    <Grid container>
      <Grid item md={12}>
        <TreeView
          style={{fontSize: "small", margin:"0 0 1px 0"}}
          defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 20 }}/>}
          defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 20 }}/>}
          defaultExpanded={['900000000000455006']}
          onNodeToggle={handleChange}

        >
          { (props.nodeId === undefined)
            ? (
              <TreeItem
                onLabelClick={(e)=> {props.setRefset({name:'Reference set', id:'900000000000455006', desc:1}); e.preventDefault();} }
                onIconClick={event => {event.preventDefault();}}
                classes={{label:classes.treeItemLabel}} nodeId={'900000000000455006'}
                label={
                  <span>&nbsp;Reference Set (113)</span>
                }
              >
                {childNodes || [<div key="stub" />]}
              </TreeItem>
            ) : (
              <>
                { (props.count === 0)
                  ? (
                    <>
                    { (props.nodeId === '900000000000455006')
                    ? (
                      <TreeItem
                        onLabelClick={(e)=> {props.setRefset({name:'Reference set', id:'900000000000455006', desc:1}); e.preventDefault();} }
                        onIconClick={event => {event.preventDefault();}}
                        classes={{label:classes.treeItemLabel}}
                        nodeId={props.nodeId}
                        label={props.label} />
                    ) : (
                      <TreeItem
                        endIcon={<RemoveIcon style={{ fontSize: 15 }}/>}
                        onLabelClick={(e)=> {e.preventDefault();} }
                        classes={{label:classes.treeItemLabelBold}}
                        nodeId={props.nodeId}
                        label={props.label} />
                    )
                    }
                    </>
                  )
                  : (
                    <>
                    { (props.nodeId === '900000000000455006')
                    ? (
                      <TreeItem onLabelClick={(e)=> {props.setRefset({name:'Reference set', id:'900000000000455006', desc:1}); e.preventDefault();} } onIconClick={event => {event.preventDefault();}} classes={{label:classes.treeItemLabel}} nodeId={props.nodeId} label={props.label}>
                        {childNodes || [<div key="stub" />]}
                      </TreeItem>
                    ) : (
                      <TreeItem onLabelClick={(e)=> {e.preventDefault();} } classes={{label:classes.treeItemLabelBold}} nodeId={props.nodeId} label={props.label}>
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
      </Grid>
    </Grid>
    </>
  );
}

