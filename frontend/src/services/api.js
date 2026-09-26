import axios from 'axios';

export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    
    // Standardize error message extraction
    const message = 
      error.response?.data?.message || 
      error.response?.data?.details ||
      error.message || 
      'Something went wrong';
      
    error.formattedMessage = message;
    
    return Promise.reject(error);
  }
);

export const authAPI = {
  register: (username, password) =>
    api.post('/auth/register', { username, password }),
  login: (username, password) =>
    api.post('/auth/login', { username, password }),
};

export const bookAPI = {
  getAll: (params = {}) => api.get('/books', { params }),
  getById: (id) => api.get(`/books/${id}`),
  create: (bookData, file) => {
    const formData = new FormData();
    formData.append('book', JSON.stringify(bookData));
    if (file) {
      formData.append('file', file);
    }
    return api.post('/books', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  update: (id, bookData, file) => {
    const formData = new FormData();
    formData.append('book', JSON.stringify(bookData));
    if (file) {
      formData.append('file', file);
    }
    return api.put(`/books/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  delete: (id) => api.delete(`/books/${id}`),
  toggleLike: (id) => api.post(`/books/${id}/like`),
  getFavorites: () => api.get('/books/favorites'),
  search: (params) => api.get('/books/search', { params }),
};

export const commentAPI = {
  getByBook: (bookId) => api.get(`/books/${bookId}/comments`),
  create: (bookId, text) =>
    api.post(`/books/${bookId}/comments`, { text }),
  delete: (id) => api.delete(`/books/comments/${id}`),
  update: (id, text) => api.put(`/books/comments/${id}`, { text }),
};

export const ratingAPI = {
  rate: (bookId, score, liked) =>
    api.post(`/books/${bookId}/rating`, { score, liked }),
  getStats: (bookId) => api.get(`/books/${bookId}/stats`),
  remove: (bookId) => api.delete(`/books/${bookId}/rating`),
};

export const userAPI = {
  getProfile: () => api.get('/users/me'),
  updateProfile: (profileData, avatar) => {
    const formData = new FormData();
    if (profileData) {
      formData.append('profile', JSON.stringify(profileData));
    }
    if (avatar) {
      formData.append('avatar', avatar);
    }
    return api.put('/users/profile', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  updatePrivacy: (settings) =>
    api.put('/users/privacy', settings),
  getHistory: () => api.get('/users/history'),
};

export const qrcodeAPI = {
  generateByBookId: (bookId) => api.get(`/qrcode/book/${bookId}`, { responseType: 'blob' }),
  generateByQRCode: (qrCode) => api.get(`/qrcode/${qrCode}`, { responseType: 'blob' }),
  generateByExchangeId: (exchangeId) => api.get(`/qrcode/exchange/${exchangeId}`, { responseType: 'blob' }),
  scan: (qrCode) => api.get(`/qrcode/scan/${qrCode}`),
};

export const exchangeAPI = {
  create: (requestedBookId, offeredBookId, message) =>
    api.post('/exchanges', {
      requestedBookId,
      offeredBookId,
      message,
    }),
  getAll: () => api.get('/exchanges'),
  getById: (id) => api.get(`/exchanges/${id}`),
  accept: (id) => api.post(`/exchanges/${id}/accept`),
  reject: (id) => api.post(`/exchanges/${id}/reject`),
  markAsShipped: (id, trackingNumber) => api.post(`/exchanges/${id}/mark-shipped`, { trackingNumber }),
  markAsDelivered: (id) => api.post(`/exchanges/${id}/mark-delivered`),
  confirmReceipt: (id) => api.post(`/exchanges/${id}/confirm-receipt`),
  cancel: (id) => api.post(`/exchanges/${id}/cancel`),
};

export default api;

