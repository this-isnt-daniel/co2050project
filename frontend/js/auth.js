class AuthService {
    static init() {
        this.loginForm = document.getElementById('login-form');
        this.loginError = document.getElementById('login-error');
        this.loginView = document.getElementById('login-view');
        this.dashboardView = document.getElementById('dashboard-view');
        
        if (this.loginForm) {
            this.loginForm.addEventListener('submit', this.handleLogin.bind(this));
        }

        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', this.handleLogout.bind(this));
        }

        // Listen for 401 Unauthorized events from api.js
        window.addEventListener('auth:unauthorized', () => {
            this.showLogin("Your session has expired. Please log in again.");
        });
    }

    static async handleLogin(e) {
        e.preventDefault();
        this.loginError.style.display = 'none';
        
        const usernameInput = document.getElementById('username').value;
        const passwordInput = document.getElementById('password').value;
        
        const submitBtn = this.loginForm.querySelector('button');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Logging in...';

        try {
            // Adjust endpoint and payload to match Spring Boot backend
            const response = await ApiClient.post('/auth/login', {
                username: usernameInput,
                password: passwordInput
            });

            // Assuming backend returns { token: "..." }
            const token = response.token || response.accessToken || response.jwt;
            if (token) {
                ApiClient.setToken(token);
                
                // Decode JWT to get user info (role, username)
                const userInfo = this.decodeJWT(token);
                sessionStorage.setItem('user_info', JSON.stringify(userInfo));

                this.showDashboard();
            } else {
                throw new Error("Invalid response from server.");
            }
        } catch (error) {
            this.loginError.textContent = error.message || "Invalid username or password.";
            this.loginError.style.display = 'block';
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Login';
        }
    }

    static handleLogout() {
        ApiClient.clearToken();
        this.showLogin();
    }

    static showLogin(message = null) {
        this.dashboardView.style.display = 'none';
        this.loginView.style.display = 'flex';
        
        if (message) {
            this.loginError.textContent = message;
            this.loginError.style.display = 'block';
        } else {
            this.loginError.style.display = 'none';
        }
        
        if (this.loginForm) {
            this.loginForm.reset();
        }
    }

    static showDashboard() {
        this.loginView.style.display = 'none';
        this.dashboardView.style.display = 'flex';
        
        // Trigger app rendering now that we are logged in
        window.dispatchEvent(new Event('app:start'));
    }

    static decodeJWT(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            console.error("Error decoding JWT", e);
            // Fallback for UI if JWT decoding fails
            return { sub: document.getElementById('username').value, role: 'UNKNOWN' };
        }
    }

    static isAuthenticated() {
        return !!ApiClient.getToken();
    }

    static getUserInfo() {
        const info = sessionStorage.getItem('user_info');
        return info ? JSON.parse(info) : null;
    }
}
