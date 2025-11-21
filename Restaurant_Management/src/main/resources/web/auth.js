// Authentication utilities
const API_BASE = window.location.origin;
const TOKEN_KEY = 'restaurant_auth_token';
const USER_KEY = 'restaurant_user';

// Check if user is authenticated
function isAuthenticated() {
    return localStorage.getItem(TOKEN_KEY) !== null;
}

// Get auth token
function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

// Get current user
function getCurrentUser() {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
}

// Save auth data
function saveAuth(token, user) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
}

// Clear auth data
function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

// Make authenticated API request
async function apiRequest(url, options = {}) {
    const token = getToken();
    
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    const response = await fetch(`${API_BASE}${url}`, {
        ...options,
        headers
    });
    
    // If unauthorized, redirect to login
    if (response.status === 401) {
        clearAuth();
        window.location.href = 'login.html';
        throw new Error('Unauthorized');
    }
    
    return response;
}

// Redirect to login if not authenticated
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}


