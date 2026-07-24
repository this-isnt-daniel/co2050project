class DailyReportModule {
    static render(container, role) {
        container.innerHTML = `
            <div class="card mb-3">
                <div class="card-header d-flex justify-content-between align-items-center" style="display:flex; justify-content:space-between; align-items:center;">
                    <span>Daily Case Report</span>
                    <input type="date" id="daily-date-picker" class="form-control" style="width: 200px;" value="${new Date().toISOString().split('T')[0]}">
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table" style="width: 100%;">
                            <thead>
                                <tr>
                                    <th style="text-align: left;">Case Number</th>
                                    <th style="text-align: left;">Type</th>
                                    <th style="text-align: left;">Incident Date</th>
                                    <th style="text-align: left;">Status</th>
                                </tr>
                            </thead>
                            <tbody id="daily-cases-list">
                                <tr><td colspan="4" style="text-align: center;">Loading daily report...</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;

        document.getElementById('daily-date-picker').addEventListener('change', (e) => {
            this.loadDailyCases(e.target.value);
        });

        this.loadDailyCases(document.getElementById('daily-date-picker').value);
    }

    static async loadDailyCases(dateStr) {
        const tbody = document.getElementById('daily-cases-list');
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">Loading...</td></tr>';
        
        try {
            // Re-using the cases endpoint. For MVP, fetch all and filter client-side.
            const res = await ApiClient.get('/cases');
            const allCases = res.content || [];
            
            const dailyCases = allCases.filter(c => c.incidentDate === dateStr);

            if (dailyCases.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No cases found for this date.</td></tr>';
                return;
            }

            tbody.innerHTML = dailyCases.map(c => `
                <tr>
                    <td>${c.caseNumber || c.id}</td>
                    <td>${c.caseType}</td>
                    <td>${c.incidentDate || '-'}</td>
                    <td><span class="badge" style="background: #e2e3e5; color: #383d41;">${c.caseStatus}</span></td>
                </tr>
            `).join('');
        } catch (err) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: red;">Failed to load daily cases.</td></tr>';
            console.error(err);
        }
    }
}
