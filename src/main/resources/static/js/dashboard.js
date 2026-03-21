google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawAllCharts);

function drawAllCharts() {
    fetch('/api/charts/categories')
        .then(response => response.json())
        .then(data => {
            const dataTable = new google.visualization.DataTable();
            dataTable.addColumn('string', 'Категорія');
            dataTable.addColumn('number', 'Кількість справ');

            data.forEach(item => {
                dataTable.addRow([item.label, item.count]);
            });

            const options = {
                pieHole: 0.4,
                colors: ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#0dcaf0'],
                chartArea: {width: '80%', height: '80%'}
            };

            const chart = new google.visualization.PieChart(document.getElementById('categoryPieChart'));
            chart.draw(dataTable, options);
        });

    fetch('/api/charts/results')
        .then(response => response.json())
        .then(data => {
            const dataTable = new google.visualization.DataTable();
            dataTable.addColumn('string', 'Результат');
            dataTable.addColumn('number', 'Кількість');
            dataTable.addColumn({ type: 'string', role: 'style' });

            data.forEach(item => {
                let color = 'color: #0d6efd';
                if(item.label.includes('Виграно') || item.label.includes('Успішно')) color = 'color: #198754';
                if(item.label.includes('Програно') || item.label.includes('Відмовлено')) color = 'color: #dc3545';

                dataTable.addRow([item.label, item.count, color]);
            });

            const options = {
                legend: { position: 'none' },
                vAxis: { minValue: 0, format: '0' },
                chartArea: {width: '80%', height: '80%'}
            };

            const chart = new google.visualization.ColumnChart(document.getElementById('resultColumnChart'));
            chart.draw(dataTable, options);
        });
}