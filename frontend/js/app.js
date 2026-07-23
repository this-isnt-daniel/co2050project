class App {
    static init() {
        AuthService.init();

        if (AuthService.isAuthenticated()) {
            AuthService.showDashboard();
        } else {
            AuthService.showLogin();
        }

        window.addEventListener('app:start', () => {
            this.renderDashboard();
        });
    }

    static renderDashboard() {
        const user = AuthService.getUserInfo();
        const role = user?.role || user?.roles || 'USER';
        const username = user?.sub || user?.username || 'User';

        // Update Topbar
        document.getElementById('user-name').textContent = username;
        
        const badge = document.getElementById('user-role-badge');
        // Clean up role string if it comes as e.g., "ROLE_ADMIN" or ["ROLE_ADMIN"]
        const roleStr = Array.isArray(role) ? role[0] : role;
        const cleanRole = roleStr.replace('ROLE_', '');
        
        badge.textContent = cleanRole;
        badge.className = `badge ${cleanRole.toLowerCase()}`;

        this.renderSidebar(cleanRole);
        this.loadDashboardData(cleanRole);
    }

    static renderSidebar(role) {
        const nav = document.getElementById('sidebar-nav');
        nav.innerHTML = ''; // clear

        const links = [
            { id: 'nav-dashboard', label: 'Dashboard Overview', roles: ['ALL'] },
            { id: 'nav-users', label: 'User Management', roles: ['ADMIN'] },
            { id: 'nav-patients', label: 'Patients', roles: ['ADMIN', 'DOCTOR', 'JMO', 'CLERICAL', 'RESEARCHER'] },
            { id: 'nav-cases', label: 'Cases', roles: ['ADMIN', 'DOCTOR', 'JMO', 'CLERICAL', 'RESEARCHER'] },
            { id: 'nav-mlef', label: 'MLEF Referrals', roles: ['ADMIN', 'DOCTOR', 'JMO'] },
            { id: 'nav-evidence', label: 'Evidence Chain', roles: ['ADMIN', 'LAB_STAFF', 'JMO'] },
            { id: 'nav-reports', label: 'Court Reports', roles: ['ADMIN', 'JMO', 'CLERICAL'] },
            { id: 'nav-mlr', label: 'Medico-Legal Reports', roles: ['ADMIN', 'JMO', 'CLERICAL'] },
        ];

        links.forEach(link => {
            if (link.roles.includes('ALL') || link.roles.includes(role)) {
                const a = document.createElement('a');
                a.href = '#';
                a.className = 'nav-item';
                a.textContent = link.label;
                a.id = link.id;
                a.addEventListener('click', (e) => {
                    e.preventDefault();
                    // Basic navigation simulation
                    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
                    a.classList.add('active');
                    document.getElementById('current-page-title').textContent = link.label;
                    this.renderPage(link.id, role);
                });
                nav.appendChild(a);
            }
        });

        // Set first nav item active
        if (nav.firstElementChild) {
            nav.firstElementChild.click();
        }
    }

    static renderPage(pageId, role) {
        const contentArea = document.getElementById('content-area');
        contentArea.innerHTML = ''; // clear

        if (pageId === 'nav-dashboard') {
            this.loadDashboardData(role, contentArea);
        } else if (pageId === 'nav-patients') {
            PatientModule.render(contentArea, role);
        } else if (pageId === 'nav-cases') {
            CaseModule.render(contentArea, role);
        } else if (pageId === 'nav-evidence') {
            CaseModule.render(contentArea, role, 'EVIDENCE');
        } else if (pageId === 'nav-reports') {
            CourtReportModule.render(contentArea, role);
        } else if (pageId === 'nav-users') {
            UserModule.render(contentArea, role);
        } else if (pageId === 'nav-mlef') {
            MlefModule.render(contentArea, role);
        } else if (pageId === 'nav-mlr') {
            MlrModule.render(contentArea, role);
        } else {
            contentArea.innerHTML = `<div class="card"><div class="card-header">${pageId}</div><p>Module implementation pending...</p></div>`;
        }
    }

    static async loadDashboardData(role, container) {
        if (!container) return;
        
        container.innerHTML = '<div>Loading dashboard data...</div>';

        try {
            const casesRes = await ApiClient.get('/cases');
            const totalCases = casesRes.totalElements || (casesRes.content ? casesRes.content.length : 0);
            const recentCases = (casesRes.content || []).slice(0, 5);

            let pendingReportsCount = 0;
            if (['ADMIN', 'JMO', 'CLERICAL'].includes(role)) {
                try {
                    const reportsRes = await ApiClient.get('/court-reports?status=PENDING_COURT_DATE');
                    pendingReportsCount = reportsRes.totalElements || (reportsRes.content ? reportsRes.content.length : 0);
                } catch (e) {
                    console.error('Failed to load reports for dashboard', e);
                }
            }
            
            let html = '<div class="grid">';
            
            if (['ADMIN', 'DOCTOR', 'JMO', 'CLERICAL', 'RESEARCHER'].includes(role)) {
                html += `
                    <div class="card">
                        <div class="card-header">Total Cases</div>
                        <p style="font-size: 2rem; font-weight: 600; color: var(--primary-color)">${totalCases}</p>
                    </div>
                `;
            }

            if (['ADMIN', 'JMO', 'CLERICAL'].includes(role)) {
                html += `
                    <div class="card">
                        <div class="card-header">Pending Court Dates</div>
                        <p style="font-size: 2rem; font-weight: 600; color: var(--warning-color)">${pendingReportsCount}</p>
                    </div>
                `;
            }
            
            html += '</div>';

            html += `
                <div style="margin-top: 2rem">
                    <h3 style="margin-bottom: 1rem; font-size: 1rem;">Recent Cases</h3>
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Case ID</th>
                                    <th>Type</th>
                                    <th>Status</th>
                                    <th>Date</th>
                                </tr>
                            </thead>
                            <tbody>
            `;

            if (recentCases.length === 0) {
                html += '<tr><td colspan="4" style="text-align: center;">No cases found.</td></tr>';
            } else {
                html += recentCases.map(c => `
                    <tr>
                        <td>${c.caseNumber}</td>
                        <td>${c.caseType}</td>
                        <td><span class="badge" style="background: ${CaseModule.getStatusColor(c.caseStatus)}">${c.caseStatus}</span></td>
                        <td>${c.incidentDate || '-'}</td>
                    </tr>
                `).join('');
            }

            html += `
                            </tbody>
                        </table>
                    </div>
                </div>
            `;

            container.innerHTML = html;
        } catch (err) {
            container.innerHTML = '<div style="color:red;">Failed to load dashboard data.</div>';
            console.error(err);
        }
    }
}

// Bootstrap
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
