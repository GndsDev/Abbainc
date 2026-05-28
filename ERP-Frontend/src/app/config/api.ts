const isLocal = window.location.hostname === 'localhost';

export const API_BASE_URL = isLocal
  ? 'http://localhost:8080/api'
  : 'https://abbainc-backend.onrender.com/api';
