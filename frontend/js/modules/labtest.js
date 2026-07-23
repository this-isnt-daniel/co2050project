class LabTestModule {
    static render(container, caseId) {
        container.innerHTML = `
            <div style="margin-top: 1rem; border-top: 1px solid #dee2e6; padding-top: 1rem;">
                <h3 style="margin-bottom: 1rem; font-size: 1rem;">Laboratory Tests</h3>
                
                <div class="grid" style="gap: 1rem; margin-bottom: 1rem;">
                    <button class="btn" id="btn-new-labtest" style="align-self: center;">+ Request Lab Test</button>
                </div>

                <div id="labtest-form-container" style="display: none; margin-bottom: 1rem; padding: 1rem; border: 1px solid #dee2e6; border-radius: 4px;">
                    <form id="form-labtest">
                        <div class="form-group">
                            <label>Test Type</label>
                            <input type="text" id="lt-type" required placeholder="e.g. Toxicology, DNA">
                        </div>
                        <div style="margin-top: 1rem;">
                            <button type="submit" class="btn">Submit Request</button>
                            <button type="button" class="btn btn-secondary" onclick="document.getElementById('labtest-form-container').style.display='none'">Cancel</button>
                        </div>
                    </form>
                </div>

                <div id="result-form-container" style="display: none; margin-bottom: 1rem; padding: 1rem; border: 1px solid #dee2e6; border-radius: 4px; background: #f8f9fa;">
                    <form id="form-result">
                        <h4>Update Test Result</h4>
                        <input type="hidden" id="result-lt-id">
                        <div class="form-group">
                            <label>Result Summary</label>
                            <textarea id="lt-result-text" required rows="3" style="width:100%; border:1px solid #dee2e6; border-radius:4px; padding:0.5rem;"></textarea>
                        </div>
                        <div style="margin-top: 1rem;">
                            <button type="submit" class="btn">Save Result</button>
                            <button type="button" class="btn btn-secondary" onclick="document.getElementById('result-form-container').style.display='none'">Cancel</button>
                        </div>
                    </form>
                </div>

                <div id="labtest-list">
                    Loading lab tests...
                </div>
            </div>
        `;

        document.getElementById('btn-new-labtest').addEventListener('click', () => {
            document.getElementById('labtest-form-container').style.display = 'block';
            document.getElementById('result-form-container').style.display = 'none';
        });

        document.getElementById('form-labtest').addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                caseId: parseInt(caseId),
                testType: document.getElementById('lt-type').value,
                requestedById: AuthService.getUserInfo().userId
            };

            try {
                await ApiClient.post('/lab-tests', data);
                alert('Lab test requested successfully');
                document.getElementById('labtest-form-container').style.display = 'none';
                this.loadLabTests(caseId);
            } catch (err) {
                alert('Failed to request lab test');
            }
        });

        document.getElementById('form-result').addEventListener('submit', async (e) => {
            e.preventDefault();
            const testId = document.getElementById('result-lt-id').value;
            const resultText = document.getElementById('lt-result-text').value;
            const dateStr = new Date().toISOString().split('T')[0];

            try {
                await ApiClient.patch(\`/lab-tests/\${testId}/result?result=\${encodeURIComponent(resultText)}&resultDate=\${dateStr}\`);
                alert('Result updated successfully');
                document.getElementById('result-form-container').style.display = 'none';
                this.loadLabTests(caseId);
            } catch (err) {
                alert('Failed to update result');
            }
        });

        this.loadLabTests(caseId);
    }

    static async loadLabTests(caseId) {
        const container = document.getElementById('labtest-list');
        try {
            const res = await ApiClient.get('/lab-tests/case/' + caseId);
            const tests = res.content || [];
            if (tests.length === 0) {
                container.innerHTML = '<p>No lab tests found for this case.</p>';
                return;
            }

            container.innerHTML = tests.map(t => {
                const hasResult = t.result !== null && t.result !== undefined && t.result.trim() !== '';
                return `
                <div class="card" style="margin-bottom: 1rem; border: 1px solid #dee2e6; box-shadow: none;">
                    <div style="display: flex; justify-content: space-between;">
                        <strong>${t.testType}</strong>
                        <span class="badge" style="background: ${hasResult ? '#d4edda' : '#fff3cd'}; color: ${hasResult ? '#155724' : '#856404'};">
                            ${hasResult ? 'COMPLETED' : 'PENDING'}
                        </span>
                    </div>
                    <p style="margin: 0.5rem 0; font-size: 0.9rem;">Requested By: ${t.requestedByName || 'Unknown'}</p>
                    ${hasResult ? 
                        `<div style="margin-top:0.5rem; padding:0.5rem; background:#f8f9fa; font-size:0.9rem;">
                            <strong>Result (Date: ${t.resultDate}):</strong> ${t.result}
                         </div>` 
                        : 
                        `<button class="btn btn-sm" style="margin-top: 0.5rem;" onclick="LabTestModule.openResultForm(${t.id})">Update Result</button>`
                    }
                </div>
                `;
            }).join('');
        } catch (err) {
            container.innerHTML = '<p>Failed to load lab tests.</p>';
        }
    }

    static openResultForm(testId) {
        document.getElementById('result-lt-id').value = testId;
        document.getElementById('result-form-container').style.display = 'block';
        document.getElementById('labtest-form-container').style.display = 'none';
        document.getElementById('result-form-container').scrollIntoView({ behavior: 'smooth' });
    }
}
