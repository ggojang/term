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
    backgroundColor: '#999',
    borderRadius: '10px',
  },
  label: {
    fontSize: '0.9em',
  },
}));

const data = {
  id: 'root',
  name: 'SNOMED CT Search and Data Entry Guide',
  children: [
    {
      id: '4',
      name: '4. Optimizing Searches',
      children: [
        {
          id: '41',
          name: '4.1 Search by Text',
        },
        {
          id: '42',
          name: '4.2 Search by Identifiers',
        },
        {
          id: '43',
          name: '4.3. Extended Searches',
          children: [
            {
              id: '431',
              name: '4.3.1. Extend Search by Word Equivalents',
            },
            {
              id: '4311',
              name: '4.3.1.* Extend Search by Synonym',
            },
            {
              id: '432',
              name: '4.3.2. Extend Search by Postcoordinated Searching',
            },
          ],
        },
        {
          id: '44',
          name: '4.4. Constrained Searches (by excluding "stop word")',
          children: [
            {
              id: '441',
              name: '4.4.1. Constrain Search by Status',
            },
            {
              id: '442',
              name: '4.4.2. Constrain Search by Supertype Ancestor',
            },
            {
              id: '4421',
              name: '4.4.2*. Constrain Search by Semantic Tag',
            },
            {
              id: '443',
              name: '4.4.3. Constrain Search by Reference Sets',
            },
            {
              id: '444',
              name: '4.4.4. Constrain Search by Language or Dialect',
            },
          ],
        },
        {
          id: '45',
          name: '4.5. Improve Search Speed',
          children: [
            {
              id: '451',
              name: '4.5.1. Enable Real Time Searching',
            },
            {
              id: '452',
              name: '4.5.2. Show an indication of Estimated Number of Matches Before Startin a Search',
            },
            {
              id: '453',
              name: '4.5.3. Enable Background Encoding',
            },
            {
              id: '454',
              name: '4.5.4. Enable Automatic and Semi-Automatic Encoding',
            },
            {
              id: '455',
              name: '4.5.5. Optimizing Indexing',
            },
          ],
        },
        {
          id: '5',
          name: '5. Optimizing Display of Search Results',
          children: [
            {
              id: '51',
              name: '5.1. Order Search Results Rationally',
              children: [
                {
                  id: '511',
                  name: '5.1.1. Order Shortest Matching Terms First',
                },
                {
                  id: '512',
                  name: '5.1.2. Order Preferred Term Matches Before Synonyms',
                },
                {
                  id: '513',
                  name: '5.1.3. Order User Preferred Language Matches First in Multilingual Environments',
                },
                {
                  id: '514',
                  name: '5.1.4. Order According to Priority in Any Active Reference Sets',
                },
                {
                  id: '515',
                  name: '5.1.5. Display the the Most Frequently Used Descriptions Listed First',
                },
                {
                  id: '516',
                  name: '5.1.6. Alphabetical Ordering',
                },
              ],
            },
            {
              id: '52',
              name: '5.2. Distinguish Identical Terms of Different Concepts',
            },
            {
              id: '53',
              name: '5.3. Avoid Multiple Hits on the Same Concept',
            },
            {
              id: '54',
              name: '5.4. Rationalize Search Results by Subsumption Checking',
            },
            {
              id: '55',
              name: '5.5. Display Navigation Results Effectively',
            },
            {
              id: '56',
              name: '5.6. Use Mnemonics and Personal Favorites for Data Entry',
            },
          ],
        },
      ],
    },
  ],
};

export default function TableOfContents(props) {

  const classes = useStyles();

  const handleChange = (event) => {

  };

  const renderTree = (nodes) => (
    <TreeItem
      key={nodes.id}
      onLabelClick={()=>props.setFromEHRLeft(nodes.id)}
      classes={{label:classes.treeItemLabel}}
      nodeId={nodes.id}
      label={
        `${nodes.id}` === '41'
        || `${nodes.id}` === '42'
        || `${nodes.id}` === '431'
        || `${nodes.id}` === '4311'
        || `${nodes.id}` === '432'
        || `${nodes.id}` === '441'
        || `${nodes.id}` === '442'
        || `${nodes.id}` === '4421'
        || `${nodes.id}` === '443'
        || `${nodes.id}` === '512'
        ? <strong>{nodes.name}</strong>
        : nodes.name}>
        {Array.isArray(nodes.children) ? nodes.children.map((node) => renderTree(node)) : null}
    </TreeItem>
  );

  return (
    <TreeView
      style={{fontSize: "small", margin:"0 0 1px 0"}}
      defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 20 }}/>}
      defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 20 }}/>}
      onNodeToggle={handleChange}
      defaultExpanded={['root', '4', '43', '44', '45', '5', '51']}
    >
      {renderTree(data)}
    </TreeView>

  );
}
