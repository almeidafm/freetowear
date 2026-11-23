document.addEventListener('DOMContentLoaded', async () => {
    const container = document.getElementById('orders');
    try {
        const response = await fetch('/admin/orders/paid', {headers: {'Accept': 'application/json'}});
        if (!response.ok) throw new Error('Falha ao carregar pedidos');
        const orders = await response.json();
        if (!orders.length) {
            container.textContent = 'Nenhum pedido pago encontrado.';
            return;
        }
        const money = value => Number(value || 0).toLocaleString('pt-BR', {style: 'currency', currency: 'BRL'});
        container.className = 'admin-table-wrap';
        container.innerHTML = `<table class="admin-table"><thead><tr><th>Pedido</th><th>Cliente</th><th>Data</th><th>Status</th><th>Total</th></tr></thead><tbody>${
            orders.map(order => `<tr><td><a href="/admin/orders/${encodeURIComponent(order.id)}">${order.id}</a></td><td>${order.customerName || '—'}</td><td>${new Date(order.createdAt).toLocaleString('pt-BR')}</td><td>${order.status}</td><td>${money(order.totalValue)}</td></tr>`).join('')
        }</tbody></table>`;
    } catch (error) {
        container.textContent = 'Não foi possível carregar os pedidos pagos.';
    }
});