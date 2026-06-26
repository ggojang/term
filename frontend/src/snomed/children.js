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
}));

export default function Children(props) {

  const classes = useStyles();

  const [childNodes, setChildNodes] = useState(null);
  const [expanded, setExpanded] = useState([]);

  /*const firstId = useContext(AppContext);  최초 concept ID 유지를 위해 설정

  console.log("Children (props.firstId) : " + props.firstId);
  console.log("Children (props.nodeId) : " + props.nodeId);
  */

  const handleChange = (event, nodes) => {
    const expandingNodes = nodes.filter(x => !expanded.includes(x));
    setExpanded(nodes);
    // firstId(root)는 useEffect로 로드, handleChange에서 교체하지 않음
    if (props.firstId) return;
    // 이 노드(props.nodeId) 자신이 확장될 때만 자식 로드
    if (!expandingNodes.includes(props.nodeId)) return;
    setChildNodes(null);
    setTimeout(() => {
      const vq = props.version ? `?version=${props.version}` : '';
      axios
        .get(`/children/SNOMEDCT/${props.nodeId}${vq}`)
        .then(result =>
          setChildNodes(
            result.data
            .sort((a,b) => a.term > b.term?1:-1)
            .map( (node, index) => (
              <Children setFromId={props.setFromId} version={props.version} classes={{label:classes.treeItemLabel}} key={index} nodeId={node.conceptId} label={renderLabel(node)} count={node.descendantCount}/>
            ))
          )
        );
    }, 50);
  };

  const renderLabel = item => (

      <Grid container wrap="nowrap" style={{padding:"0", margin:"0"}}>
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

    if (props.firstId) {
      setChildNodes(null);
        setTimeout(() => {
          const vq = props.version ? `?version=${props.version}` : '';
          axios
            .get(`/children/SNOMEDCT/${props.firstId}${vq}`)
            .then(result =>
              setChildNodes(
                result.data
                .sort((a,b) => a.term > b.term?1:-1)
                .map( (node,index) => (
                  <Children setFromId={props.setFromId} version={props.version} classes={{label:classes.treeItemLabel}} key={index} nodeId={node.conceptId} label={renderLabel(node)} count={node.descendantCount}/>
                ))
              )
            );
        }, 50);
    }
  },[props.firstId, props.version]); /* left Hierarchy에서 concept을 선택하면 refresh를 위해 pros.nodeId 추 */


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
      { (props.firstId) /* firstId rootID임, 이 조건이 없으면 무한 loop error*/
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
