import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import TreeView from '@material-ui/lab/TreeView';
import TreeItem from '@material-ui/lab/TreeItem';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';

const ROOT_ID = '900000000000455006';

const useStyles = makeStyles(() => ({
  treeView: {
    fontSize: 'small',
    margin: '0 0 1px 0',
    userSelect: 'none',
  },
  labelNormal: {
    fontSize: '0.88em',
    fontWeight: 'normal',
    lineHeight: 1.8,
    color: '#000',
  },
  labelBold: {
    fontSize: '0.88em',
    fontWeight: 'bold',
    lineHeight: 1.8,
    color: '#000',
  },
}));

export default function Left({ setRefset }) {
  const classes = useStyles();

  const [tree, setTree]         = useState({});   // id → { conceptId, term, descendantCount }
  const [children, setChildren] = useState({});   // parentId → [childId, ...]
  const [parents, setParents]   = useState({});   // childId → parentId
  const [boldSet, setBoldSet]   = useState(new Set());
  const [expanded, setExpanded] = useState([ROOT_ID]);

  // 지정 노드부터 루트까지 bold 전파
  const propagateBold = useCallback((startId, parentsMap) => {
    setBoldSet(prev => {
      const next = new Set(prev);
      let cur = startId;
      while (cur) {
        if (next.has(cur)) break; // 이미 전파됨 → 중단
        next.add(cur);
        cur = parentsMap[cur];
      }
      return next;
    });
  }, []);

  // 자식 노드 로드
  const fetchChildren = useCallback((parentId, parentsMap) => {
    axios.get(`http://api.infoclinic.co/children/SNOMEDCT/${parentId}`)
      .then(res => {
        const nodes = res.data.sort((a, b) => a.term > b.term ? 1 : -1);

        // tree 맵 갱신
        setTree(prev => {
          const next = { ...prev };
          nodes.forEach(n => { next[n.conceptId] = n; });
          return next;
        });

        // children 맵 갱신
        setChildren(prev => ({
          ...prev,
          [parentId]: nodes.map(n => n.conceptId),
        }));

        // parents 맵 갱신 & leaf이면 즉시 bold 전파
        const newParents = { ...parentsMap };
        nodes.forEach(n => { newParents[n.conceptId] = parentId; });
        setParents(newParents);

        nodes.forEach(n => {
          if (n.descendantCount === 0) {
            // leaf 노드 발견 → leaf + 모든 조상에 bold 전파
            propagateBold(n.conceptId, newParents);
          }
        });
      });
  }, [propagateBold]);

  // 최초 루트 자식 로드
  useEffect(() => {
    fetchChildren(ROOT_ID, {});
  }, []); // eslint-disable-line

  function handleToggle(e, nodeIds) {
    const opening = nodeIds.filter(id => !expanded.includes(id));
    setExpanded(nodeIds);
    opening.forEach(id => {
      if (!children[id]) fetchChildren(id, parents);
    });
  }

  function labelClass(id) {
    return boldSet.has(id) ? classes.labelBold : classes.labelNormal;
  }

  function renderNode(id) {
    const node = tree[id];
    if (!node) return null;
    const isLeaf = node.descendantCount === 0;
    const label = (
      <span
        className={labelClass(id)}
        onClick={() => setRefset({ name: node.term, id: node.conceptId, desc: isLeaf ? 0 : 1 })}
      >
        {node.term}{!isLeaf && ` (${node.descendantCount})`}
      </span>
    );

    if (isLeaf) {
      return <TreeItem key={id} nodeId={id} label={label} />;
    }
    const nodeChildren = children[id];
    return (
      <TreeItem key={id} nodeId={id} label={label}>
        {nodeChildren
          ? nodeChildren.map(cid => renderNode(cid))
          : <TreeItem nodeId={`${id}-loading`} label="..." />}
      </TreeItem>
    );
  }

  const rootChildren = children[ROOT_ID];

  return (
    <TreeView
      className={classes.treeView}
      defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 20 }} />}
      defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 20 }} />}
      expanded={expanded}
      onNodeToggle={handleToggle}
    >
      <TreeItem
        nodeId={ROOT_ID}
        label={
          <span
            className={labelClass(ROOT_ID)}
            onClick={() => setRefset({ name: 'Reference Set', id: ROOT_ID, desc: 1 })}
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
