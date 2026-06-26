import React, { useState, useEffect } from 'react';
import axios from 'axios';
import useScrollTrigger from './useScrollTrigger.js';
import useAsync from "../useAsync.js";
import { withStyles, makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import Grid from "@material-ui/core/Grid";
import Box from "@material-ui/core/Box";
import Divider from "@material-ui/core/Divider";
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import {Star, StarBorder} from '@material-ui/icons';
import Container from '@material-ui/core/Container';
import { BrowserRouter as Link} from "react-router-dom";

const BOOKMARK_KEY = 'stom-bookmarks';

function useToast() {
  const [msg, setMsg] = useState('');
  const show = (m) => { setMsg(m); setTimeout(() => setMsg(''), 2200); };
  return [msg, show];
}

function getSemanticTag(fsn) {
  if (!fsn) return null;
  const m = fsn.match(/\(([^)]+)\)$/);
  return m ? m[1] : null;
}
function getSemanticTagClass(tag) {
  if (!tag) return 'badge-st-default';
  const t = tag.toLowerCase();
  if (t.includes('disorder')) return 'badge-st-disorder';
  if (t.includes('procedure')) return 'badge-st-procedure';
  if (t.includes('finding')) return 'badge-st-finding';
  if (t.includes('observable')) return 'badge-st-observable';
  if (t.includes('substance')) return 'badge-st-substance';
  if (t.includes('organism')) return 'badge-st-organism';
  if (t.includes('body')) return 'badge-st-body';
  if (t.includes('qualifier')) return 'badge-st-qualifier';
  if (t.includes('situation')) return 'badge-st-situation';
  if (t.includes('event')) return 'badge-st-event';
  return 'badge-st-default';
}

const StyledTableCell = withStyles((theme) => ({
  head: {
    backgroundColor: "#f9f9f9", //"#e3f2fd",
    color: theme.palette.common.black,
  },
  body: {
    fontSize: 11,
  },
}))(TableCell);

const useStyles = makeStyles((theme) => ({
  container: {
    '-ms-overflow-style': 'none', /* IE and Edge */
    scrollbarWidth: 'none', /* Firefox */
    '&::-webkit-scrollbar': {
        display: 'none', /* Chrome, Safari, Opera*/
    },
  },
  lineheight: {
    lineHeight: 2,
  },
  alertWarning: {
    backgroundImage: 'linear-gradient(to bottom,#f7edb5 0,#f5e79e 100%)',
    backgroundRepeat: 'repeat-x',
    color: '#8a6d3b',
    backgroundColor: '#fcf8e3',
    borderColor: '#f5e79e',

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
  },
  label: {
    fontSize: '0.8em',
  },
  gridcontainer: {
    height: '100vh',
  },
  divider: {
    borderBottom: "solid 1px #e4e7ec",
    marginBottom: 2,
  },
  gridBorder: {
    borderRight: "dotted 1px lightGray",
    borderLeft: "dotted 1px lightGray",
  },
  fsn: {
    fontSize:'1rem', fontWeight:'800', color: '#d9534f',
  },
  preferred: {
    fontSize:'1rem', fontWeight:'800', color: '#468847'
  },
  synonym: {
    fontSize:'1rem', fontWeight:'800', color: '#3a87ad'
  },
  acceptable: {
    fontSize:'1rem', fontWeight:'800', color: '#FFD700'
  },
  tooltip:{
    color: '#fffff',
  },
  flagIcon : {
    position: 'relative',
    display: 'inline-block',
    backgroundRepeat: 'no-repeat',
    backgroundPosition: '50%',
  },
  link: {
    textDecoration: "none",
    color: '#000',
    /* '&:hover': {
      color: '#3a87ad',
    }, */
  },
  boxHover: {
    /* 마우스 오버(마우스 올렸을때) */
    '&:hover' : {
        backgroundColor: "#dce6f0",

    },
  },
  boxActive: {
    /* 마우스 클릭하고있을때 */
    '&:active' : {
        backgroundColor: "#777",
    },
  },
  boxVisited: {
    /* 마우스 한번클릭후 */
    '&:visited' : {
        color: "white",
    },
  },
}));

function versionParam(version) {
  return version ? `?version=${version}` : '';
}

async function getEntity(id, version) {
  const response = await axios.get(`/entity/SNOMEDCT/${id}${versionParam(version)}`);
  return response.data;
}

async function getDescriptions(id, version) {
  const response = await axios.get(`/descriptions/SNOMEDCT/${id}${versionParam(version)}`);
  return response.data;
}

async function getAssociations(id, version) {
  const response = await axios.get(`/associations/SNOMEDCT/${id}${versionParam(version)}`);
  return response.data;
}

async function getPostexpr(id, version) {
  const response = await axios.get(`/postexpr/SNOMEDCT/${id}${versionParam(version)}`);
  return response.data;
}

async function getMembers(id, version) {
  const vq = version ? `&version=${version}` : '';
  const response = await axios.get(`/members/SNOMEDCT?refcpntid=${id}${vq}`);
  return response.data;
}

async function getHistories(id, version) {
  const response = await axios.get(`/histories/SNOMEDCT/${id}${versionParam(version)}`);
  return response.data;
}

export default function Main(props) {

  const id = props.id;
  const classes = useStyles();
  const [language, setLanguage] = useState("US");
  const [toastMsg, showToast] = useToast();
  const [bookmarks, setBookmarks] = useState(() => {
    try { return JSON.parse(localStorage.getItem(BOOKMARK_KEY) || '[]'); } catch { return []; }
  });

  const isBookmarked = bookmarks.some(b => b.id === id);

  const toggleBookmark = (conceptId, term) => {
    const exists = bookmarks.find(b => b.id === conceptId);
    const next = exists
      ? bookmarks.filter(b => b.id !== conceptId)
      : [...bookmarks, { id: conceptId, term }];
    setBookmarks(next);
    localStorage.setItem(BOOKMARK_KEY, JSON.stringify(next));
    showToast(exists ? 'Bookmark removed' : '⭐ Bookmarked');
  };

  const copyToClipboard = (text, msg) => {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(() => showToast(msg)).catch(() => fallbackCopy(text, msg));
    } else {
      fallbackCopy(text, msg);
    }
  };

  const fallbackCopy = (text, msg) => {
    const ta = document.createElement('textarea');
    ta.value = text;
    ta.style.position = 'fixed';
    ta.style.opacity = '0';
    document.body.appendChild(ta);
    ta.focus();
    ta.select();
    try { document.execCommand('copy'); showToast(msg); } catch (e) { showToast('복사 실패'); }
    document.body.removeChild(ta);
  };

  const copySCTID = (sctid) => {
    copyToClipboard(sctid, 'SCTID copied!');
  };

  const shareLink = (conceptId) => {
    const url = `${window.location.origin}${window.location.pathname}#${conceptId}`;
    copyToClipboard(url, 'Link copied!');
  };

  const exportCSV = (entity, desc, asso) => {
    const rows = [['Section', 'Field', 'Value']];
    rows.push(['Concept', 'SCTID', entity.conceptId]);
    rows.push(['Concept', 'FSN', entity.term]);
    rows.push(['Concept', 'Semantic Tag', entity.semanticTag || '']);
    rows.push(['Concept', 'Definition Status', entity.definitionStatus ? entity.definitionStatus.id : '']);
    if (desc) {
      desc.forEach(d => rows.push(['Description', d.typeId, d.term]));
    }
    if (asso) {
      asso.forEach(a => rows.push(['Relationship', a.type ? a.type.term : '', a.destination ? a.destination.term : '']));
    }
    const csv = rows.map(r => r.map(v => `"${String(v).replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `SNOMEDCT_${entity.conceptId}.csv`; a.click();
    URL.revokeObjectURL(url);
    showToast('CSV downloaded');
  };
  /*
  const [trigger, setRef] = useScrollTrigger({ disableHysteresis: true,threshold: 0 });
  */
  const version = props.version;
  const [stateEntity] = useAsync(() => getEntity(id, version), [id, version]);
  const [stateAsso] = useAsync(() => getAssociations(id, version), [id, version]);
  const [statePost] = useAsync(() => getPostexpr(id, version), [id, version]);
  const [stateDesc] = useAsync(() => getDescriptions(id, version), [id, version]);
  const [stateMemb] = useAsync(() => getMembers(id, version), [id, version]);
  const [stateHist] = useAsync(() => getHistories(id, version), [id, version]);

  const { loadingEntity, data: entity, errorEntity } = stateEntity;
  const { loadingAsso, data: asso, errorAsso } = stateAsso;
  const { loadingPost, data: post, errorPost } = statePost;
  const { loadingDesc, data: desc, errorDesc } = stateDesc;
  const { loadingMemb, data: memb, errorMemb } = stateMemb;
  const { loadingHist, data: hist, errorHist } = stateHist;


  /*
  if (loadingEntity) return <div></div>;
  if (error) {
    console.log(error);
    return <div>에러가 발생했습니다</div>;
  }
*/

  if (!entity || !asso || !post || !desc || !memb || !hist) return null;
  /*console.log(entity + ' ' + asso);
*/

  /*
   * Definition
   */

  /**
   * @name checkIsa
   * @desc type이 IS-A Relationship인지 확인
   * @param {String} typeId Relationship Type Id
   * @return {Boolean}
   */
  const ISA = "116680003";
  function checkIsa(typeId) {
  	return typeId === ISA ? true:false;
  };

  /**
	 * @name bindByRelationshipGroup
	 * @desc Relationship Group에 따라 분류
	 * @param {Array} rels
	 * @return {Array}
	 */
	function bindByRelationshipGroup(rels) {
		let newArr = [],
      types = {},
      key = 'relationshipGroup',
      i, j, cur;

    rels.sort(function(a, b) { // 오름차순
      return a[key] < b[key] ? -1 : a[key] > b[key] ? 1 : 0;
    });

    for (i = 0, j = rels.length; i < j; i++) {
        cur = rels[i];
        if (!(cur[key] in types)) {
          types[cur[key]] = { type: cur[key], data: [] };
          newArr.push(types[cur[key]]);
        }
        types[cur[key]].data.push(cur);
    }

    /*console.log(newArr);*/

    return newArr;
	};

  /**
	* @name splitByRelationshipType
	* @desc Relationship Type에 따라 분리
	* @param {Array} rels
	*/

	// IS-A Relationships
	let isaRels = [];
	// Defining Non-Group Relationships
	let defNonGroupRels = [];
	// Defining Group Relationships
	let defGroupRels = [];

	asso.forEach(function(rel) {
		let isIsa = checkIsa(rel.type.conceptId);
		if (isIsa) {
			isaRels.push(rel);
		} else {
			if (rel.relationshipGroup === "0") {
				defNonGroupRels.push(rel);
			} else {
				defGroupRels.push(rel);
			}
		}
	});

	const NonGroupRels = defNonGroupRels;
	const GroupRels = bindByRelationshipGroup(defGroupRels);

  /*
  console.log(isaRels);
  */

  /*
   * Postexpression
   */
  let postExprHead = [];
  let postExprBody = [];
  if (post.indexOf(":") === -1) {
    postExprHead.push(post);
  } else {
    postExprHead = post.split(':');
    postExprBody.push(postExprHead[1]);
    /* postExprBody = postExprHead[1].split(','); */
  }

  /* typeId */
  const FSN = "900000000000003001";
  const SYNONYM = "900000000000013009";
  /* languageReferenceList.refsetId */
  const GB = "900000000000508004";
  const US = "900000000000509007";
  /* languageReferenceList.acceptabilityId */
  const PREFERRED = "900000000000548007"
  const ACCEPTABLE = "900000000000549004";

  let GBArr = [];
  let USArr = [];

  for (let des of desc) {
    if (des.typeId === FSN) {
      for (let de of (des.languageReferencesetList || [])) {
  	    if (de.refsetId === GB) {

          GBArr.push({
            "descriptionId" : des.descriptionId,
            "typeId" : "F",
            "term" : des.term,
            "acceptabilityId" : de.acceptabilityId
          });
        } else if (de.refsetId === US) {

          USArr.push({
            "descriptionId" : des.descriptionId,
            "typeId" : "F",
            "term" : des.term,
            "acceptabilityId" : de.acceptabilityId
          });
        }
      }
    }
  }

  for (let des of desc) {
    if (des.typeId === SYNONYM) {
      for (let de of (des.languageReferencesetList || [])) {
        if (de.acceptabilityId === PREFERRED) {
	        if (de.refsetId === GB) {

            GBArr.push({
              "descriptionId" : des.descriptionId,
              "typeId" : "P",
              "term" : des.term,
              "acceptabilityId" : de.acceptabilityId
            });
          } else if (de.refsetId === US) {

            USArr.push({
              "descriptionId" : des.descriptionId,
              "typeId" : "P",
              "term" : des.term,
              "acceptabilityId" : de.acceptabilityId
            });
          }
        }
      }
    }
  }

  for (let des of desc) {
    if (des.typeId === SYNONYM) {
      for (let de of (des.languageReferencesetList || [])) {
        if (de.acceptabilityId === ACCEPTABLE) {
	        if (de.refsetId === GB) {

            GBArr.push({
              "descriptionId" : des.descriptionId,
              "typeId" : "S",
              "term" : des.term,
              "acceptabilityId" : de.acceptabilityId
            });
          } else if (de.refsetId === US) {

            USArr.push({
              "descriptionId" : des.descriptionId,
              "typeId" : "S",
              "term" : des.term,
              "acceptabilityId" : de.acceptabilityId
            });
          }
        }
      }
    }
  }

  /*
   * Reference Set
  */
  /*
  for (let mem of memb) {
    if (mem.type.id === '900000000000456007') {
      for (let me in mem.extra) {
        console.log(mem.extra[me].id + ' ' + mem.extra[me].name);
      }
    }
  }
  */
  const refsetDescriptor = '900000000000456007';
  const loincPartMap ='705111002';
  const simpleType = '446609009';

  let refset = [];
  refset[refsetDescriptor] = [];
  refset[loincPartMap] = []
  let refset2 = [];

  let order;
  let c=0;
  let c2=0;

  for (let mem of memb) {
    if (mem.type.id === refsetDescriptor) {
      if (mem.extra["Attribute order"]) {
          order = mem.extra["Attribute order"].id;
          refset[refsetDescriptor][order] = [];
        for (let me in mem.extra) {
          refset[refsetDescriptor][order].push({
            "title": me,
            "id" : mem.extra[me].id,
            "name" : mem.extra[me].name
          })
        }
      }
    } else if (mem.type.id === loincPartMap) {
      refset[loincPartMap][c2]=[];
      for (let me in mem.extra) {
        refset[loincPartMap][c2].push({
          "refsetName": mem.refset.name.split('(')[0] + ' / ' + mem.type.name.split('(')[0] ,
          "title": me,
          "id" : mem.extra[me].id,
          "name" : mem.extra[me].name
        })
      }
      c2++;
    } else {
      refset2[c] = [];
      if (mem.type.id === simpleType) {
        refset2[c].push({
          "refsetName": mem.refset.name.split('(')[0] + ' / ' + mem.type.name.split('(')[0],
          "title": '',
          "id" : '',
          "name" : ''
        })
      } else {
        for (let me in mem.extra) {
          refset2[c].push({
            "refsetName": mem.refset.name.split('(')[0] + ' / ' + mem.type.name.split('(')[0],
            "title": me,
            "id" : mem.extra[me].id,
            "name" : mem.extra[me].name
          })
        }
      }
      c++;
    }
  }

  /* console.log(refset3[loincPartMap]); */

  /*
   * Histories
  */
  let conHist = [];
  let descHist = [];

  for (let his of hist) {
    if (his.definitionStatus ) {
      conHist.push({
        "conceptId" : his.conceptId,
        "active" : his.active.toString(),
        "effectiveTime" : his.effectiveTime,
        "definitionStatus" : his.definitionStatus.name
      });
    } else {
      descHist.push({
        "descriptionId" : his.descriptionId,
        "active" : his.active.toString(),
        "effectiveTime" : his.effectiveTime,
        "term" : his.term
      });
    }
  }
  /*console.log(conHist);
  console.log(descHist);*/

  const semTag = entity ? getSemanticTag(entity.term) : null;
  const semTagClass = getSemanticTagClass(semTag);

  return (
    <div>
      {/* ── 토스트 ── */}
      <div className={`stom-toast ${toastMsg ? 'show' : ''}`}>{toastMsg}</div>

      {/* ── 액션 바 ── */}
      <div className="concept-action-bar">
        <button className="concept-action-btn" onClick={() => copySCTID(entity.conceptId)}>
          📋 {entity.conceptId}
        </button>
        <button
          className={`concept-action-btn ${isBookmarked ? 'active' : ''}`}
          onClick={() => toggleBookmark(entity.conceptId, entity.term)}
        >
          {isBookmarked ? '⭐ Bookmarked' : '☆ Bookmark'}
        </button>
        <button className="concept-action-btn" onClick={() => shareLink(entity.conceptId)}>
          🔗 Share
        </button>
        <button className="concept-action-btn" onClick={() => exportCSV(entity, desc, asso)}>
          ⬇ CSV
        </button>
        {semTag && (
          <span className={`badge badge-st ${semTagClass}`} style={{ marginLeft: 'auto' }}>
            {semTag}
          </span>
        )}
      </div>

      <Container
        className={classes.container}
        style={{
          padding: "0 4px 0 4px",
          height: "87vh",
          overflow: "scroll",
          maxWidth: '100vw'}}>

      <Grid container className={classes.gridcontainer} spacing={1} >
        <Grid item className={classes.gridBorder} p={0} md={12}>
          <Box pt={2} pb={0} px={1}>
            <Typography className="section-label">Definition</Typography>
          </Box>
          <Divider className={classes.divider}/>
          <br />
          <Typography className={classes.label}>
            &nbsp;
            {entity.definitionStatus.id === '900000000000073002' &&
              <Tooltip className={classes.tooltip} placement="top-end" title="Fully Defined" arrow>
                <span className={clsx(classes.alertWarning, classes.badge)}>≡</span>
              </Tooltip>
            }
            {entity.definitionStatus.id === '900000000000074008' &&
              <Tooltip className={classes.tooltip} placement="top-end" title="Primitive" arrow>
                <span className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</span>
              </Tooltip>
            }
            <span > {entity.conceptId} | {entity.term} ({entity.semanticTag}) |</span>
          </Typography>
          <br />
          <div >
            { isaRels &&
              <div>
              { isaRels.map(item => (
                  <Typography component={'div'} key={item.destination.conceptId} className={clsx(classes.lineheight,classes.label)}>
                    &nbsp;
                    <span>
                      {item.type.definitionStatus.id === '900000000000073002' && <span className={clsx(classes.alertWarning, classes.badge)}>≡</span>}
                      {item.type.definitionStatus.id === '900000000000074008' && <span className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</span>}
                      <span> {item.type.conceptId} | {item.type.term} ({item.type.semanticTag}) |</span>
                    </span>
                    <br />
                    &nbsp;
                    <span>
                      <span>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;= </span> {item.destination.definitionStatus.id === '900000000000073002' && <span className={clsx(classes.alertWarning, classes.badge)}>≡</span>}
                      {item.destination.definitionStatus.id === '900000000000074008' && <span className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</span>}
                      <span> {item.destination.conceptId} | {item.destination.term} ({item.destination.semanticTag}) |</span>
                    </span>
                    <br />
                  </Typography>
              ))}
              </div>
            }
          </div>
          <div>
            { NonGroupRels &&
              <div>
              { NonGroupRels.map(item => (
                      <Typography component={'div'} key={item.type.conceptId} className={clsx(classes.lineheight,classes.label)}>
                        &nbsp;
                        <span>
                          {item.type.definitionStatus.id === '900000000000073002' && <span className={clsx(classes.alertWarning, classes.badge)}>≡</span>}
                          {item.type.definitionStatus.id === '900000000000074008' && <span className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</span>}
                          <span> {item.type.conceptId} | {item.type.term} ({item.type.semanticTag}) |</span>
                        </span>
                        <br />
                        &nbsp;
                        <span>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;=</span>
                        <span>
			  {item.destination.definitionStatus.id === 'Concrete Value'  && <span className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</span> &&  <span> {item.destination.term}</span>}
                          {item.destination.definitionStatus.id === '900000000000073002' && <span className={clsx(classes.alertWarning, classes.badge)}>≡</span> && <span> {item.destination.conceptId} | {item.destination.term} ({item.destination.semanticTag}) |</span>}
                          {item.destination.definitionStatus.id === '900000000000074008' && <span className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</span> &&  <span> {item.destination.conceptId} | {item.destination.term} ({item.destination.semanticTag}) |</span>}
                        </span>
                        <br/>
                      </Typography>
              ))}
              </div>
            }
          </div>
          <div>
            { GroupRels &&
            <div>
              { GroupRels.map((items,index) => (
                <div key={index}>
                  { items.data &&
                  <div>
                    <Box
                      onClick={() => props.setMrcmFromMain(items.data)}
                      classes={{root:classes.boxHover}}
                      p={1} m={1} border={4} borderColor="#dce6f0">
                      { items.data.map(item => (
                        <Typography key={item.type.conceptId} component={'div'} className={clsx(classes.lineheight,classes.label)}>
                          &nbsp;
                          <span>
                            {item.type.definitionStatus.id === '900000000000073002' && <span className={clsx(classes.alertWarning, classes.badge)}>≡</span>}
                            {item.type.definitionStatus.id === '900000000000074008' && <span className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</span>}
                            <span> {item.type.conceptId} | {item.type.term} ({item.type.semanticTag}) |</span>
                          </span>
                          <br />
                          &nbsp;
                          <span>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;= </span>
                          <span onClick={() => props.setFromId(item.destination.conceptId)}>
			    {item.destination.definitionStatus.id === 'Concrete Value'  && <span className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</span> && <span> {item.destination.term} </span>}
                            {item.destination.definitionStatus.id === '900000000000073002' && <span className={clsx(classes.alertWarning, classes.badge)}>≡</span> && <span> {item.destination.conceptId} | {item.destination.term} ({item.destination.semanticTag}) |</span>}  
                            {item.destination.definitionStatus.id === '900000000000074008' && <span className={clsx(classes.alertWarning, classes.badge)}>&nbsp;&nbsp;</span> && <span> {item.destination.conceptId} | {item.destination.term} ({item.destination.semanticTag}) |</span>}
                          </span>
                          <br />
                        </Typography>
                      ))}
                    </Box>
                  </div>
                  }
                </div>
              ))}
            </div>
            }
          </div>
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
          <Box pt={2} pb={0} px={1}>
            <Typography className="section-label">Post-coordinated expression</Typography>
          </Box>
          <Divider className={classes.divider}/>
          <br />
          <div  className={clsx(classes.lineheight,classes.label)}>
            { postExprHead[1] === undefined &&
              <Box p={1}>
                {postExprHead[0]}
              </Box>
            }
            { postExprHead[1] &&
              <Box p={1}>
                { postExprHead[0] }:
                <div>
		  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;{ postExprHead[1] }
		</div>
              </Box>
            }
          </div>
        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
          <Box pt={2} pb={0} px={1}>
            <Typography className="section-label">Description</Typography>
          </Box>
          <Divider className={classes.divider}/>
          <Box align="right" p={1}>
            <IconButton style={{padding:"0 0 8px 8px"}} onClick={() => { setLanguage("GB") }}>
              <img src={require("../flag/1x1/gb.svg")} style={{borderRadius:"50%"}} width="25"/>
            </IconButton>

            <IconButton style={{padding:"0 0 8px 8px"}} onClick={() => { setLanguage("US") }}>
              <img src={require("../flag/1x1/us.svg")} style={{borderRadius:"50%"}} width="25"/>
            </IconButton>

          <TableContainer align="center">
            <Table size="small" aria-label="a medium table">
              <TableHead>
                <TableRow >
                  <StyledTableCell className={classes.label} >DescriptionId</StyledTableCell>
                  <StyledTableCell className={classes.label} align="center">Type</StyledTableCell>
                  <StyledTableCell className={classes.label} align="center">Acceptability</StyledTableCell>
                  <StyledTableCell className={classes.label} align="center">Term</StyledTableCell>

                </TableRow>
              </TableHead>
              { language === "US" &&
              <TableBody>
                { USArr.map((row, index) => (
                <TableRow key={index}>
                  <StyledTableCell width="10%" className={classes.label} component="th" scope="row">
                    <span>
                    {row.descriptionId}
                    </span>
                  </StyledTableCell>

                  <StyledTableCell width="5%" align="center">
                    <Tooltip placement="top-end" title={row.typeId === 'F' ? 'Fully Specified Name' : row.typeId === 'P' ? 'Preferred Name' : 'Synonym'} arrow>
                      <span className={`badge ${row.typeId === 'F' ? 'badge-fsn' : 'badge-syn'}`}>
                        {row.typeId === 'F' ? 'FSN' : row.typeId === 'P' ? 'PT' : 'SY'}
                      </span>
                    </Tooltip>
                  </StyledTableCell>

                  <StyledTableCell className={classes.label} width="10%" align="center">
                    {row.acceptabilityId === PREFERRED &&
                      <Tooltip placement="top-end" title="Preferred" arrow>
                        <span className="badge badge-pref">P</span>
                      </Tooltip>
                    }
                    {row.acceptabilityId === ACCEPTABLE &&
                      <Tooltip placement="top-end" title="Acceptable" arrow>
                        <span className="badge badge-accept">A</span>
                      </Tooltip>
                    }
                  </StyledTableCell>

                  <StyledTableCell className={classes.label} width="75%" align="left">
                    {row.term}
                  </StyledTableCell>
                </TableRow>
                ))}
              </TableBody>
              }
              { language === "GB" &&
              <TableBody>
                { GBArr.map((row, index) => (
                <TableRow key={index}>
                  <StyledTableCell width="10%" className={classes.label} component="th" scope="row">
                    <span>
                      {row.descriptionId}
                    </span>
                  </StyledTableCell>

                  <StyledTableCell width="5%" align="center">
                    <Tooltip placement="top-end" title={row.typeId === 'F' ? 'Fully Specified Name' : row.typeId === 'P' ? 'Preferred Name' : 'Synonym'} arrow>
                      <span className={`badge ${row.typeId === 'F' ? 'badge-fsn' : 'badge-syn'}`}>
                        {row.typeId === 'F' ? 'FSN' : row.typeId === 'P' ? 'PT' : 'SY'}
                      </span>
                    </Tooltip>
                  </StyledTableCell>

                    <StyledTableCell className={classes.label} width="10%" align="center">
                      {row.acceptabilityId === PREFERRED &&
                        <Tooltip placement="top-end" title="Preferred" arrow>
                          <span className="badge badge-pref">P</span>
                        </Tooltip>
                      }
                      {row.acceptabilityId === ACCEPTABLE &&
                        <Tooltip placement="top-end" title="Acceptable" arrow>
                          <span className="badge badge-accept">A</span>
                        </Tooltip>
                      }
                    </StyledTableCell>

                  <StyledTableCell className={classes.label} width="75%" align="left">
                    {row.term}
                  </StyledTableCell>
                </TableRow>
                ))}
              </TableBody>
              }
            </Table>
          </TableContainer>
          </Box>

        </Grid>
        <Grid item className={classes.gridBorder} md={12} >
          <Box pt={2} pb={0} px={1}>
            <Typography className="section-label">Reference Set</Typography>
          </Box>
          <Divider className={classes.divider}/>
          < br/>
          { refset[refsetDescriptor][0] &&
            <Box p={1}>
            <Typography className={classes.label}><b>@Reference set descriptor reference set</b></Typography>

            <TableContainer p={1} align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                    { refset[refsetDescriptor][0].map((rs0, index) => (
                    <StyledTableCell key={index} className={classes.label}>
                      {rs0.title}
                    </StyledTableCell>
                    ))}
                  </TableRow>
                </TableHead>
                <TableBody>
                  { refset[refsetDescriptor].map((rs,index2) => (
                  <TableRow key={index2}>
                    { rs.map((r,index3) => (
                      <StyledTableCell key={index3}  >
                      { r.title === "Attribute order" && <div>
                        {r.id}
                        </div>
                      }
                      { r.title !== "Attribute order" && <div>
                        {r.name}
                        </div>
                      }
                      </StyledTableCell>
                    ))}
                  </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            <br />
            </Box>
          }

          { refset2.length > 0 && refset2[0].length > 0 && refset2[0][0].refsetName &&
            <>
            {refset2.map((rs, index5) => (
              <Box p={1} key={index5}>
              <Typography className={classes.label}><b>@{rs[0].refsetName}</b></Typography>
              <TableContainer align="center">
                <Table size="small" aria-label="a small table">
                  <TableHead>
                    <TableRow >
                      { rs.map((r0, index6) => (
                        <StyledTableCell key={index6} className={classes.label}>
                          {r0.title}
                        </StyledTableCell>
                      ))}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    <TableRow>
                    { rs.map((r,index7) => (
                        <StyledTableCell key={index7} >
                        { r.name &&
                          <div>
                            {r.name}
                          </div>
                        }
                        { r.name === undefined &&
                          <div>
                            {r.id}
                          </div>
                        }
                        </StyledTableCell>
                    ))}
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
              <br />
              </Box>
            ))}
            </>
          }
          { refset[loincPartMap].length !== 0 &&
            <Box p={1}>
            <Typography className={classes.label}><b>@{refset[loincPartMap][0][0].refsetName}</b></Typography>
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <colgroup>
                  <col style={{width:'13%'}}/>
                  <col style={{width:'40%'}}/>
                  <col style={{width:'34%'}}/>
                  <col style={{width:'13%'}}/>
                </colgroup>
                <TableHead>
                  <TableRow >
                  { refset[loincPartMap][0].map ((r0, index5) => (
                    <StyledTableCell key={index5} className={classes.label}>
                      {r0.title}
                    </StyledTableCell>
                  )) }
                  </TableRow>
                </TableHead>
                <TableBody>
                  { refset[loincPartMap].map ((rs, index6) => (
                  <TableRow key={index6}>
                    { rs.map((r,index7) => (
                        <StyledTableCell key={index7} >
                        { r.name &&
                          <div>
                            {r.name}
                          </div>
                        }
                        { r.name === undefined &&
                          <div>
                            {r.id}
                          </div>
                        }
                        </StyledTableCell>
                    ))}
                  </TableRow>
                ))}
                </TableBody>
              </Table>
            </TableContainer>
            <br />
            </Box>
          }

        </Grid>

        <Grid item className={classes.gridBorder} md={12} >
          <Box pt={2} pb={0} px={1}>
            <Typography className="section-label">History</Typography>
          </Box>
          <Divider className={classes.divider}/>

          <Box p={1}>
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                      <StyledTableCell width="20%" className={classes.label}>
                        {"Concept ID"}
                      </StyledTableCell>
                      <StyledTableCell width="10%" className={classes.label}>
                        {"Active"}
                      </StyledTableCell>
                      <StyledTableCell width="25%" className={classes.label}>
                        {"Effective Time"}
                      </StyledTableCell>
                      <StyledTableCell width="45%" className={classes.label}>
                        {"Definition Status"}
                      </StyledTableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {conHist.map((ch, index) => (
                    <TableRow key={index}>
                      <StyledTableCell width="20%" className={classes.label}>
                        <span>
                          {ch["conceptId"]}
                        </span>
                      </StyledTableCell>
                      <StyledTableCell width="10%" className={classes.label}>
                        {ch["active"]}
                      </StyledTableCell>
                      <StyledTableCell width="25%" className={classes.label}>
                        {ch["effectiveTime"]}
                      </StyledTableCell>
                      <StyledTableCell width="45%" className={classes.label}>
                        {ch["definitionStatus"]}
                      </StyledTableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
          <Box p={1}>
            <TableContainer align="center">
              <Table size="small" aria-label="a small table">
                <TableHead>
                  <TableRow >
                      <StyledTableCell width="20%" className={classes.label}>
                        {"Description ID"}
                      </StyledTableCell>
                      <StyledTableCell width="10%" className={classes.label}>
                        {"Active"}
                      </StyledTableCell>
                      <StyledTableCell width="20%" className={classes.label}>
                        {"Effective Time"}
                      </StyledTableCell>
                      <StyledTableCell width="50%" className={classes.label}>
                        {"Term"}
                      </StyledTableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {descHist.map((dh, index) => (
                    <TableRow key={index}>
                      <StyledTableCell width="20%" className={classes.label}>
                        <span>
                        {dh["descriptionId"]}
                        </span>
                      </StyledTableCell>
                      <StyledTableCell width="10%" className={classes.label}>
                        {dh["active"]}
                      </StyledTableCell>
                      <StyledTableCell width="20%" className={classes.label}>
                        {dh["effectiveTime"]}
                      </StyledTableCell>
                      <StyledTableCell width="50%" className={classes.label}>
                        {dh["term"]}
                      </StyledTableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        </Grid>
      </Grid>
      </Container>
    </div>
  )
}
