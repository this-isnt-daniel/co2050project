class DocumentModule {
    /**
     * Mounts the document upload UI into a specific container
     * @param {HTMLElement} container The DOM element to render into
     * @param {string} ownerType The type of entity (e.g. PATIENT, CASE)
     * @param {number} ownerId The ID of the entity
     */
    static render(container, ownerType, ownerId) {
        container.innerHTML = `
            <div style="margin-top: 1rem; border-top: 1px solid #dee2e6; padding-top: 1rem;">
                <h3 style="margin-bottom: 1rem; font-size: 1rem;">Attached Documents</h3>
                
                <div class="grid" style="gap: 1rem; margin-bottom: 1rem;">
                    <input type="file" id="doc-file-input" style="padding: 0.5rem; border: 1px solid #dee2e6; border-radius: 4px;">
                    <button class="btn" id="btn-upload-doc" style="align-self: center;">Upload File</button>
                </div>

                <div class="table-container">
                    <table id="doc-table">
                        <thead>
                            <tr>
                                <th>File Name</th>
                                <th>Type</th>
                                <th>Size</th>
                                <th>Uploaded At</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="3">Loading documents...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        `;

        document.getElementById('btn-upload-doc').addEventListener('click', async () => {
            const fileInput = document.getElementById('doc-file-input');
            if (fileInput.files.length === 0) {
                alert('Please select a file to upload.');
                return;
            }

            const file = fileInput.files[0];
            const formData = new FormData();
            formData.append('file', file);

            try {
                // The backend API expects ownerType and ownerId as RequestParams, and the file as multipart
                await ApiClient.upload(`/documents/upload?ownerType=${ownerType}&ownerId=${ownerId}`, formData);
                alert('File uploaded successfully!');
                fileInput.value = '';
                this.loadDocuments(ownerType, ownerId);
            } catch (error) {
                alert('Failed to upload file.');
            }
        });

        this.loadDocuments(ownerType, ownerId);
    }

    static formatBytes(bytes, decimals = 2) {
        if (!+bytes) return '0 Bytes';
        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
    }

    static async loadDocuments(ownerType, ownerId) {
        try {
            const response = await ApiClient.get(`/documents?ownerType=${ownerType}&ownerId=${ownerId}`);
            const tbody = document.querySelector('#doc-table tbody');
            tbody.innerHTML = '';
            
            if (response.content && response.content.length > 0) {
                response.content.forEach(doc => {
                    const tr = document.createElement('tr');
                    const uploadDate = new Date(doc.uploadedAt).toLocaleString();
                    const sizeStr = doc.fileSizeBytes ? DocumentModule.formatBytes(doc.fileSizeBytes) : 'N/A';
                    tr.innerHTML = `
                        <td><a href="#" onclick="alert('Download not fully implemented in mock yet')">${doc.fileName}</a></td>
                        <td>${doc.fileType || 'Unknown'}</td>
                        <td>${sizeStr}</td>
                        <td>${uploadDate}</td>
                    `;
                    tbody.appendChild(tr);
                });
            } else {
                tbody.innerHTML = '<tr><td colspan="4">No documents attached.</td></tr>';
            }
        } catch (error) {
            console.error(error);
            const tbody = document.querySelector('#doc-table tbody');
            tbody.innerHTML = '<tr><td colspan="4">Failed to load documents.</td></tr>';
        }
    }
}
