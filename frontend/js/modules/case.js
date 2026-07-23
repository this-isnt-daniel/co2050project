class CaseModule {
    static render(container, role, mode = 'DEFAULT') {
        CaseModule.currentMode = mode;
        const title = mode === 'EVIDENCE' ? 'Chain of Custody - Select Case' : 'Case Management';
        container.innerHTML = `
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <span>${title}</span>
                    ${mode !== 'EVIDENCE' && ['ADMIN', 'DOCTOR', 'JMO'].includes(role) ? `<button id="btn-new-case" class="btn" style="padding: 0.5rem 1rem;">+ New Case</button>` : ''}
                </div>
                
                <div id="case-form-container" style="display: none; margin-top: 1rem; border-top: 1px solid #dee2e6; padding-top: 1rem;">
                    <form id="case-form" class="grid" style="gap: 1rem;">
                        <div class="form-group">
                            <label>Case Number *</label>
                            <input type="text" id="case-num" required placeholder="e.g. CW/01/24">
                        </div>
                        <div class="form-group">
                            <label>Case Type</label>
                            <select id="case-type">
                                <option value="CLINICAL">Clinical</option>
                                <option value="AUTOPSY">Autopsy</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Patient ID</label>
                            <div style="display: flex; gap: 0.5rem; align-items: center;">
                                <input type="number" id="case-pat-id" placeholder="ID of registered patient" style="flex: 1;">
                                <button type="button" class="btn" id="btn-check-patient" style="padding: 0.5rem;">Check</button>
                            </div>
                            <small id="patient-name-display" style="color: var(--primary-color); font-weight: bold; margin-top: 0.25rem; display: block;"></small>
                        </div>
                        <div class="form-group">
                            <label>Incident Date</label>
                            <input type="date" id="case-date">
                        </div>
                        <div class="form-group">
                            <label>Referred By</label>
                            <select id="case-referred">
                                <option value="POLICE">Police</option>
                                <option value="HOSPITAL">Hospital</option>
                                <option value="COURT">Court</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Referring Authority</label>
                            <input type="text" id="case-auth" placeholder="e.g. Cinnamon Gardens Police">
                        </div>
                        <div class="form-group">
                            <label>Assigned Doctor</label>
                            <select id="case-doc-id">
                                <option value="">-- Select Doctor (Optional) --</option>
                            </select>
                        </div>
                        <div class="form-group" style="grid-column: 1 / -1; display: flex; gap: 1rem;">
                            <button type="submit" class="btn">Save Case</button>
                            <button type="button" class="btn" id="btn-cancel-case" style="background: #6c757d;">Cancel</button>
                        </div>
                    </form>
                </div>

                <div class="table-container" style="margin-top: 1rem;">
                    <table id="case-table">
                        <thead>
                            <tr>
                                <th>Case #</th>
                                <th>Type</th>
                                <th>Patient ID</th>
                                <th>Incident Date</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="6">Loading cases...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        `;

        if (mode !== 'EVIDENCE' && ['ADMIN', 'DOCTOR', 'JMO'].includes(role)) {
            document.getElementById('btn-new-case').addEventListener('click', () => {
                CaseModule.populateDropdowns();
                document.getElementById('case-form-container').style.display = 'block';
                document.getElementById('btn-new-case').style.display = 'none';
                
                // Auto-recommend case number based on default type
                CaseModule.recommendCaseNumber(document.getElementById('case-type').value);
            });
            document.getElementById('btn-cancel-case').addEventListener('click', () => {
                document.getElementById('case-form-container').style.display = 'none';
                document.getElementById('btn-new-case').style.display = 'block';
                document.getElementById('case-form').reset();
            });

            document.getElementById('case-type').addEventListener('change', (e) => {
                CaseModule.recommendCaseNumber(e.target.value);
            });

            document.getElementById('btn-check-patient').addEventListener('click', async () => {
                const id = document.getElementById('case-pat-id').value;
                const display = document.getElementById('patient-name-display');
                if (!id) {
                    display.innerText = 'Please enter a Patient ID.';
                    display.style.color = 'red';
                    return;
                }
                display.innerText = 'Checking...';
                display.style.color = 'var(--text-main)';
                try {
                    const patient = await ApiClient.get('/patients/' + id);
                    if (patient && patient.fullName) {
                        display.innerText = `Verified: ${patient.fullName}`;
                        display.style.color = 'green';
                    } else {
                        display.innerText = 'Patient not found.';
                        display.style.color = 'red';
                    }
                } catch (e) {
                    display.innerText = 'Patient not found.';
                    display.style.color = 'red';
                }
            });

            document.getElementById('case-form').addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.saveCase();
            });
        }

        this.loadCases();
    }

    static async recommendCaseNumber(type) {
        const year = new Date().getFullYear().toString().slice(-2);
        const prefix = type === 'CLINICAL' ? 'CW' : 'PM';
        let nextNum = 1;

        try {
            const res = await ApiClient.get('/cases');
            const cases = res.content || [];
            
            // Filter cases by the same prefix and year
            const matchingCases = cases.filter(c => c.caseNumber && c.caseNumber.startsWith(`${prefix}/`) && c.caseNumber.endsWith(`/${year}`));
            
            if (matchingCases.length > 0) {
                // Extract the middle numbers (x) from CW/x/24
                const numbers = matchingCases.map(c => {
                    const parts = c.caseNumber.split('/');
                    if (parts.length === 3) {
                        return parseInt(parts[1], 10);
                    }
                    return 0;
                }).filter(n => !isNaN(n));
                
                if (numbers.length > 0) {
                    nextNum = Math.max(...numbers) + 1;
                }
            }
        } catch (e) {
            console.error('Failed to fetch cases for numbering', e);
        }

        const paddedNum = nextNum.toString().padStart(3, '0');
        document.getElementById('case-num').value = `${prefix}/${paddedNum}/${year}`;
    }

    static async populateDropdowns() {
        try {
            // Fetch doctors from Staff
            const uRes = await ApiClient.get('/staff');
            const staffList = uRes.content || [];
            const dSelect = document.getElementById('case-doc-id');
            if (dSelect) {
                const doctors = staffList.filter(s => s.staffRole === 'DOCTOR' || s.staffRole === 'JMO');
                dSelect.innerHTML = '<option value="">-- Select Doctor (Optional) --</option>' + 
                    doctors.map(d => `<option value="${d.id}">${d.name} (${d.staffRole})</option>`).join('');
            }
        } catch (e) {
            console.error('Failed to load doctors for dropdown');
        }
    }

    static async loadCases() {
        try {
            const response = await ApiClient.get('/cases');
            const tbody = document.querySelector('#case-table tbody');
            tbody.innerHTML = '';
            
            if (response.content && response.content.length > 0) {
                response.content.forEach(c => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${c.caseNumber}</td>
                        <td>${c.caseType}</td>
                        <td>${c.patientId || '-'}</td>
                        <td>${c.incidentDate || '-'}</td>
                        <td><span class="badge" style="background: ${this.getStatusColor(c.caseStatus)}">${c.caseStatus}</span></td>
                        <td>
                            <button class="btn btn-sm" data-action="view-case" data-id="${c.id}" data-casenum="${c.caseNumber}" data-patientname="${c.patientName || ''}" style="padding: 0.2rem 0.5rem; font-size: 0.8rem;">
                                ${CaseModule.currentMode === 'EVIDENCE' ? 'Manage Evidence' : 'View'}
                            </button>
                        </td>
                    `;
                    tbody.appendChild(tr);
                });

                // Attach event listeners to view buttons
                document.querySelectorAll('button[data-action="view-case"]').forEach(btn => {
                    btn.addEventListener('click', (e) => {
                        const caseId = e.target.getAttribute('data-id');
                        const caseNum = e.target.getAttribute('data-casenum');
                        const patName = e.target.getAttribute('data-patientname');
                        this.renderCaseDetails(caseId, caseNum, patName);
                    });
                });
            } else {
                tbody.innerHTML = '<tr><td colspan="6">No cases found.</td></tr>';
            }
        } catch (error) {
            console.error(error);
            alert('Failed to load cases.');
        }
    }

    static renderCaseDetails(caseId, caseNum, patientName) {
        const container = document.getElementById('content-area');
        const patDisplay = patientName ? ` - Patient: ${patientName}` : '';
        container.innerHTML = `
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <span>Case Details: ${caseNum}${patDisplay}</span>
                    <button class="btn" onclick="CaseModule.render(document.getElementById('content-area'), AuthService.getUserInfo().role || 'USER')" style="padding: 0.5rem 1rem;">Back to Cases</button>
                </div>
                
                <div id="case-documents-container"></div>
                <div id="case-evidence-container"></div>
                <div id="case-labtest-container"></div>
            </div>
        `;

        if (CaseModule.currentMode === 'EVIDENCE') {
            // Only render evidence module
            EvidenceModule.render(document.getElementById('case-evidence-container'), parseInt(caseId));
        } else {
            // Render all modules
            DocumentModule.render(document.getElementById('case-documents-container'), 'CASE', parseInt(caseId));
            EvidenceModule.render(document.getElementById('case-evidence-container'), parseInt(caseId));
            LabTestModule.render(document.getElementById('case-labtest-container'), parseInt(caseId));
        }
    }

    static getStatusColor(status) {
        switch(status) {
            case 'OPEN': return '#007bff';
            case 'IN_PROGRESS': return '#ffc107';
            case 'REPORT_DRAFTED': return '#17a2b8';
            case 'SUBMITTED': return '#28a745';
            case 'CLOSED': return '#6c757d';
            default: return '#6c757d';
        }
    }

    static async saveCase() {
        const payload = {
            caseNumber: document.getElementById('case-num').value,
            caseType: document.getElementById('case-type').value,
            patientId: document.getElementById('case-pat-id').value ? parseInt(document.getElementById('case-pat-id').value) : null,
            incidentDate: document.getElementById('case-date').value || null,
            referredBy: document.getElementById('case-referred').value,
            referringAuthority: document.getElementById('case-auth').value,
            assignedDoctorId: document.getElementById('case-doc-id').value ? parseInt(document.getElementById('case-doc-id').value) : null
        };

        try {
            await ApiClient.post('/cases', payload);
            alert('Case created successfully!');
            document.getElementById('case-form').reset();
            document.getElementById('case-form-container').style.display = 'none';
            if(document.getElementById('btn-new-case')) document.getElementById('btn-new-case').style.display = 'block';
            this.loadCases();
        } catch (error) {
            console.error(error);
            alert('Failed to save case. Check console for details.');
        }
    }
}
