import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api/v1'; // Replace with your backend URL

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: false, // Include cookies for refresh tokens
});

// Function to get the CSRF token from cookies
function getCsrfToken() {
    const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
    if (match) return decodeURIComponent(match[2]);
    return null;
}

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jwtToken');
        if (
            token &&
            config.headers &&
            !config.url?.includes('/auth/signup') &&
            !config.url?.includes('/auth/login') &&
            !config.url?.includes('/auth/refresh-token')
        ) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Handle token refresh on 401 responses
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (
            error.response?.status === 401 &&
            !originalRequest._retry &&
            !originalRequest.url.includes('/auth/login') &&
            !originalRequest.url.includes('/auth/signup') &&
            !originalRequest.url.includes('/auth/refresh-token')
        ) {
            originalRequest._retry = true;
            try {
                await api.post('/auth/refresh-token');
                return api(originalRequest);
            } catch (err) {
                // Redirect to login page or handle as per your app logic
                window.location.href = '/login';
                return Promise.reject(err);
            }
        }
        return Promise.reject(error);
    }
);

export default api;
