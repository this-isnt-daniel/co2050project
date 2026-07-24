class MonthlyStatsModule {
    static render(container, role) {
        container.innerHTML = `
            <div class="stats-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <div>
                    <h2>Monthly Statistics</h2>
                    <p style="color: #6c757d; font-size: 14px;">Case trends and distributions for <span id="stats-year-label">2026</span></p>
                </div>
                <select id="stats-year-select" class="form-control" style="width: 100px;">
                    <option value="2026" selected>2026</option>
                    <option value="2025">2025</option>
                    <option value="2024">2024</option>
                </select>
            </div>

            <!-- KPI Cards -->
            <div class="stats-kpi-row" style="display: flex; gap: 15px; margin-bottom: 20px; flex-wrap: wrap;">
                <div class="card" style="flex: 1; text-align: center; padding: 20px;">
                    <h2 id="kpi-patients" style="color: #4a90e2; font-size: 32px; margin: 0;">0</h2>
                    <p style="margin: 0; color: #6c757d; font-size: 14px;">Patients<br>in <span class="yr-label">2026</span></p>
                </div>
                <div class="card" style="flex: 1; text-align: center; padding: 20px;">
                    <h2 id="kpi-mlef" style="color: #27ae60; font-size: 32px; margin: 0;">0</h2>
                    <p style="margin: 0; color: #6c757d; font-size: 14px;">MLEF<br>in <span class="yr-label">2026</span></p>
                </div>
                <div class="card" style="flex: 1; text-align: center; padding: 20px;">
                    <h2 id="kpi-mlr" style="color: #f39c12; font-size: 32px; margin: 0;">0</h2>
                    <p style="margin: 0; color: #6c757d; font-size: 14px;">MLR<br>in <span class="yr-label">2026</span></p>
                </div>
                <div class="card" style="flex: 1; text-align: center; padding: 20px;">
                    <h2 id="kpi-lab" style="color: #e74c3c; font-size: 32px; margin: 0;">0</h2>
                    <p style="margin: 0; color: #6c757d; font-size: 14px;">Lab Requests<br>in <span class="yr-label">2026</span></p>
                </div>
                <div class="card" style="flex: 1; text-align: center; padding: 20px;">
                    <h2 id="kpi-pmr" style="color: #8e44ad; font-size: 32px; margin: 0;">0</h2>
                    <p style="margin: 0; color: #6c757d; font-size: 14px;">PMRs<br>in <span class="yr-label">2026</span></p>
                </div>
            </div>

            <!-- Bar Chart -->
            <div class="card mb-3">
                <div class="card-header">Monthly Case Volume &mdash; <span id="bar-year-label">2026</span></div>
                <div class="card-body" style="height: 350px;">
                    <canvas id="monthlyVolumeChart"></canvas>
                </div>
            </div>

            <!-- Pie Charts -->
            <div style="display: flex; gap: 15px; margin-bottom: 20px;">
                <div class="card" style="flex: 1;">
                    <div class="card-header">Body Harm Distribution (All MLEF)</div>
                    <div class="card-body" style="height: 250px; display: flex; justify-content: center;">
                        <canvas id="bodyHarmChart"></canvas>
                    </div>
                </div>
                <div class="card" style="flex: 1;">
                    <div class="card-header">Hurt Category (MLEF)</div>
                    <div class="card-body" style="height: 250px; display: flex; justify-content: center;">
                        <canvas id="hurtCategoryChart"></canvas>
                    </div>
                </div>
            </div>
        `;

        document.getElementById('stats-year-select').addEventListener('change', (e) => {
            const y = e.target.value;
            document.getElementById('stats-year-label').textContent = y;
            document.getElementById('bar-year-label').textContent = y;
            document.querySelectorAll('.yr-label').forEach(el => el.textContent = y);
            this.loadStats(y);
        });

        this.loadStats('2026');
    }

    static async loadStats(year) {
        try {
            const res = await ApiClient.get(`/statistics/monthly?year=${year}`);
            if (!res) return;

            // Update KPIs
            document.getElementById('kpi-patients').textContent = res.summary.patients || 0;
            document.getElementById('kpi-mlef').textContent = res.summary.mlef || 0;
            document.getElementById('kpi-mlr').textContent = res.summary.mlr || 0;
            document.getElementById('kpi-lab').textContent = res.summary.labRequests || 0;
            document.getElementById('kpi-pmr').textContent = res.summary.pmr || 0;

            this.renderBarChart(res.monthlyVolumes);
            this.renderBodyHarmChart(res.mlefBodyHarmDistribution);
            this.renderHurtCategoryChart(res.mlefHurtCategoryDistribution);

        } catch (err) {
            console.error('Failed to load statistics', err);
        }
    }

    static renderBarChart(data) {
        if (this.volumeChartInst) this.volumeChartInst.destroy();
        const ctx = document.getElementById('monthlyVolumeChart').getContext('2d');
        
        // Match the colors in the user's screenshot
        const colors = ['#4a90e2', '#a55eea', '#2ecc71', '#f39c12'];
        
        const datasets = data.datasets.map((ds, i) => ({
            label: ds.label,
            data: ds.data,
            backgroundColor: colors[i % colors.length],
            borderWidth: 0
        }));

        this.volumeChartInst = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom' }
                },
                scales: {
                    y: { beginAtZero: true, grid: { borderDash: [2,2] } },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    static renderBodyHarmChart(data) {
        if (this.harmChartInst) this.harmChartInst.destroy();
        if (!data) return;
        const ctx = document.getElementById('bodyHarmChart').getContext('2d');
        this.harmChartInst = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.data,
                    backgroundColor: ['#4a90e2', '#2ecc71', '#e74c3c', '#f1c40f', '#9b59b6']
                }]
            },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'right' } } }
        });
    }

    static renderHurtCategoryChart(data) {
        if (this.hurtChartInst) this.hurtChartInst.destroy();
        if (!data) return;
        const ctx = document.getElementById('hurtCategoryChart').getContext('2d');
        this.hurtChartInst = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.data,
                    backgroundColor: ['#34495e', '#16a085', '#d35400', '#2980b9']
                }]
            },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'right' } } }
        });
    }
}
