const $ = s => document.querySelector(s);
const nf = n => (+n).toLocaleString('pt-BR', { maximumFractionDigits: 1 });
const fm = n => (+n).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 });
const pc = n => nf(n * 100) + '%';

let currentData = null;
let CH = [];
let Q = [];
let N = 0;
const PAL = ['#5eead4', '#fbbf24', '#60a5fa', '#fb7185', '#c084fc', '#bef264', '#22d3ee', '#f97316'];

const cd = (t, b, c = '', id = '') => `<div class="c ${c}" ${id ? `id="${id}"` : ''}><h3>${t}</h3>${b}</div>`;
const ms = (l, v) => `<div class="ms"><b>${v}</b><span>${l}</span></div>`;
const mg = a => `<div class="mg">${a.join('')}</div>`;

const kc = (label, formattedValue, kpi, inv = false) => {
    const change = kpi ? kpi.deltaPercentage : null;
    let badgeHtml = '<i class="mut">— vs período anterior</i>';

    if (change !== null && change !== undefined) {
        const isInc = change >= 0;
        const good = inv ? !isInc : isInc;
        const arrow = isInc ? '▲' : '▼';
        badgeHtml = `<i class="${good ? 'up' : 'dn'}">${arrow} ${nf(Math.abs(change))}% vs período anterior</i>`;
    }

    return `<div class="k"><span>${label}</span><b>${formattedValue}</b>${badgeHtml}</div>`;
};

const ch = (t, type, lb, ds, o = {}, cls = '', anchor = '') => {
    const id = 'chart' + (++N);
    Q.push(() => {
        const canvas = $('#' + id);
        if (!canvas) return;
        CH.push(new Chart(canvas, {
            type,
            data: {
                labels: lb,
                datasets: ds.map((d, i) => ({
                    borderColor: PAL[i % PAL.length],
                    ...(type === 'doughnut'
                        ? { backgroundColor: PAL, borderWidth: 0, hoverOffset: 5 }
                        : { backgroundColor: PAL[i % PAL.length], borderRadius: 5 }
                    ),
                    ...d
                }))
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                color: '#89949d',
                plugins: {
                    legend: {
                        display: type === 'doughnut' || ds.length > 1,
                        labels: {
                            color: '#aab4bc',
                            boxWidth: 9,
                            boxHeight: 9,
                            usePointStyle: true,
                            pointStyle: 'circle',
                            padding: 16
                        }
                    },
                    tooltip: {
                        backgroundColor: '#111619',
                        titleColor: '#f4f7f8',
                        bodyColor: '#d3dcdf',
                        borderColor: '#30383d',
                        borderWidth: 1,
                        padding: 11,
                        displayColors: true
                    }
                },
                scales: type === 'doughnut' ? {} : {
                    x: {
                        grid: { color: 'rgba(130,145,155,.09)' },
                        ticks: { color: '#7f8a94', maxRotation: 35, autoSkip: true },
                        ...(o.indexAxis === 'y' ? { beginAtZero: true } : {})
                    },
                    y: {
                        grid: { color: 'rgba(130,145,155,.12)' },
                        ticks: { color: '#7f8a94' },
                        ...(o.indexAxis === 'y' ? { beginAtZero: true } : {})
                    }
                },
                ...o
            }
        }));
    });
    return cd(t, `<div class="cv"><canvas id="${id}"></canvas></div>`, cls, anchor);
};

function dashboardView() {
    if (!currentData) return '';
    const k = currentData.kpis || {};
    const out = [];

    // 9 KPI Cards
    out.push(`<div class="kg">${[
        ['Receita total (Gross Revenue)', fm(k.grossRevenue?.currentValue || 0), k.grossRevenue],
        ['Receita líquida (Net Revenue)', fm(k.netRevenue?.currentValue || 0), k.netRevenue],
        ['Nº de pedidos (Orders)', nf(k.ordersCount?.currentValue || 0), k.ordersCount],
        ['Ticket médio (Average Order Value, AOV)', fm(k.averageOrderValue?.currentValue || 0), k.averageOrderValue],
        ['Novos clientes (New Customers)', nf(k.newCustomers?.currentValue || 0), k.newCustomers],
        ['Taxa de recompra (Repeat Purchase Rate)', pc((k.repeatPurchaseRate?.currentValue || 0) / 100), k.repeatPurchaseRate],
        ['Taxa de cancelamento (Cancellation Rate)', pc((k.cancellationRate?.currentValue || 0) / 100), k.cancellationRate, true],
        ['Prazo médio de entrega (Avg Delivery Time)', nf(k.averageDeliveryTimeDays?.currentValue || 0) + ' dias', k.averageDeliveryTimeDays, true],
        ['Nota média (Avg Rating)', nf(k.averageRating?.currentValue || 0) + ' ★', k.averageRating]
    ].map(item => kc(item[0], item[1], item[2], item[3] || false)).join('')}</div>`);

    // 12-month Revenue Chart
    const rev12 = currentData.revenue12Months || { labels: [], data: [] };
    out.push(ch(
        'Receita total · últimos 12 meses',
        'line',
        rev12.labels || [],
        [{ label: 'Receita', data: rev12.data || [], fill: false, tension: 0.35 }],
        { scales: { y: { beginAtZero: true, ticks: { callback: v => fm(v) } } } },
        '',
        'revenue-12-months'
    ));

    // 12-month Rating Chart
    const rat12 = currentData.rating12Months || { labels: [], data: [] };
    out.push(ch(
        'Nota média · últimos 12 meses',
        'line',
        rat12.labels || [],
        [{ label: 'Nota média', data: rat12.data || [], fill: false, tension: 0.35 }],
        { scales: { y: { beginAtZero: true, min: 0, max: 5, ticks: { stepSize: 1 } } } },
        '',
        'rating-12-months'
    ));

    // Revenue by Category (Doughnut)
    const catRev = currentData.categoryRevenue || [];
    const catLabels = catRev.length ? catRev.map(c => c.categoryName) : ['Sem dados'];
    const catData = catRev.length ? catRev.map(c => c.revenue || c.totalRevenue || 0) : [0];
    out.push(ch(
        'Receita por categoria',
        'doughnut',
        catLabels,
        [{ label: 'Receita', data: catData }],
        {},
        '',
        'category'
    ));

    // Revenue by State (Horizontal Bar)
    const stRev = currentData.stateRevenue || [];
    const stateLabels = stRev.length ? stRev.map(s => s.state) : ['Sem dados'];
    const stateData = stRev.length ? stRev.map(s => s.revenue || s.totalRevenue || 0) : [0];
    out.push(ch(
        'Receita por estado',
        'bar',
        stateLabels,
        [{ label: 'Receita', data: stateData }],
        { indexAxis: 'y', plugins: { legend: { display: false } } },
        '',
        'state'
    ));

    // Delivery Price / Freight Summary
    const fs = currentData.freightSummary || { averageFreight: 0, freightToRevenuePercentage: 0, freeShippingPercentage: 0 };
    out.push(cd('Frete', mg([
        ms('Frete médio por pedido', fm(fs.averageFreight || 0)),
        ms('Frete / receita total', pc((fs.freightToRevenuePercentage || 0) / 100)),
        ms('Pedidos com frete grátis', pc((fs.freeShippingPercentage || 0) / 100))
    ]), '', 'freight'));

    return `<div class="g">${out.join('')}</div>`;
}

async function loadData() {
    CH.forEach(c => c.destroy());
    CH = [];
    Q = [];
    N = 0;
    const days = $('#pd').value;
    $('#dashboard').innerHTML = '<div class="loading-state">Atualizando métricas para os últimos ' + days + ' dias...</div>';

    try {
        const res = await fetch(`/api/admin/dashboard?days=${days}`);
        if (!res.ok) {
            throw new Error('Erro ao obter métricas (' + res.status + ')');
        }
        currentData = await res.json();
        $('#dashboard').innerHTML = dashboardView();
        Q.forEach(f => f());
    } catch (err) {
        console.error('Falha ao carregar dashboard:', err);
        $('#dashboard').innerHTML = '<div class="error-state">Não foi possível carregar os dados do dashboard. Verifique sua conexão ou tente novamente.</div>';
    }
}

Chart.defaults.color = '#8f9aa4';
Chart.defaults.borderColor = 'rgba(130,145,155,.12)';
Chart.defaults.font.family = 'Inter, ui-sans-serif, system-ui, sans-serif';

$('#pd').addEventListener('change', loadData);
window.addEventListener('resize', () => CH.forEach(c => c.resize()));

document.addEventListener('DOMContentLoaded', loadData);
