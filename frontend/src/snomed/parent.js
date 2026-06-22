import React, {useState, useEffect, useContext} from 'react';
import { AppContext } from './layout.js';
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
import { Link } from "react-router-dom";

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
    borderRadius: '10px',
    backgroundImage: 'linear-gradient(to bottom,#f7edb5 0,#f5e79e 100%)',
    backgroundColor: '#f5e79e',
  },
  label: {
    fontSize: '0.9em',
  },
  lineheight: {
    lineHeight: 2,
  },
}));

export default function Parent(props) {

  const classes = useStyles();

  const [childNodes, setChildNodes] = useState(null);
  const [expanded, setExpanded] = useState([]);

  /*const firstId = useContext(AppContext);  최초 concept ID 유지를 위해 설정 */
  /*console.log("firstId : " + firstId);*/

  const handleChange = (event, nodes) => {
    /*console.log("handleChange (nodes) => " + nodes);
    setChildNodes(null);*/
    const expandingNodes = nodes.filter(x => !expanded.includes(x));
    console.log("handleChange (expandingNodes)=> " + expandingNodes);
    setExpanded(nodes);
    if (expandingNodes[0]) {
      const childId = expandingNodes[0];
      setTimeout(() => {
        axios
          .get(`/parents/SNOMEDCT/${childId}`)
          .then(result =>
            setChildNodes(
              result.data
              .sort((a,b) => a.term > b.term?1:-1)
              .map( node => (
                <Parent setFromId={props.setFromId} classes={{label:classes.treeItemLabel}} key={node.conceptId} nodeId={node.conceptId} label={renderLabel(node)} count={node.descendantCount}/>
              ))
            )
          );
      }, 50);
    }
    console.log(childNodes);
  };

  const renderLabel = item => (

      <Grid container  wrap="nowrap" style={{padding:"0", margin:"0"}}>
        <Grid item style={{padding:"0 0 0 0 ", margin:"0"}}>
        {item.definitionStatus.id === '900000000000073002' ? (
            <Typography className={clsx(classes.alertWarning, classes.badge)}>≡</Typography>
          ):(
            <Typography className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</Typography>
          )
        }
        </Grid>
        <Grid item style={{padding:"0 0 0 8px", margin:"0"}}>
        { (item.descendantCount === 0) ? (
            <Typography className={classes.label}>{item.term}</Typography>
          ) : (
            <Typography className={classes.label}>{item.term} ({item.descendantCount})</Typography>
          )
        }
        </Grid>
      </Grid>

  );

  useEffect(() => {

    setChildNodes(null);
    if (props.firstId) {
      setTimeout(() => {
        axios
          .get(`/parents/SNOMEDCT/${props.firstId}`)
          .then(result =>
            setChildNodes(
              result.data
              .sort((a,b) => a.term > b.term?1:-1)
              .map(node => (
                <Parent setFromId={props.setFromId} classes={{label:classes.treeItemLabel}} key={node.conceptId} nodeId={node.conceptId} label={renderLabel(node)} count={node.descendantCount}/>
              ))
            )
          );
      }, 50);
    }
  },[props.firstId]); /* left Hierarchy에서 concept을 선택하면 refresh를 위해 pros.nodeId 추 */


  /*console.log("props.nodeId => " + props.nodeId + " , " + "firstId => " + firstId);
  console.log('childNodes : ' + childNodes);
  */
  return (

    <TreeView
      style={{fontSize: "small", margin:"0 0 1px 0"}}
      defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 20 }}/>}
      defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 20 }}/>}
      onNodeToggle={handleChange}
    >
      { props.firstId /* firstId rootID임, 이 조건이 없으면 무한 loop error*/
      ? (
        <>
        {childNodes}
        </>
      ) : (
        <>
      { (props.count === 0)
        ? (
          <TreeItem
            endIcon={<RemoveIcon style={{ fontSize: 15 }}/>}
            onLabelClick={()=>props.setFromId(props.nodeId)}
            classes={{label:classes.treeItemLabel}}
            nodeId={props.nodeId}
            label={props.label} />
        ) : (
          <TreeItem onLabelClick={()=>props.setFromId(props.nodeId)} classes={{label:classes.treeItemLabel}} nodeId={props.nodeId} label={props.label}>
            {childNodes || [<div key="stub" />]}
          </TreeItem>
        )
      }
      </>
      )
      }
    </TreeView>

  );
}
