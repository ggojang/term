import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import TreeView from '@material-ui/lab/TreeView';
import TreeItem from '@material-ui/lab/TreeItem';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';

const ROOT_ID = '900000000000455006';

const useStyles = makeStyles(() => ({
  treeView: { fontSize: 'small', margin: '0 0 1px 0', userSelect: 'none' },
  hasMember: { fontSize: '0.9em', fontWeight: 'bold',   color: '#000', cursor: 'pointer' },
  normal:    { fontSize: '0.9em', fontWeight: 'normal', color: '#000', cursor: 'pointer' },
}));

function fetchLevel(parentId) {
  return axios
    .get(`/children/SNOMEDCT/${parentId}`)
    .then(res => ({ parentId, nodes: res.data.sort((a, b) => (a.term > b.term ? 1 : -1)) }));
}

export default function Left({ setRefset }) {
  const classes = useStyles();

  const dataRef = useRef({ tree: {}, children: {}, parents: {}, memberSet: new Set() });

  const [, forceUpdate] = useState(0);
  const [expanded, setExpanded] = useState([ROOT_ID]);

  useEffect(() => {
    // member가 존재하는 refset ID 목록을 먼저 가져옴
    axios.get('/refsets/SNOMEDCT?release=itn&hasmbrs=true').then(res => {
      const d = dataRef.current;
      const ids = Array.isArray(res.data) ? res.data : [];
      ids.forEach(id => d.memberSet.add(String(id)));
      forceUpdate(n => n + 1);
    });
  }, []); // eslint-disable-line

  useEffect(() => {
    const d = dataRef.current;
    const visited = new Set([ROOT_ID]);
    let queue = [ROOT_ID];

    async function runBFS() {
      while (queue.length > 0) {
        const batch = queue;
        queue = [];

        const results = await Promise.all(batch.map(pid => fetchLevel(pid)));

        results.forEach(({ parentId, nodes }) => {
          d.children[parentId] = nodes.map(n => n.conceptId);
          nodes.forEach(n => {
            d.tree[n.conceptId] = n;
            d.parents[n.conceptId] = parentId;
            if (n.descendantCount > 0 && !visited.has(n.conceptId)) {
              visited.add(n.conceptId);
              queue.push(n.conceptId);
            }
          });
        });

        forceUpdate(n => n + 1);
      }
    }

    runBFS();
  }, []); // eslint-disable-line

  function handleToggle(_, nodeIds) {
    setExpanded(nodeIds);
  }

  function renderNode(id) {
    const d = dataRef.current;
    const node = d.tree[id];
    if (!node) return null;
    const isLeaf = node.descendantCount === 0;
    const hasMember = d.memberSet.has(id);

    const label = (
      <span
        className={hasMember ? classes.hasMember : classes.normal}
        onClick={() => setRefset({ name: node.term, id: node.conceptId, desc: isLeaf ? 0 : 1 })}
      >
        {node.term}{!isLeaf && ` (${node.descendantCount})`}
      </span>
    );

    const nodeChildren = d.children[id];
    if (isLeaf || !nodeChildren || nodeChildren.length === 0) {
      return <TreeItem key={id} nodeId={id} label={label} />;
    }
    return (
      <TreeItem key={id} nodeId={id} label={label}>
        {nodeChildren.map(cid => renderNode(cid))}
      </TreeItem>
    );
  }

  const d = dataRef.current;
  const rootChildren = d.children[ROOT_ID];

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
            className={classes.normal}
            onClick={() => setRefset({ name: 'Reference Set', id: ROOT_ID, desc: 1 })}
          >
            Reference Set (113)
          </span>
        }
      >
        {rootChildren
          ? rootChildren.map(id => renderNode(id))
          : <TreeItem nodeId="loading" label="로딩 중..." />}
      </TreeItem>
    </TreeView>
  );
}
