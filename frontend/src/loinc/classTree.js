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



export default function ClassTree(props) {

  const classes = useStyles();

  const [childNodes, setChildNodes] = useState(null);
  const [expanded, setExpanded] = useState([]);

  const handleChange = (event, nodes) => {
    const expandingNodes = nodes.filter(x => !expanded.includes(x));
    setExpanded(nodes);
    // root(nodeId===undefined)는 useEffect로 로드, handleChange에서 교체하지 않음
    if (props.nodeId === undefined) return;
    // 이 노드(props.nodeId) 자신이 확장될 때만 자식 로드
    if (!expandingNodes.includes(props.nodeId)) return;
    setChildNodes(null);
    setTimeout(() => {
      axios
        .get(`/children/LOINC/${props.nodeId}`)
        .then(result =>
          setChildNodes(
            result.data
            .sort((a,b) => a.prefName > b.prefName?1:-1)
            .map( (node, index) => (
              <ClassTree setLoincId={props.setLoincId} classes={{label:classes.treeItemLabel}} key={index} nodeId={node.code} label={renderLabel(node)} count={node.chdCnt}/>
            ))
          )
        );
    }, 50);
  };

  const renderLabel = item => (

      <Grid container wrap="nowrap" style={{padding:"0", margin:"0"}}>
        <Grid item style={{padding:"0 0 0 0px", margin:"0"}}>
        { (item.chdCnt === 0) ? (
            <Typography className={classes.label}>{item.prefName}</Typography>
          ) : (
            <Typography className={classes.label}>{item.prefName} ({item.chdCnt})</Typography>
          )
        }
        </Grid>
      </Grid>

  );

  useEffect(() => {
    if (props.nodeId === undefined) {
    setTimeout(() => {
      axios
        .get(`/children/LOINC/class`)
        .then(result =>
          setChildNodes(
            result.data
            .map((node,index) => (
              <ClassTree setLoincId={props.setLoincId} classes={{label:classes.treeItemLabel}} key={index} nodeId={node.code} label={renderLabel(node)} count={node.chdCnt}/>
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
      defaultExpanded={['Class']}
      onNodeToggle={handleChange}

    >
      { (props.nodeId === undefined)
        ? (
          <TreeItem
            onLabelClick={(e)=> {props.setLoincId('Class'); e.preventDefault();} }
            onIconClick={event => {event.preventDefault();}}
            classes={{label:classes.treeItemLabel}} nodeId={'Class'}
            label={"Class"}
          >
            {childNodes || [<div key="stub" />]}
          </TreeItem>
        ) : (
          <>
            { (props.count === 0)
              ? (
                <>
                { (props.nodeId === 'Class')
                ? (
                  <TreeItem
                    onLabelClick={(e)=> {props.setLoincId('Class'); e.preventDefault();} }
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
                { (props.nodeId === 'Class')
                ? (
                  <TreeItem onLabelClick={(e)=> {props.setLoincId('Class'); e.preventDefault();} } onIconClick={event => {event.preventDefault();}} classes={{label:classes.treeItemLabel}} nodeId={props.nodeId} label={props.label}>
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
