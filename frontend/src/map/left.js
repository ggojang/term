import React, { useState, useEffect} from 'react';
import axios from 'axios';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import Grid from "@material-ui/core/Grid";
import Typography from '@material-ui/core/Typography';
import { Link } from "react-router-dom";
import FormLabel from '@material-ui/core/FormLabel';
import FormControl from '@material-ui/core/FormControl';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormHelperText from '@material-ui/core/FormHelperText';
import Checkbox from '@material-ui/core/Checkbox';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import Container from '@material-ui/core/Container';
import Divider from "@material-ui/core/Divider";

const useStyles = makeStyles((theme) => ({
  container: {
    '-ms-overflow-style': 'none', /* IE and Edge */
    scrollbarWidth: 'none', /* Firefox */
    '&::-webkit-scrollbar': {
        display: 'none', /* Chrome, Safari, Opera*/
    },
  },
  link: {
    textDecoration: "none",
    color: '#000',
    /* '&:hover': {
      color: '#3a87ad',
    }, */
  },
  label: {
    fontSize: '0.7em',
  },
  height: {
    height: "22px",
  },
  lineheight: {
    lineHeight: 2,
  },
  formControl: {
    margin: theme.spacing(0),
  },
  gridBorder: {
    borderRight: "dotted 1px lightGray",
  },
  divider: {
    borderBottom: "solid 2px #2196F3",
  },
}));



export default function Left(props) {

  const classes = useStyles();

  const semanticTags = [
      {name:'finding',state:true}, {name:'disorder',state:true}, {name:'procedure',state:false},
      {name:'event',state:true}, {name:'regime/therapy',state:false}, {name:'situation',state:true},
      {name:'administrative concept',state:false},
      {name:'assessment scale',state:false}, {name:'attribute',state:false}, {name:'basic dose form',state:false},
      {name:'body structure',state:false}, {name:'cell',state:false}, {name:'cell structure',state:false},
      {name:'clinical drug',state:false}, {name: 'context-dependent category',state:false},
      {name:'core metadata concept',state:false}, {name:'disposition',state:false},
      {name:'dose form',state:false}, {name:'environment',state:false}, {name:'ethnic group',state:false},
      {name:'foundation metadata concept',state:false}, {name:'geographic location',state:false}, {name:'inactive concept',state:false},
      {name:'intended site',state:false}, {name:'life style',state:false}, {name:'link assertion',state:false},
      {name:'linkage concept',state:false}, {name:'medicinal product form',state:false}, {name:'medicinal product',state:false},
      {name:'metadata',state:false}, {name:'morphologic abnormality',state:false}, {name:'namespace concept',state:false},
      {name:'navigational concept',state:false}, {name:'observable entity',state:false}, {name:'occupation',state:false},
      {name:'organism',state:false}, {name:'OWL metadata concept',state:false}, {name:'person',state:false},
      {name:'physical force',state:false}, {name:'physical object',state:false}, {name:'product name',state:false},
      {name:'product',state:false}, {name:'qualifier value',state:false}, {name:'racial group',state:false},
      {name:'record artifact',state:false}, {name:'release characteristic',state:false}, {name:'religion/philosophy',state:false},
      {name:'role',state:false}, {name:'SNOMED RT+CTV3',state:false}, {name:'social concept',state:false},
      {name:'special concept',state:false}, {name:'specimen',state:false}, {name:'staging scale',state:false},
      {name:'state of matter',state:false}, {name:'substance',state:false}, {name:'supplier',state:false},
      {name:'transformation',state:false}, {name:'tumor staging',state:false}, {name:'unit of presentation',state:false}
      , {name:'virtual clinical drug',state:false}
  ];

  const [radioState, setRadioState] = useState('diagnosis');
  const [checkState, setCheckState] = useState(semanticTags);

  useEffect(() => {
    props.setSemanTag(checkState);
  }, [checkState, radioState])

  const handleRadioChange = (event) => {
    setRadioState(event.target.value);

    if (event.target.value === 'diagnosis') {
      setCheckState([
        {name:'finding',state:true}, {name:'disorder',state:true}, {name:'procedure',state:false},
        {name:'event',state:true}, {name:'regime/therapy',state:false}, {name:'situation',state:true},
        {name:'administrative concept',state:false},
        {name:'assessment scale',state:false}, {name:'attribute',state:false}, {name:'basic dose form',state:false},
        {name:'body structure',state:false}, {name:'cell',state:false}, {name:'cell structure',state:false},
        {name:'clinical drug',state:false}, {name: 'context-dependent category',state:false},
        {name:'core metadata concept',state:false}, {name:'disposition',state:false},
        {name:'dose form',state:false}, {name:'environment',state:false}, {name:'ethnic group',state:false},
        {name:'foundation metadata concept',state:false}, {name:'geographic location',state:false}, {name:'inactive concept',state:false},
        {name:'intended site',state:false}, {name:'life style',state:false}, {name:'link assertion',state:false},
        {name:'linkage concept',state:false}, {name:'medicinal product form',state:false}, {name:'medicinal product',state:false},
        {name:'metadata',state:false}, {name:'morphologic abnormality',state:false}, {name:'namespace concept',state:false},
        {name:'navigational concept',state:false}, {name:'observable entity',state:false}, {name:'occupation',state:false},
        {name:'organism',state:false}, {name:'OWL metadata concept',state:false}, {name:'person',state:false},
        {name:'physical force',state:false}, {name:'physical object',state:false}, {name:'product name',state:false},
        {name:'product',state:false}, {name:'qualifier value',state:false}, {name:'racial group',state:false},
        {name:'record artifact',state:false}, {name:'release characteristic',state:false}, {name:'religion/philosophy',state:false},
        {name:'role',state:false}, {name:'SNOMED RT+CTV3',state:false}, {name:'social concept',state:false},
        {name:'special concept',state:false}, {name:'specimen',state:false}, {name:'staging scale',state:false},
        {name:'state of matter',state:false}, {name:'substance',state:false}, {name:'supplier',state:false},
        {name:'transformation',state:false}, {name:'tumor staging',state:false}, {name:'unit of presentation',state:false}
        , {name:'virtual clinical drug',state:false}
      ]);
    } else if (event.target.value === 'procedure') {
      setCheckState([
        {name:'finding',state:false}, {name:'disorder',state:false}, {name:'procedure',state:true},
        {name:'event',state:false}, {name:'regime/therapy',state:true}, {name:'situation',state:true},
        {name:'administrative concept',state:false},
        {name:'assessment scale',state:false}, {name:'attribute',state:false}, {name:'basic dose form',state:false},
        {name:'body structure',state:false}, {name:'cell',state:false}, {name:'cell structure',state:false},
        {name:'clinical drug',state:false}, {name: 'context-dependent category',state:false},
        {name:'core metadata concept',state:false}, {name:'disposition',state:false},
        {name:'dose form',state:false},{name:'environment',state:false}, {name:'ethnic group',state:false},
        {name:'foundation metadata concept',state:false}, {name:'geographic location',state:false}, {name:'inactive concept',state:false},
        {name:'intended site',state:false}, {name:'life style',state:false}, {name:'link assertion',state:false},
        {name:'linkage concept',state:false}, {name:'medicinal product form',state:false}, {name:'medicinal product',state:false},
        {name:'metadata',state:false}, {name:'morphologic abnormality',state:false}, {name:'namespace concept',state:false},
        {name:'navigational concept',state:false}, {name:'observable entity',state:false}, {name:'occupation',state:false},
        {name:'organism',state:false}, {name:'OWL metadata concept',state:false}, {name:'person',state:false},
        {name:'physical force',state:false}, {name:'physical object',state:false}, {name:'product name',state:false},
        {name:'product',state:false}, {name:'qualifier value',state:false}, {name:'racial group',state:false},
        {name:'record artifact',state:false}, {name:'release characteristic',state:false}, {name:'religion/philosophy',state:false},
        {name:'role',state:false}, {name:'SNOMED RT+CTV3',state:false}, {name:'social concept',state:false},
        {name:'special concept',state:false}, {name:'specimen',state:false}, {name:'staging scale',state:false},
        {name:'state of matter',state:false}, {name:'substance',state:false}, {name:'supplier',state:false},
        {name:'transformation',state:false}, {name:'tumor staging',state:false}, {name:'unit of presentation',state:false}
        , {name:'virtual clinical drug',state:false}
      ]);
    } else if (event.target.value === 'all') {
      setCheckState([
        {name:'finding',state:true}, {name:'disorder',state:true}, {name:'procedure',state:true},
        {name:'event',state:true}, {name:'regime/therapy',state:true}, {name:'situation',state:true},
        {name:'administrative concept',state:true},
        {name:'assessment scale',state:true}, {name:'attribute',state:true}, {name:'basic dose form',state:true},
        {name:'body structure',state:true}, {name:'cell',state:true}, {name:'cell structure',state:true},
        {name:'clinical drug',state:true}, {name: 'context-dependent category',state:true},
        {name:'core metadata concept',state:true}, {name:'disposition',state:true},
        {name:'dose form',state:true},{name:'environment',state:true}, {name:'ethnic group',state:true},
        {name:'foundation metadata concept',state:true}, {name:'geographic location',state:true}, {name:'inactive concept',state:true},
        {name:'intended site',state:true}, {name:'life style',state:true}, {name:'link assertion',state:true},
        {name:'linkage concept',state:true}, {name:'medicinal product form',state:true}, {name:'medicinal product',state:true},
        {name:'metadata',state:true}, {name:'morphologic abnormality',state:true}, {name:'namespace concept',state:true},
        {name:'navigational concept',state:true}, {name:'observable entity',state:true}, {name:'occupation',state:true},
        {name:'organism',state:true}, {name:'OWL metadata concept',state:true}, {name:'person',state:true},
        {name:'physical force',state:true}, {name:'physical object',state:true}, {name:'product name',state:true},
        {name:'product',state:true}, {name:'qualifier value',state:true}, {name:'racial group',state:true},
        {name:'record artifact',state:true}, {name:'release characteristic',state:true}, {name:'religion/philosophy',state:true},
        {name:'role',state:true}, {name:'SNOMED RT+CTV3',state:true}, {name:'social concept',state:true},
        {name:'special concept',state:true}, {name:'specimen',state:true}, {name:'staging scale',state:true},
        {name:'state of matter',state:true}, {name:'substance',state:true}, {name:'supplier',state:true},
        {name:'transformation',state:true}, {name:'tumor staging',state:true}, {name:'unit of presentation',state:true}
        , {name:'virtual clinical drug',state:false}
      ]);
    } else if (event.target.value === 'none') {
      setCheckState([
        {name:'finding',state:false}, {name:'disorder',state:false}, {name:'procedure',state:false},
        {name:'event',state:false}, {name:'regime/therapy',state:false}, {name:'situation',state:false},
        {name:'administrative concept',state:false},
        {name:'assessment scale',state:false}, {name:'attribute',state:false}, {name:'basic dose form',state:false},
        {name:'body structure',state:false}, {name:'cell',state:false}, {name:'cell structure',state:false},
        {name:'clinical drug',state:false}, {name: 'context-dependent category',state:false}, 
        {name:'core metadata concept',state:false}, {name:'disposition',state:false},
        {name:'dose form',state:false}, {name:'environment',state:false}, {name:'ethnic group',state:false},
        {name:'foundation metadata concept',state:false}, {name:'geographic location',state:false}, {name:'inactive concept',state:false},
        {name:'intended site',state:false}, {name:'life style',state:false}, {name:'link assertion',state:false},
        {name:'linkage concept',state:false}, {name:'medicinal product form',state:false}, {name:'medicinal product',state:false},
        {name:'metadata',state:false}, {name:'morphologic abnormality',state:false}, {name:'namespace concept',state:false},
        {name:'navigational concept',state:false}, {name:'observable entity',state:false}, {name:'occupation',state:false},
        {name:'organism',state:false}, {name:'OWL metadata concept',state:false}, {name:'person',state:false},
        {name:'physical force',state:false}, {name:'physical object',state:false}, {name:'product name',state:false},
        {name:'product',state:false}, {name:'qualifier value',state:false}, {name:'racial group',state:false},
        {name:'record artifact',state:false}, {name:'release characteristic',state:false}, {name:'religion/philosophy',state:false},
        {name:'role',state:false}, {name:'SNOMED RT+CTV3',state:false}, {name:'social concept',state:false},
        {name:'special concept',state:false}, {name:'specimen',state:false}, {name:'staging scale',state:false},
        {name:'state of matter',state:false}, {name:'substance',state:false}, {name:'supplier',state:false},
        {name:'transformation',state:false}, {name:'tumor staging',state:false}, {name:'unit of presentation',state:false}
	, {name:'virtual clinical drug',state:false}
      ]);
    }
  };

  const handleCheckChange = (event) => {
    let tmp = [];
    checkState.forEach((item, index, arr) => {
      if ( arr[index].name === event.target.name) {
        arr[index].state = event.target.checked;
        //console.log(event.target.name, event.target.checked);
      }
      tmp.push(arr[index]);
    })
    //setCheckState(checkState => ({ [ ...checkState, {name:event.target.name, state:event.target.checked }]);
    setCheckState(tmp);
    //console.log(tmp);
  };

  return (
    <Grid container>
      <Grid item className={classes.gridBorder}>
        <FormControl component="fieldset" className={classes.formControl} style={{
          margin : "0 0 0 0", padding: "0 0 0 12px"}}>
          <FormLabel style={{margin:"0 0 4px 0"}} component="legend" >Semantic Tag ({semanticTags.length})</FormLabel>
          <RadioGroup row aria-label="selectRadio1" name="selectRadio1" value={radioState} onChange={handleRadioChange}>
            <FormControlLabel className={clsx(classes.height)}  value="all" control={<Radio size='small'/>} label={<Typography variant="body2">All</Typography>} />
            <FormControlLabel className={clsx(classes.height)}  value="none" control={<Radio size='small'/>} label={<Typography variant="body2">None</Typography>} />
          </RadioGroup>
          <RadioGroup row aria-label="selectRadio2" name="selectRadio2" value={radioState} onChange={handleRadioChange}>
            <FormControlLabel className={clsx(classes.height)}  value="diagnosis" control={<Radio size='small'/>} label={<Typography variant="body2">Diagnosis</Typography>} />
            <FormControlLabel className={clsx(classes.height)}  value="procedure" control={<Radio size='small'/>} label={<Typography variant="body2">Procedure</Typography>} />
          </RadioGroup>
          <Divider style={{margin:"4px 0 4px 0"}} className={classes.divider}/>
          <Container
            className={classes.container} /*ref={setRef}*/
            style={{
              margin : "0 0 0 0",
              padding: "0 0 0 0",
              height: "78vh",
              overflow: "scroll"}}>
            <FormGroup row>
            {
              checkState.map( (tag, index) => (
                <div key={index} >
                  <FormControlLabel
                    className={clsx(classes.height)}
                    control={<Checkbox size='small' checked={tag.state} onChange={handleCheckChange} name={tag.name} />}
                    label={<Typography className={classes.label}>{tag.name}</Typography>}
                  />
                </div>
              ))
            }
            </FormGroup>
          </Container>

        </FormControl>
      </Grid>
    </Grid>
  );

}

