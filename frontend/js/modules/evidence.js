class EvidenceModule {
    static render(container, caseId) {
        container.innerHTML = `
            <div style="margin-top: 1rem; border-top: 1px solid #dee2e6; padding-top: 1rem;">
                <h3 style="margin-bottom: 1rem; font-size: 1rem;">Evidence & Chain of Custody</h3>
                
                <div class="grid" style="gap: 1rem; margin-bottom: 1rem;">
                    <button class="btn" id="btn-new-evidence" style="align-self: center;">+ Record New Evidence</button>
                </div>

                <div id="evidence-form-container" style="display: none; margin-bottom: 1rem; padding: 1rem; border: 1px solid #dee2e6; border-radius: 4px;">
                    <form id="form-evidence">
                        <div class="form-group">
                            <label>Evidence Type</label>
                            <input type="text" id="ev-type" required placeholder="e.g. DNA Swab, Weapon">
                        </div>
                        <div class="form-group">
                            <label>Description</label>
                            <input type="text" id="ev-desc" required>
                        </div>
                        <div class="form-group">
                            <label>Storage Location</label>
                            <input type="text" id="ev-loc">
                        </div>
                        <div style="margin-top: 1rem;">
                            <button type="submit" class="btn">Save Evidence</button>
                            <button type="button" class="btn btn-secondary" onclick="document.getElementById('evidence-form-container').style.display='none'">Cancel</button>
                        </div>
                    </form>
                </div>

                <div id="transfer-form-container" style="display: none; margin-bottom: 1rem; padding: 1rem; border: 1px solid #dee2e6; border-radius: 4px;">
                    <form id="form-transfer">
                        <h4>Transfer Custody</h4>
                        <input type="hidden" id="transfer-ev-id">
                        <div class="form-group">
                            <label>Transfer To (Staff ID)</label>
                            <input type="number" id="transfer-to" required>
                        </div>
                        <div class="form-group">
                            <label>Reason</label>
                            <input type="text" id="transfer-reason" required>
                        </div>
                        <div style="margin-top: 1rem;">
                            <button type="submit" class="btn">Record Transfer</button>
                            <button type="button" class="btn btn-secondary" onclick="document.getElementById('transfer-form-container').style.display='none'">Cancel</button>
                        </div>
                    </form>
                </div>

                <div id="evidence-list">
                    Loading evidence...
                </div>
            </div>
        `;

        document.getElementById('btn-new-evidence').addEventListener('click', () => {
            document.getElementById('evidence-form-container').style.display = 'block';
            document.getElementById('transfer-form-container').style.display = 'none';
        });

        document.getElementById('form-evidence').addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                caseId: parseInt(caseId),
                evidenceType: document.getElementById('ev-type').value,
                description: document.getElementById('ev-desc').value,
                storageLocation: document.getElementById('ev-loc').value,
                collectedById: AuthService.getUserInfo().userId,
                collectedAt: new Date().toISOString()
            };

            try {
                await ApiClient.post('/evidence', data);
                alert('Evidence recorded successfully');
                document.getElementById('evidence-form-container').style.display = 'none';
                this.loadEvidence(caseId);
            } catch (err) {
                alert('Failed to save evidence');
            }
        });

        document.getElementById('form-transfer').addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                evidenceId: parseInt(document.getElementById('transfer-ev-id').value),
                transferredFromId: AuthService.getUserInfo().userId,
                transferredToId: parseInt(document.getElementById('transfer-to').value),
                reason: document.getElementById('transfer-reason').value
            };

            try {
                await ApiClient.post('/evidence/custody-transfer', data);
                alert('Custody transfer recorded successfully');
                document.getElementById('transfer-form-container').style.display = 'none';
                this.loadEvidence(caseId);
            } catch (err) {
                alert('Failed to record transfer');
            }
        });

        this.loadEvidence(caseId);
    }

    static async loadEvidence(caseId) {
        const container = document.getElementById('evidence-list');
        try {
            const res = await ApiClient.get('/evidence/case/' + caseId);
            const evs = res.content || [];
            if (evs.length === 0) {
                container.innerHTML = '<p>No evidence found for this case.</p>';
                return;
            }

            container.innerHTML = evs.map(ev => {
                const logs = (ev.custodyLog || []).map(l => 
                    `<li>${new Date(l.transferTimestamp).toLocaleString()}: Transferred to <b>${l.transferredToName || 'Unknown'}</b> - ${l.reason}</li>`
                ).join('');

                return `
                <div class="card" style="margin-bottom: 1rem; border: 1px solid #dee2e6; box-shadow: none;">
                    <div style="display: flex; justify-content: space-between;">
                        <strong>${ev.evidenceType}</strong>
                        <button class="btn btn-sm" onclick="EvidenceModule.openTransferForm(${ev.id})">Transfer Custody</button>
                    </div>
                    <p style="margin: 0.5rem 0; font-size: 0.9rem;">${ev.description} (Loc: ${ev.storageLocation || 'N/A'})</p>
                    <ul style="font-size: 0.8rem; background: #f8f9fa; padding: 0.5rem 1rem; list-style-type: square; margin-left: 1rem;">
                        ${logs}
                    </ul>
                </div>
                `;
            }).join('');
        } catch (err) {
            container.innerHTML = '<p>Failed to load evidence.</p>';
        }
    }

    static openTransferForm(evidenceId) {
        document.getElementById('transfer-ev-id').value = evidenceId;
        document.getElementById('transfer-form-container').style.display = 'block';
        document.getElementById('evidence-form-container').style.display = 'none';
        // Scroll into view
        document.getElementById('transfer-form-container').scrollIntoView({ behavior: 'smooth' });
    }
}
