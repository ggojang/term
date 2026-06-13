import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import TreeView from '@material-ui/lab/TreeView';
import TreeItem from '@material-ui/lab/TreeItem';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';

const useStyles = makeStyles(() => ({
  treeView: {
    fontSize: 'small',
    margin: '0 0 1px 0',
    userSelect: 'none',
  },
  label: {
    fontSize: '0.88em',
    fontWeight: 'bold',
    lineHeight: 1.8,
    cursor: 'pointer',
  },
  labelCategory: {
    fontSize: '0.88em',
    fontWeight: 'bold',
    lineHeight: 1.8,
    color: '#37474f',
  },
  labelLeaf: {
    fontSize: '0.88em',
    fontWeight: 'bold',
    lineHeight: 1.8,
    color: '#1a237e',
    cursor: 'pointer',
  },
}));

export default function Left({ setRefset }) {
  const classes = useStyles();
  const [tree, setTree] = useState([]);           // flat map: id → node data
  const [children, setChildren] = useState({});   // id → [childId, ...]
  const [expanded, setExpanded] = useState(['900000000000455006']);
  const [loading, setLoading] = useState({});

  // 최초: 루트 자식 로드
  useEffect(() => {
    fetchChildren('900000000000455006');
  }, []);

  function fetchChildren(parentId) {
    if (loading[parentId]) return;
    setLoading(prev => ({ ...prev, [parentId]: true }));
    axios.get(`http://api.infoclinic.co/children/SNOMEDCT/${parentId}`)
      .then(res => {
        const nodes = res.data.sort((a, b) => a.term > b.term ? 1 : -1);
        setTree(prev => {
          const next = { ...prev };
          nodes.forEach(n => { next[n.conceptId] = n; });
          return next;
        });
        setChildren(prev => ({
          ...prev,
          [parentId]: nodes.map(n => n.conceptId),
        }));
      });
  }

  function handleToggle(e, nodeIds) {
    // 새로 펼쳐지는 노드 찾아서 자식 로드
    const opening = nodeIds.filter(id => !expanded.includes(id));
    opening.forEach(id => {
      if (!children[id]) fetchChildren(id);
    });
    setExpanded(nodeIds);
  }

  function renderNode(id) {
    const node = tree[id];
    if (!node) return null;
    const isLeaf = node.descendantCount === 0;
    const label = (
      <span
        className={isLeaf ? classes.labelLeaf : classes.labelCategory}
        onClick={() => setRefset({ name: node.term, id: node.conceptId, desc: isLeaf ? 0 : 1 })}
      >
        {node.term}{!isLeaf && ` (${node.descendantCount})`}
      </span>
    );

    const nodeChildren = children[id];
    if (isLeaf) {
      return (
        <TreeItem
          key={id}
          nodeId={id}
          label={label}
          style={{ fontWeight: 'bold' }}
        />
      );
    }
    return (
      <TreeItem key={id} nodeId={id} label={label} style={{ fontWeight: 'bold' }}>
        {nodeChildren
          ? nodeChildren.map(cid => renderNode(cid))
          : <TreeItem nodeId={`${id}-loading`} label="..." />}
      </TreeItem>
    );
  }

  const rootChildren = children['900000000000455006'];

  return (
    <TreeView
      className={classes.treeView}
      defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 20 }} />}
      defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 20 }} />}
      expanded={expanded}
      onNodeToggle={handleToggle}
    >
      <TreeItem
        nodeId="900000000000455006"
        label={
          <span
            className={classes.labelCategory}
            onClick={() => setRefset({ name: 'Reference Set', id: '900000000000455006', desc: 1 })}
          >
            Reference Set (113)
          </span>
        }
      >
        {rootChildren
          ? rootChildren.map(id => renderNode(id))
          : <TreeItem nodeId="loading" label="..." />}
      </TreeItem>
    </TreeView>
  );
}
