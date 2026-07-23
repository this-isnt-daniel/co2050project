class MlefModule {
    static render(container, role) {
        container.innerHTML = `
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <span>MLEF Referrals</span>
                    ${['ADMIN', 'DOCTOR', 'JMO'].includes(role) ? `<button id="btn-new-mlef" class="btn" style="padding: 0.5rem 1rem;">+ New MLEF</button>` : ''}
                </div>
                
                <div id="mlef-form-container" style="display: none; margin-top: 1rem; border-top: 1px solid #dee2e6; padding-top: 1rem;">
                    <form id="mlef-form" class="grid" style="gap: 1rem;">
                        <input type="hidden" id="mlef-id">
                        <div class="form-group">
                            <label>Case ID *</label>
                            <input type="number" id="mlef-case-id" required>
                        </div>
                        <div class="form-group">
                            <label>Examining Doctor ID</label>
                            <input type="number" id="mlef-doc-id">
                        </div>
                        <div class="form-group">
                            <label>Issue Date</label>
                            <input type="date" id="mlef-issue-date">
                        </div>
                        <div class="form-group">
                            <label>Received Date</label>
                            <input type="date" id="mlef-received-date">
                        </div>
                        <div class="form-group">
                            <label>Referring Hospital</label>
                            <input type="text" id="mlef-hospital">
                        </div>
                        <div class="form-group">
                            <label>Referring Medical Officer</label>
                            <input type="text" id="mlef-officer">
                        </div>
                        <div class="form-group">
                            <label>Police Station</label>
                            <input type="text" id="mlef-police-station">
                        </div>
                        <div class="form-group">
                            <label>Police Reference</label>
                            <input type="text" id="mlef-police-ref">
                        </div>
                        <div class="form-group">
                            <label>Case Reference</label>
                            <input type="text" id="mlef-case-ref">
                        </div>
                        <div class="form-group" style="grid-column: 1 / -1; display: flex; gap: 1rem;">
                            <button type="submit" class="btn">Save MLEF</button>
                            <button type="button" class="btn" id="btn-cancel-mlef" style="background: #6c757d;">Cancel</button>
                        </div>
                    </form>
                </div>

                <div class="table-container" style="margin-top: 1rem;">
                    <table id="mlef-table">
                        <thead>
                            <tr>
                                <th>MLEF Number</th>
                                <th>Case ID</th>
                                <th>Issue Date</th>
                                <th>Received Date</th>
                                <th>Police Station</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="7">Loading MLEF records...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        `;

        if (['ADMIN', 'DOCTOR', 'JMO'].includes(role)) {
            document.getElementById('btn-new-mlef').addEventListener('click', () => {
                document.getElementById('mlef-form').reset();
                document.getElementById('mlef-id').value = '';
                document.getElementById('mlef-form-container').style.display = 'block';
                document.getElementById('btn-new-mlef').style.display = 'none';
            });
            document.getElementById('btn-cancel-mlef').addEventListener('click', () => {
                document.getElementById('mlef-form-container').style.display = 'none';
                document.getElementById('btn-new-mlef').style.display = 'block';
                document.getElementById('mlef-form').reset();
            });

            document.getElementById('mlef-form').addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.saveMlef();
            });
        }

        this.loadMlefs();
    }

    static async loadMlefs() {
        try {
            const response = await ApiClient.get('/mlef');
            const tbody = document.querySelector('#mlef-table tbody');
            tbody.innerHTML = '';
            
            if (response.content && response.content.length > 0) {
                response.content.forEach(m => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${m.mlefNumber || m.id}</td>
                        <td>${m.caseNumber || m.caseId}</td>
                        <td>${m.dateOfIssue || '-'}</td>
                        <td>${m.receivedDate || '-'}</td>
                        <td>${m.policeStation || '-'}</td>
                        <td>${m.reportStatus || 'DRAFT'}</td>
                        <td>
                            <button class="btn btn-sm" onclick="MlefModule.openUpdateForm(${m.id})" style="padding: 0.2rem 0.5rem; font-size: 0.8rem;">Edit</button>
                        </td>
                    `;
                    tbody.appendChild(tr);
                });
            } else {
                tbody.innerHTML = '<tr><td colspan="7">No MLEF records found.</td></tr>';
            }
        } catch (error) {
            console.error(error);
            const tbody = document.querySelector('#mlef-table tbody');
            tbody.innerHTML = '<tr><td colspan="7">Failed to load MLEF records.</td></tr>';
        }
    }

    static async openUpdateForm(id) {
        try {
            const mlef = await ApiClient.get('/mlef/' + id);
            if (mlef) {
                document.getElementById('mlef-id').value = mlef.id;
                document.getElementById('mlef-case-id').value = mlef.caseId;
                document.getElementById('mlef-doc-id').value = mlef.examiningDoctorId || '';
                document.getElementById('mlef-issue-date').value = mlef.dateOfIssue || '';
                document.getElementById('mlef-received-date').value = mlef.receivedDate || '';
                document.getElementById('mlef-hospital').value = mlef.referringHospital || '';
                document.getElementById('mlef-officer').value = mlef.referringMedicalOfficer || '';
                document.getElementById('mlef-police-station').value = mlef.policeStation || '';
                document.getElementById('mlef-police-ref').value = mlef.policeReference || '';
                document.getElementById('mlef-case-ref').value = mlef.caseReference || '';
                
                document.getElementById('mlef-form-container').style.display = 'block';
                document.getElementById('btn-new-mlef').style.display = 'none';
                document.getElementById('mlef-form-container').scrollIntoView({ behavior: 'smooth' });
            }
        } catch (error) {
            alert('Failed to fetch MLEF details');
        }
    }

    static async saveMlef() {
        const id = document.getElementById('mlef-id').value;
        const payload = {
            caseId: parseInt(document.getElementById('mlef-case-id').value),
            examiningDoctorId: document.getElementById('mlef-doc-id').value ? parseInt(document.getElementById('mlef-doc-id').value) : null,
            dateOfIssue: document.getElementById('mlef-issue-date').value || null,
            receivedDate: document.getElementById('mlef-received-date').value || null,
            referringHospital: document.getElementById('mlef-hospital').value || null,
            referringMedicalOfficer: document.getElementById('mlef-officer').value || null,
            policeStation: document.getElementById('mlef-police-station').value || null,
            policeReference: document.getElementById('mlef-police-ref').value || null,
            caseReference: document.getElementById('mlef-case-ref').value || null
        };

        try {
            if (id) {
                await ApiClient.put('/mlef/' + id, payload);
            } else {
                await ApiClient.post('/mlef', payload);
            }
            alert('MLEF saved successfully!');
            document.getElementById('mlef-form').reset();
            document.getElementById('mlef-form-container').style.display = 'none';
            document.getElementById('btn-new-mlef').style.display = 'block';
            this.loadMlefs();
        } catch (error) {
            console.error(error);
            alert('Failed to save MLEF. Check console for details.');
        }
    }
}
