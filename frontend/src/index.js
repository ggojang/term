import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter } from "react-router-dom";
import axios from 'axios';
import './index.css';
import App from './App';
import * as serviceWorker from './serviceWorker';

const BASE_PATH = process.env.PUBLIC_URL || '';
if (BASE_PATH) {
  axios.interceptors.request.use(config => {
    if (config.url && config.url.startsWith('/') && !config.url.startsWith(BASE_PATH)) {
      config.url = BASE_PATH + config.url;
    }
    return config;
  });
}

ReactDOM.render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
  document.getElementById('root')
);

serviceWorker.unregister();
