class UserModule {
    static render(container, role) {
        if (role !== 'ADMIN') {
            container.innerHTML = '<div class="card"><p style="color:red;">Unauthorized Access</p></div>';
            return;
        }

        container.innerHTML = `
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <span>User Management</span>
                    <button class="btn" id="btn-new-user">Create New User</button>
                </div>
                
                <div id="user-form-container" style="display: none; margin-bottom: 1rem; padding: 1rem; border: 1px solid #dee2e6; border-radius: 4px;">
                    <form id="form-user">
                        <input type="hidden" id="user-id">
                        <div class="grid" style="grid-template-columns: 1fr 1fr; gap: 1rem;">
                            <div class="form-group">
                                <label>Username *</label>
                                <input type="text" id="user-username" required>
                            </div>
                            <div class="form-group">
                                <label>Password <span id="pwd-help" style="font-weight:normal; font-size:0.8rem; color:#6c757d;"></span></label>
                                <input type="password" id="user-password">
                            </div>
                            <div class="form-group">
                                <label>Role *</label>
                                <select id="user-role" required>
                                    <option value="ADMIN">ADMIN</option>
                                    <option value="DOCTOR">DOCTOR</option>
                                    <option value="JMO">JMO</option>
                                    <option value="LAB_STAFF">LAB_STAFF</option>
                                    <option value="CLERICAL">CLERICAL</option>
                                    <option value="RESEARCHER">RESEARCHER</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Link Existing Staff ID (Optional)</label>
                                <input type="number" id="user-staff-id">
                            </div>
                            <div class="form-group">
                                <label>OR Create New Staff: Name</label>
                                <input type="text" id="user-staff-name" placeholder="Leave blank if using ID">
                            </div>
                        </div>
                        <div style="margin-top: 1rem;">
                            <button type="submit" class="btn" id="btn-save-user">Save User</button>
                            <button type="button" class="btn btn-secondary" onclick="document.getElementById('user-form-container').style.display='none'">Cancel</button>
                        </div>
                    </form>
                </div>

                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Username</th>
                                <th>Role</th>
                                <th>Staff Name</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody id="user-list">
                            <tr><td colspan="6">Loading users...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        `;

        document.getElementById('btn-new-user').addEventListener('click', () => {
            document.getElementById('form-user').reset();
            document.getElementById('user-id').value = '';
            document.getElementById('user-staff-name').disabled = false;
            document.getElementById('pwd-help').innerText = '(Required, min 8 chars)';
            document.getElementById('user-form-container').style.display = 'block';
        });

        document.getElementById('form-user').addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('user-id').value;
            let staffId = document.getElementById('user-staff-id').value ? parseInt(document.getElementById('user-staff-id').value) : null;
            const staffName = document.getElementById('user-staff-name').value;
            const role = document.getElementById('user-role').value;

            try {
                // If creating a new staff member
                if (staffName && !staffId) {
                    const staffRes = await ApiClient.post('/staff', {
                        name: staffName,
                        staffRole: ['ADMIN', 'CLERICAL', 'LAB_STAFF', 'DOCTOR', 'JMO'].includes(role) ? role : 'ADMIN' // Fallback
                    });
                    staffId = staffRes.id;
                }

                const data = {
                    username: document.getElementById('user-username').value,
                    userRole: role,
                    password: document.getElementById('user-password').value,
                    staffId: staffId
                };

                if (id) {
                    await ApiClient.put('/users/' + id, data);
                    alert('User updated successfully');
                } else {
                    await ApiClient.post('/users', data);
                    alert('User created successfully');
                }
                document.getElementById('user-form-container').style.display = 'none';
                this.loadUsers();
            } catch (err) {
                alert('Failed to save user. Check console for details.');
                console.error(err);
            }
        });

        this.loadUsers();
    }

    static async loadUsers() {
        const tbody = document.getElementById('user-list');
        try {
            const res = await ApiClient.get('/users');
            const users = res.content || [];
            
            if (users.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">No users found.</td></tr>';
                return;
            }

            tbody.innerHTML = users.map(u => {
                const statusColor = u.isActive ? '#28a745' : '#dc3545';
                return `
                    <tr>
                        <td>${u.id}</td>
                        <td>${u.username}</td>
                        <td>${u.userRole}</td>
                        <td>${u.staffName || '-'}</td>
                        <td style="color: ${statusColor}; font-weight: bold;">${u.isActive ? 'ACTIVE' : 'INACTIVE'}</td>
                        <td>
                            <button class="btn btn-sm" onclick="UserModule.openEditForm(${u.id}, '${u.username}', '${u.userRole}', ${u.staffId || null}, '${u.staffName || ''}')" style="padding: 0.2rem 0.5rem; font-size: 0.8rem;">Edit</button>
                            ${u.isActive ? `<button class="btn btn-sm btn-secondary" onclick="UserModule.deactivateUser(${u.id})" style="padding: 0.2rem 0.5rem; font-size: 0.8rem; margin-left: 0.5rem;">Deactivate</button>` : ''}
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: red;">Failed to load users.</td></tr>';
            console.error(err);
        }
    }

    static openEditForm(id, username, role, staffId, staffName) {
        document.getElementById('form-user').reset();
        document.getElementById('user-id').value = id;
        document.getElementById('user-username').value = username;
        document.getElementById('user-role').value = role;
        document.getElementById('user-staff-id').value = staffId || '';
        document.getElementById('user-staff-name').value = staffName || '';
        document.getElementById('user-staff-name').disabled = !!staffId; // Disable if already linked
        document.getElementById('pwd-help').innerText = '(Leave blank to keep unchanged)';
        
        document.getElementById('user-form-container').style.display = 'block';
        document.getElementById('user-form-container').scrollIntoView({ behavior: 'smooth' });
    }

    static async deactivateUser(id) {
        if (!confirm('Are you sure you want to deactivate this user?')) return;
        try {
            await ApiClient.patch('/users/' + id + '/deactivate');
            alert('User deactivated successfully');
            this.loadUsers();
        } catch (err) {
            alert('Failed to deactivate user');
            console.error(err);
        }
    }
}
