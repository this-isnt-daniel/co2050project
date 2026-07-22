const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Core API utility to handle fetches with JWT injection and automatic 401 handling.
 */
class ApiClient {
    static getToken() {
        return sessionStorage.getItem('jwt_token');
    }

    static setToken(token) {
        sessionStorage.setItem('jwt_token', token);
    }

    static clearToken() {
        sessionStorage.removeItem('jwt_token');
        sessionStorage.removeItem('user_info');
    }

    static handleUnauthorized() {
        this.clearToken();
        // Trigger a custom event to notify the app to show the login screen
        window.dispatchEvent(new Event('auth:unauthorized'));
    }

    static async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);

            if (response.status === 401) {
                this.handleUnauthorized();
                throw new Error('Session expired or unauthorized');
            }

            // For 204 No Content
            if (response.status === 204) {
                return null;
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const errorMsg = data?.message || response.statusText || 'API Request Failed';
                throw new Error(errorMsg);
            }

            return data;
        } catch (error) {
            console.error(`API Error (${endpoint}):`, error);
            throw error;
        }
    }

    static async get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    static async post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    }

    static async put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    }

    static async delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }

    static async upload(endpoint, formData) {
        const headers = {};
        const token = this.getToken();
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: headers,
                body: formData
            });

            if (response.status === 401) {
                this.handleUnauthorized();
                throw new Error('Unauthorized');
            }

            if (!response.ok) {
                const data = await response.json().catch(() => ({}));
                throw new Error(data.message || 'API request failed');
            }

            return await response.json().catch(() => null);
        } catch (error) {
            console.error(`API Upload Error (${endpoint}):`, error);
            throw error;
        }
    }
}
