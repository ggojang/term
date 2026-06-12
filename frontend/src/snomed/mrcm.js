import React, {useState, useEffect, useRef} from 'react';
import { makeStyles } from '@material-ui/core/styles';
import axios from 'axios';
import useAsync from "../useAsync.js";
import Grid from "@material-ui/core/Grid";
import TreeView from '@material-ui/lab/TreeView';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import RemoveIcon from '@material-ui/icons/Remove';
import TreeItem from '@material-ui/lab/TreeItem';

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
  lineheight: {
    lineHeight: 2,
  },
}));

async function getMRCM(id) {
  const response = await axios.get(
    `/allow/attributes/SNOMEDCT/${id}`
  );
  return response.data;
}

export default function Mrcm(props) {

  const classes = useStyles();

  const [childNodes, setChildNodes] = useState(null);

  const [stateMRCM] = useAsync(() => getMRCM(props.id), [props.id]);
  const { loadingMRCM, data: mrcms, errorMRCM } = stateMRCM;

/*  const [mrcms, setMrcms] = useState([]);*/
  const [mrcm, setMrcm] = useState([]);
  const [mrcm2, setMrcm2] = useState([]);
  const [expanded, setExpanded] = useState([]);
  const [flag, setFlag] = useState([]);

  useEffect(() => {
    let fl = [];
    let ex = [];
    setMrcm([]);
    if (mrcms) {
      let members=[];
      if (props.mrcmFromMain[0] && props.valueFrom === 'BoxClick') {
        /*console.log("Box Click");*/
        setMrcm2([]);
        mrcms.map( (mr, index) => {
          fl.push(mr.id);
          let member = {};
          member["id"] = mr.id;
          member["name"] = mr.name;
          member["ranges"] = [];
          mr.ranges.map( (m, index3) => {
            member["ranges"].push({id:m.id, name:m.name});
          });
          members.push(member);
        });

        props.mrcmFromMain.map( (ma, index2) => {
          members.map( (mb, index) => {

            if ( mb.id === ma.type.conceptId) {
              ex.push(mb.id);
                const i = fl.indexOf(mb.id);
                members[i]["id"] = mb.id;
                members[i]["name"] = mb.name;
                members[i]["bold"] = "check";
                members[i]["ranges"] = [];
                members[i]["ranges"].push({id:ma.destination.conceptId, name:ma.destination.term + " (" + ma.destination.semanticTag + ")"});

              /*mrcm.push({ id:mr.id, name:mr.name, ranges: {id:ma.destination.conceptId, name:ma.destination.term + " (" + ma.destination.semanticTag + ")"}});
                */
            }
          });
        });
        /*
        console.log("mrcms : " + mrcms);
        console.log("props.mrcmFromMain : " + props.mrcmFromMain);
        console.log("mrcm : " + members);
        */
        setExpanded(ex);
        setMrcm2(members.sort((a,b) => a.name > b.name?1:-1));
      }
      return setMrcm([]);
    }
    /*console.log("props.mrcmFromMain:"+props.mrcmFromMain + " , " + "props.mrcmFromSearch:"+props.mrcmFromSearch);
    console.log(props.mrcmFromMain.map(m => console.log(m.type.term + ":" + m.destination.term)));*/
  },[mrcms, props.valueFrom, props.id, props.mrcmFromMain]);

  useEffect(() => {
    let members= [];
    let fl=[];
    if (mrcms) {
      if (props.valueFrom !== 'BoxClick') {
        /*console.log("Search or Tab Click");*/
        setMrcm([]);
        setMrcm2([]);
        setExpanded([]);
        mrcms.map( (mr, index) => {
          let member = {};
          fl.push(mr.id);
          member["id"] = mr.id;
          member["name"] = mr.name;
          member["ranges"] = [];
          mr.ranges.map( (m, index3) => {
            member["ranges"].push({id:m.id, name:m.name});
          });
          members.push(member);
        });
        setFlag(fl);
        setMrcm(members.sort((a,b) => a.name > b.name?1:-1));
      }
    }

  },[mrcms, props.valueFrom, props.id, props.mrcmFromSearch]);

  return (
    <>
    { (mrcm2.length !== 0)
      ? (
        <TreeView
          style={{fontSize: "small", margin:"0 0 1px 0"}}
          defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 20 }}/>}
          defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 20 }}/>}
        >
        <>
          { mrcm2.map((mr, index) => (
            <div key={index}>
              { (mr.bold === "check")
                ? (
                  <TreeItem TransitionComponent="ul" classes={{label:classes.treeItemLabel}} nodeId={mr.id} label={<b>{mr.name}</b>} >
                  { mr.ranges.map((m, index2) => (
                    <div key={index2}>
                        <TreeItem endIcon={<RemoveIcon style={{ fontSize: 15 }}/>} classes={{label:classes.treeItemLabel}} nodeId={m.id} label={<b>{m.name}</b>} />
                    </div>
                  ))}
                  </TreeItem>
                ) : (
                  <TreeItem classes={{label:classes.treeItemLabel}} nodeId={mr.id} label={mr.name} >
                  { mr.ranges.map((m, index2) => (
                    <div key={index2}>
                        <TreeItem endIcon={<RemoveIcon style={{ fontSize: 15 }}/>} classes={{label:classes.treeItemLabel}} nodeId={m.id} label={m.name} />
                    </div>
                  ))}
                  </TreeItem>
                )
              }
            </div>
          ))}
        </>
        </TreeView>
      ):(
        <TreeView
          style={{fontSize: "small", margin:"0 0 1px 0"}}
          defaultCollapseIcon={<ExpandMoreIcon style={{ fontSize: 20 }}/>}
          defaultExpandIcon={<ChevronRightIcon style={{ fontSize: 20 }}/>}

        >
        <>
          { mrcm.map((mr, index) => (
            <div key={index}>
              <TreeItem classes={{label:classes.treeItemLabel}} nodeId={mr.id} label={mr.name} >
              { mr.ranges.map((m, index2) => (
                <div key={index2}>
                    <TreeItem endIcon={<RemoveIcon style={{ fontSize: 15 }}/>} classes={{label:classes.treeItemLabel}} nodeId={m.id} label={m.name} />
                </div>
              ))}
              </TreeItem>
            </div>
          ))}
        </>
        </TreeView>
      )
    }
    </>

  );
}
