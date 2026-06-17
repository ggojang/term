import React, { useState } from 'react';
import Grid from '@material-ui/core/Grid';
import HiraLeft from './left.js';
import HiraMain from './main.js';

export default function HiraLayout({ selectedCode, setSelectedCode }) {
  const [category, setCategory] = useState('행위'); // 행위 | 약제 | 치료재료

  const handleSelect = (cat, code) => {
    setSelectedCode({ category: cat, code });
  };

  const sel = selectedCode || {};

  return (
    <Grid container style={{ height: 'calc(100vh - 64px)', overflow: 'hidden' }}>
      <Grid
        item
        xs={3}
        style={{ borderRight: '1px solid #e0e0e0', overflowY: 'auto', height: '100%' }}
      >
        <HiraLeft
          category={category}
          setCategory={setCategory}
          selectedCode={sel.code}
          onSelect={(code) => handleSelect(category, code)}
        />
      </Grid>
      <Grid item xs={9} style={{ overflowY: 'auto', height: '100%', padding: '0 16px' }}>
        <HiraMain category={sel.category} code={sel.code} />
      </Grid>
    </Grid>
  );
}
