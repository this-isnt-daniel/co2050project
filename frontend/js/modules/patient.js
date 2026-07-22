class PatientModule {
    static render(container, role) {
        container.innerHTML = `
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <span>Patient Management</span>
                    ${role !== 'RESEARCHER' ? `<button id="btn-new-patient" class="btn" style="padding: 0.5rem 1rem;">+ New Patient</button>` : ''}
                </div>
                
                <div id="patient-form-container" style="display: none; margin-top: 1rem; border-top: 1px solid #dee2e6; padding-top: 1rem;">
                    <form id="patient-form" class="grid" style="gap: 1rem;">
                        <div class="form-group">
                            <label>Full Name *</label>
                            <input type="text" id="pat-fullname" required>
                        </div>
                        <div class="form-group">
                            <label>NIC / Passport</label>
                            <input type="text" id="pat-nic">
                        </div>
                        <div class="form-group">
                            <label>Age</label>
                            <input type="number" id="pat-age" min="0" max="150">
                        </div>
                        <div class="form-group">
                            <label>Gender</label>
                            <select id="pat-gender">
                                <option value="UNKNOWN">Unknown</option>
                                <option value="MALE">Male</option>
                                <option value="FEMALE">Female</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Contact Info</label>
                            <input type="text" id="pat-contact">
                        </div>
                        <div class="form-group" style="grid-column: 1 / -1;">
                            <label>Address</label>
                            <input type="text" id="pat-address">
                        </div>
                        <div class="form-group" style="grid-column: 1 / -1; display: flex; gap: 1rem;">
                            <button type="submit" class="btn">Save Patient</button>
                            <button type="button" class="btn" id="btn-cancel-patient" style="background: #6c757d;">Cancel</button>
                        </div>
                    </form>
                </div>

                <div class="table-container" style="margin-top: 1rem;">
                    <table id="patient-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>NIC / Passport</th>
                                <th>Age</th>
                                <th>Gender</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="5">Loading patients...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        `;

        if (role !== 'RESEARCHER') {
            document.getElementById('btn-new-patient').addEventListener('click', () => {
                document.getElementById('patient-form-container').style.display = 'block';
                document.getElementById('btn-new-patient').style.display = 'none';
            });
            document.getElementById('btn-cancel-patient').addEventListener('click', () => {
                document.getElementById('patient-form-container').style.display = 'none';
                document.getElementById('btn-new-patient').style.display = 'block';
                document.getElementById('patient-form').reset();
            });

            document.getElementById('patient-form').addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.savePatient();
            });
        }

        this.loadPatients();
    }

    static async loadPatients() {
        try {
            const response = await ApiClient.get('/patients');
            const tbody = document.querySelector('#patient-table tbody');
            tbody.innerHTML = '';
            
            if (response.content && response.content.length > 0) {
                response.content.forEach(p => {
                    // Researcher gets deidentified data (no names/NIC)
                    const name = p.fullName || '[RESTRICTED]';
                    const nic = p.nicPassportNo || '[RESTRICTED]';
                    const age = p.age !== null ? p.age : 'N/A';
                    
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${p.id}</td>
                        <td>${name}</td>
                        <td>${nic}</td>
                        <td>${age}</td>
                        <td>${p.gender || 'UNKNOWN'}</td>
                    `;
                    tbody.appendChild(tr);
                });
            } else {
                tbody.innerHTML = '<tr><td colspan="5">No patients found.</td></tr>';
            }
        } catch (error) {
            console.error(error);
            alert('Failed to load patients.');
        }
    }

    static async savePatient() {
        const payload = {
            fullName: document.getElementById('pat-fullname').value,
            nicPassportNo: document.getElementById('pat-nic').value,
            age: document.getElementById('pat-age').value ? parseInt(document.getElementById('pat-age').value) : null,
            gender: document.getElementById('pat-gender').value,
            contactInfo: document.getElementById('pat-contact').value,
            address: document.getElementById('pat-address').value
        };

        try {
            await ApiClient.post('/patients', payload);
            alert('Patient registered successfully!');
            document.getElementById('patient-form').reset();
            document.getElementById('patient-form-container').style.display = 'none';
            document.getElementById('btn-new-patient').style.display = 'block';
            this.loadPatients();
        } catch (error) {
            console.error(error);
            alert('Failed to save patient. Check console for details.');
        }
    }
}
