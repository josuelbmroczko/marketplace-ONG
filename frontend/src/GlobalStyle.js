import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
// Importe o GlobalStyle
import { GlobalStyle } from './styles.js';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    {/* Adicione o GlobalStyle aqui */}
    <GlobalStyle />
    <App />
  </React.StrictMode>,
);