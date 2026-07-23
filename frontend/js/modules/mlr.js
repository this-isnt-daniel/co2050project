class MlrModule {
    static render(container, role) {
        container.innerHTML = `
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <span>Medico-Legal Reports (MLR)</span>
                    ${['ADMIN', 'JMO', 'CLERICAL'].includes(role) ? `<button id="btn-new-mlr" class="btn" style="padding: 0.5rem 1rem;">+ New MLR</button>` : ''}
                </div>
                
                <div id="mlr-form-container" style="display: none; margin-top: 1rem; border-top: 1px solid #dee2e6; padding-top: 1rem;">
                    <form id="mlr-form" class="grid" style="gap: 1rem;">
                        <input type="hidden" id="mlr-id">
                        <div class="form-group">
                            <label>Case ID *</label>
                            <input type="number" id="mlr-case-id" required>
                        </div>
                        <div class="form-group">
                            <label>Examination Date</label>
                            <input type="date" id="mlr-exam-date">
                        </div>
                        <div class="form-group" style="grid-column: 1 / -1; display: flex; gap: 1rem;">
                            <button type="submit" class="btn">Save DRAFT</button>
                            <button type="button" class="btn" id="btn-cancel-mlr" style="background: #6c757d;">Cancel</button>
                        </div>
                    </form>
                </div>

                <div id="mlr-update-form-container" style="display: none; margin-top: 1rem; border-top: 1px solid #dee2e6; padding-top: 1rem; background: #fff3cd; padding: 1rem; border-radius: 4px;">
                    <form id="mlr-update-form" class="grid" style="gap: 1rem;">
                        <h4>Update / Create Revision</h4>
                        <input type="hidden" id="mlr-update-id">
                        <input type="hidden" id="mlr-update-case-id">
                        <div class="form-group">
                            <label>Examination Date</label>
                            <input type="date" id="mlr-update-exam-date">
                        </div>
                        <div class="form-group">
                            <label>Revision Reason (Optional if DRAFT)</label>
                            <input type="text" id="mlr-revision-reason" placeholder="Why is this being revised?">
                        </div>
                        <div class="form-group" style="grid-column: 1 / -1; display: flex; gap: 1rem;">
                            <button type="submit" class="btn">Save Update</button>
                            <button type="button" class="btn" id="btn-cancel-update-mlr" style="background: #6c757d;">Cancel</button>
                        </div>
                    </form>
                </div>

                <div class="table-container" style="margin-top: 1rem;">
                    <table id="mlr-table">
                        <thead>
                            <tr>
                                <th>MLR Number</th>
                                <th>Case Num</th>
                                <th>Exam Date</th>
                                <th>Status</th>
                                <th>Prepared By</th>
                                <th>Finalized Date</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="7">Loading MLR records...</td></tr>
                        </tbody>
                    </table>
                </div>

                <div id="mlr-revisions-container" style="display: none; margin-top: 2rem;">
                    <h4>Revision History</h4>
                    <table id="mlr-revisions-table">
                        <thead>
                            <tr>
                                <th>Rev #</th>
                                <th>Status at Rev</th>
                                <th>Revised By</th>
                                <th>Reason</th>
                                <th>Created At</th>
                            </tr>
                        </thead>
                        <tbody></tbody>
                    </table>
                </div>
            </div>
        `;

        if (['ADMIN', 'JMO', 'CLERICAL'].includes(role)) {
            document.getElementById('btn-new-mlr').addEventListener('click', () => {
                document.getElementById('mlr-form').reset();
                document.getElementById('mlr-id').value = '';
                document.getElementById('mlr-form-container').style.display = 'block';
                document.getElementById('mlr-update-form-container').style.display = 'none';
                document.getElementById('btn-new-mlr').style.display = 'none';
            });
            document.getElementById('btn-cancel-mlr').addEventListener('click', () => {
                document.getElementById('mlr-form-container').style.display = 'none';
                document.getElementById('btn-new-mlr').style.display = 'block';
                document.getElementById('mlr-form').reset();
            });
            document.getElementById('btn-cancel-update-mlr').addEventListener('click', () => {
                document.getElementById('mlr-update-form-container').style.display = 'none';
                document.getElementById('btn-new-mlr').style.display = 'block';
                document.getElementById('mlr-update-form').reset();
            });

            document.getElementById('mlr-form').addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.createMlr();
            });
            
            document.getElementById('mlr-update-form').addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.updateMlr();
            });
        }

        this.loadMlrs();
    }

    static async loadMlrs() {
        try {
            const response = await ApiClient.get('/mlr');
            const tbody = document.querySelector('#mlr-table tbody');
            tbody.innerHTML = '';
            document.getElementById('mlr-revisions-container').style.display = 'none';
            
            if (response.content && response.content.length > 0) {
                response.content.forEach(m => {
                    const tr = document.createElement('tr');
                    const bg = m.reportStatus === 'FINALIZED' ? '#d4edda' : '#fff3cd';
                    tr.innerHTML = `
                        <td>${m.mlrNumber || m.id}</td>
                        <td>${m.caseNumber || m.caseId}</td>
                        <td>${m.examinationDate || '-'}</td>
                        <td><span class="badge" style="background: ${bg}; color: #333">${m.reportStatus}</span></td>
                        <td>${m.preparedByName || m.preparedById || '-'}</td>
                        <td>${m.dateFinalized || '-'}</td>
                        <td>
                            <button class="btn btn-sm" onclick="MlrModule.openUpdateForm(${m.id})" style="padding: 0.2rem 0.5rem; font-size: 0.8rem;">Edit</button>
                            ${m.reportStatus !== 'FINALIZED' ? `<button class="btn btn-sm" onclick="MlrModule.finalizeMlr(${m.id})" style="padding: 0.2rem 0.5rem; font-size: 0.8rem; background: #28a745; color: white;">Finalize</button>` : ''}
                            <button class="btn btn-sm" onclick="MlrModule.viewRevisions(${m.id})" style="padding: 0.2rem 0.5rem; font-size: 0.8rem; background: #17a2b8; color: white;">History</button>
                        </td>
                    `;
                    tbody.appendChild(tr);
                });
            } else {
                tbody.innerHTML = '<tr><td colspan="7">No MLR records found.</td></tr>';
            }
        } catch (error) {
            console.error(error);
            const tbody = document.querySelector('#mlr-table tbody');
            tbody.innerHTML = '<tr><td colspan="7">Failed to load MLR records.</td></tr>';
        }
    }

    static async openUpdateForm(id) {
        try {
            const mlr = await ApiClient.get('/mlr/' + id);
            if (mlr) {
                document.getElementById('mlr-update-id').value = mlr.id;
                document.getElementById('mlr-update-case-id').value = mlr.caseId;
                document.getElementById('mlr-update-exam-date').value = mlr.examinationDate || '';
                document.getElementById('mlr-revision-reason').value = '';
                
                document.getElementById('mlr-form-container').style.display = 'none';
                document.getElementById('mlr-update-form-container').style.display = 'block';
                document.getElementById('btn-new-mlr').style.display = 'none';
                document.getElementById('mlr-update-form-container').scrollIntoView({ behavior: 'smooth' });
            }
        } catch (error) {
            alert('Failed to fetch MLR details');
        }
    }

    static async viewRevisions(id) {
        try {
            const mlr = await ApiClient.get('/mlr/' + id);
            const container = document.getElementById('mlr-revisions-container');
            const tbody = document.querySelector('#mlr-revisions-table tbody');
            tbody.innerHTML = '';
            
            if (mlr && mlr.revisions && mlr.revisions.length > 0) {
                mlr.revisions.forEach(rev => {
                    const tr = document.createElement('tr');
                    const createdAt = new Date(rev.createdAt).toLocaleString();
                    tr.innerHTML = `
                        <td>${rev.revisionNumber}</td>
                        <td>${rev.reportStatusAtRevision}</td>
                        <td>${rev.revisedByName || rev.revisedById || '-'}</td>
                        <td>${rev.revisionReason || '-'}</td>
                        <td>${createdAt}</td>
                    `;
                    tbody.appendChild(tr);
                });
                container.style.display = 'block';
                container.scrollIntoView({ behavior: 'smooth' });
            } else {
                alert('No revision history for this report.');
                container.style.display = 'none';
            }
        } catch (error) {
            alert('Failed to fetch MLR revisions');
        }
    }

    static async createMlr() {
        const payload = {
            caseId: parseInt(document.getElementById('mlr-case-id').value),
            preparedById: AuthService.getUserInfo().userId,
            examinationDate: document.getElementById('mlr-exam-date').value || null,
            reportStatus: 'DRAFT'
        };

        try {
            await ApiClient.post('/mlr', payload);
            alert('MLR DRAFT created successfully!');
            document.getElementById('mlr-form').reset();
            document.getElementById('mlr-form-container').style.display = 'none';
            document.getElementById('btn-new-mlr').style.display = 'block';
            this.loadMlrs();
        } catch (error) {
            console.error(error);
            alert('Failed to create MLR. Check console for details.');
        }
    }

    static async updateMlr() {
        const id = document.getElementById('mlr-update-id').value;
        const reason = document.getElementById('mlr-revision-reason').value;
        
        const payload = {
            caseId: parseInt(document.getElementById('mlr-update-case-id').value),
            preparedById: AuthService.getUserInfo().userId,
            examinationDate: document.getElementById('mlr-update-exam-date').value || null
        };
        
        let url = '/mlr/' + id;
        const params = [];
        if (reason) params.push('revisionReason=' + encodeURIComponent(reason));
        params.push('revisedById=' + AuthService.getUserInfo().userId);
        
        if (params.length > 0) {
            url += '?' + params.join('&');
        }

        try {
            await ApiClient.put(url, payload);
            alert('MLR updated successfully!');
            document.getElementById('mlr-update-form').reset();
            document.getElementById('mlr-update-form-container').style.display = 'none';
            document.getElementById('btn-new-mlr').style.display = 'block';
            this.loadMlrs();
        } catch (error) {
            console.error(error);
            alert('Failed to update MLR. Check console for details.');
        }
    }

    static async finalizeMlr(id) {
        if (!confirm('Are you sure you want to finalize this report? It cannot be directly edited afterwards.')) {
            return;
        }
        try {
            await ApiClient.patch('/mlr/' + id + '/finalize', {});
            alert('MLR Finalized successfully!');
            this.loadMlrs();
        } catch (error) {
            console.error(error);
            alert('Failed to finalize MLR.');
        }
    }
}
