class CourtReportModule {
    static render(container, role) {
        container.innerHTML = `
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <span>Court Reports</span>
                    ${['ADMIN', 'JMO'].includes(role) ? '<button class="btn" id="btn-new-report">Draft New Report</button>' : ''}
                </div>
                
                <div id="report-form-container" style="display: none; margin-bottom: 1rem; padding: 1rem; border: 1px solid #dee2e6; border-radius: 4px;">
                    <form id="form-report">
                        <div class="grid" style="grid-template-columns: 1fr 1fr; gap: 1rem;">
                            <div class="form-group">
                                <label>Case Selection</label>
                                <select id="rpt-case-id" required>
                                    <option value="">Loading cases...</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Report Type</label>
                                <select id="rpt-type" required>
                                    <option value="MLR">Medico-Legal Report (MLR)</option>
                                    <option value="PMR">Post-Mortem Report (PMR)</option>
                                    <option value="MLEF">Medico-Legal Examination Form (MLEF)</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Court Name (Optional)</label>
                                <input type="text" id="rpt-court">
                            </div>
                            <div class="form-group">
                                <label>Date of Trial (Optional)</label>
                                <input type="date" id="rpt-trial-date">
                            </div>
                            <div class="form-group">
                                <label>Requested Date</label>
                                <input type="date" id="rpt-req-date">
                            </div>
                            <div class="form-group">
                                <label>Court Case Number</label>
                                <input type="text" id="rpt-court-num">
                            </div>
                        </div>
                        <div style="margin-top: 1rem;">
                            <button type="submit" class="btn">Draft Report</button>
                            <button type="button" class="btn btn-secondary" onclick="document.getElementById('report-form-container').style.display='none'">Cancel</button>
                        </div>
                    </form>
                </div>

                <div class="filters" style="margin-bottom: 1rem;">
                    <select id="filter-status" class="form-control" style="width: 200px; display: inline-block;">
                        <option value="">All Statuses</option>
                        <option value="DRAFT">DRAFT</option>
                        <option value="ISSUED">ISSUED</option>
                        <option value="PENDING_COURT_DATE">PENDING_COURT_DATE</option>
                    </select>
                    <button class="btn btn-secondary" id="btn-filter" style="margin-left: 0.5rem;">Filter</button>
                </div>

                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Report #</th>
                                <th>Case Num</th>
                                <th>Type</th>
                                <th>Status</th>
                                <th>Court</th>
                                <th>Court Case #</th>
                                <th>Trial Date</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody id="report-list">
                            <tr><td colspan="7">Loading reports...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <div id="update-form-container" style="display: none; margin-top: 1rem; padding: 1rem; border: 1px solid #dee2e6; border-radius: 4px; background: #f8f9fa;">
                <form id="form-update">
                    <h4>Update Report Status</h4>
                    <input type="hidden" id="update-rpt-id">
                    <input type="hidden" id="update-rpt-case-id">
                    <input type="hidden" id="update-rpt-type">
                    <div class="grid" style="grid-template-columns: 1fr 1fr; gap: 1rem;">
                        <div class="form-group">
                            <label>Status</label>
                            <select id="update-rpt-status" required>
                                <option value="DRAFT">DRAFT</option>
                                <option value="ISSUED">ISSUED</option>
                                <option value="PENDING_COURT_DATE">PENDING_COURT_DATE</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Date of Trial (Optional)</label>
                            <input type="date" id="update-rpt-trial-date">
                        </div>
                        <div class="form-group">
                            <label>Requested Date</label>
                            <input type="date" id="update-rpt-req-date">
                        </div>
                        <div class="form-group">
                            <label>Court Case Number</label>
                            <input type="text" id="update-rpt-court-num">
                        </div>
                    </div>
                    <div style="margin-top: 1rem;">
                        <button type="submit" class="btn">Update Report</button>
                        <button type="button" class="btn btn-secondary" onclick="document.getElementById('update-form-container').style.display='none'">Cancel</button>
                    </div>
                </form>
            </div>
        `;

        if (document.getElementById('btn-new-report')) {
            document.getElementById('btn-new-report').addEventListener('click', () => {
                CourtReportModule.populateCasesDropdown();
                document.getElementById('report-form-container').style.display = 'block';
                document.getElementById('update-form-container').style.display = 'none';
            });
        }

        document.getElementById('btn-filter').addEventListener('click', () => {
            const status = document.getElementById('filter-status').value;
            this.loadReports(status);
        });

        document.getElementById('form-report').addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                caseId: parseInt(document.getElementById('rpt-case-id').value),
                reportType: document.getElementById('rpt-type').value,
                reportStatus: 'DRAFT',
                courtName: document.getElementById('rpt-court').value || null,
                dateOfTrial: document.getElementById('rpt-trial-date').value || null,
                requestedDate: document.getElementById('rpt-req-date').value || null,
                courtCaseNumber: document.getElementById('rpt-court-num').value || null,
                preparedById: AuthService.getUserInfo().userId
            };

            try {
                await ApiClient.post('/court-reports', data);
                alert('Report drafted successfully');
                document.getElementById('report-form-container').style.display = 'none';
                this.loadReports();
            } catch (err) {
                alert('Failed to draft report');
            }
        });

        document.getElementById('form-update').addEventListener('submit', async (e) => {
            e.preventDefault();
            const reportId = document.getElementById('update-rpt-id').value;
            const data = {
                caseId: parseInt(document.getElementById('update-rpt-case-id').value),
                reportType: document.getElementById('update-rpt-type').value,
                reportStatus: document.getElementById('update-rpt-status').value,
                dateOfTrial: document.getElementById('update-rpt-trial-date').value || null,
                requestedDate: document.getElementById('update-rpt-req-date').value || null,
                courtCaseNumber: document.getElementById('update-rpt-court-num').value || null
            };

            try {
                await ApiClient.put('/court-reports/' + reportId, data);
                alert('Report updated successfully');
                document.getElementById('update-form-container').style.display = 'none';
                this.loadReports();
            } catch (err) {
                alert('Failed to update report');
            }
        });

        this.loadReports();
    }

    static async populateCasesDropdown() {
        try {
            const res = await ApiClient.get('/cases');
            const cases = res.content || [];
            const select = document.getElementById('rpt-case-id');
            if (select) {
                select.innerHTML = '<option value="">-- Select a Case --</option>' + 
                    cases.map(c => `<option value="${c.id}">${c.caseNumber} - ${c.caseType}</option>`).join('');
            }
        } catch (err) {
            console.error('Failed to load cases for dropdown', err);
            const select = document.getElementById('rpt-case-id');
            if (select) select.innerHTML = '<option value="">Error loading cases</option>';
        }
    }

    static async loadReports(status = '') {
        const tbody = document.getElementById('report-list');
        tbody.innerHTML = '<tr><td colspan="7">Loading reports...</td></tr>';
        
        try {
            let url = '/court-reports';
            if (status) {
                url += '?status=' + status;
            }
            const res = await ApiClient.get(url);
            const reports = res.content || [];
            
            if (reports.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No court reports found.</td></tr>';
                return;
            }

            tbody.innerHTML = reports.map(r => {
                const color = r.reportStatus === 'ISSUED' ? '#155724' : (r.reportStatus === 'DRAFT' ? '#856404' : '#0c5460');
                const bg = r.reportStatus === 'ISSUED' ? '#d4edda' : (r.reportStatus === 'DRAFT' ? '#fff3cd' : '#d1ecf1');
                return `
                    <tr>
                        <td>${r.courtReportNumber || r.id}</td>
                        <td>${r.caseNumber || r.caseId}</td>
                        <td>${r.reportType}</td>
                        <td><span class="badge" style="background: ${bg}; color: ${color}">${r.reportStatus}</span></td>
                        <td>${r.courtName || '-'}</td>
                        <td>${r.courtCaseNumber || '-'}</td>
                        <td>${r.dateOfTrial || '-'}</td>
                        <td>
                            <button class="btn btn-sm" onclick="CourtReportModule.openUpdateForm(${r.id}, ${r.caseId}, '${r.reportType}', '${r.reportStatus}', '${r.dateOfTrial || ''}', '${r.requestedDate || ''}', '${r.courtCaseNumber || ''}')">Update</button>
                            <button class="btn btn-sm" onclick="CourtReportModule.downloadPdf(${r.caseId}, '${r.reportType}')">Download</button>
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: red;">Failed to load reports.</td></tr>';
        }
    }

    static openUpdateForm(id, caseId, type, status, trialDate, requestedDate, courtCaseNumber) {
        document.getElementById('update-rpt-id').value = id;
        document.getElementById('update-rpt-case-id').value = caseId;
        document.getElementById('update-rpt-type').value = type;
        document.getElementById('update-rpt-status').value = status;
        document.getElementById('update-rpt-trial-date').value = trialDate;
        document.getElementById('update-rpt-req-date').value = requestedDate;
        document.getElementById('update-rpt-court-num').value = courtCaseNumber;
        
        document.getElementById('update-form-container').style.display = 'block';
        document.getElementById('report-form-container').style.display = 'none';
        document.getElementById('update-form-container').scrollIntoView({ behavior: 'smooth' });
    }

    static async downloadPdf(caseId, type) {
        try {
            const token = ApiClient.getToken();
            const response = await fetch(`http://localhost:8080/api/reports/case/${caseId}?type=${type}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to generate PDF. Make sure the ' + type + ' record exists for this case.');
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = `${type}-Case-${caseId}.pdf`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (err) {
            console.error('PDF download error:', err);
            alert(err.message);
        }
    }
}
