import React, { useState } from 'react';
import Main from './main.js';
import Left from './left.js';

export default function MapLayout() {
  const [semanTag, setSemanTag] = useState([]);
  return <Main semanTag={semanTag} setSemanTag={setSemanTag} Left={Left} />;
}
