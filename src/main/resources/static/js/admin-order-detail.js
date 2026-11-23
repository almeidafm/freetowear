document.addEventListener('DOMContentLoaded', async () => {
    const container = document.getElementById('order-detail');
    const trackingForm = document.getElementById('tracking-form');
    const trackingMessage = document.getElementById('tracking-message');
    const id = decodeURIComponent(window.location.pathname.split('/').pop());
    const money = value => Number(value || 0).toLocaleString('pt-BR', {style: 'currency', currency: 'BRL'});
    const escapeHtml = value => String(value ?? '—').replace(/[&<>'"]/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[char]));

    trackingForm.addEventListener('submit', async event => {
        event.preventDefault();
        trackingMessage.hidden = false;
        trackingMessage.textContent = 'Salvando evento...';
        try {
            const response = await fetch(trackingForm.action, {
                method: 'POST',
                body: new FormData(trackingForm),
                headers: {'Accept': 'application/json'}
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.error || 'Não foi possível atualizar o rastreamento.');
            trackingMessage.textContent = result.message || 'Evento atualizado com sucesso.';
            trackingMessage.style.color = 'var(--primary)';
            window.setTimeout(() => window.location.reload(), 700);
        } catch (error) {
            trackingMessage.textContent = error.message;
            trackingMessage.style.color = '#b42318';
        }
    });

    try {
        const response = await fetch(`/admin/orders/data/${encodeURIComponent(id)}`, {headers: {'Accept': 'application/json'}});
        if (!response.ok) throw new Error('Pedido não encontrado');
        const order = await response.json();
        document.getElementById('tracking-form').action = `/order/${encodeURIComponent(order.id)}/tracking`;
        const nextStatuses = {
            PAYMENT_RECEIVED: ['PREPARING_ORDER'], PREPARING_ORDER: ['READY_TO_SHIP'],
            READY_TO_SHIP: ['SHIPPED'], SHIPPED: ['IN_TRANSIT'],
            IN_TRANSIT: ['OUT_FOR_DELIVERY'], OUT_FOR_DELIVERY: ['DELIVERED', 'DELIVERY_ATTEMPTED'],
            DELIVERY_ATTEMPTED: ['OUT_FOR_DELIVERY', 'AWAITING_PICKUP'], AWAITING_PICKUP: ['DELIVERED']
        };
        const lastEvent = (order.trackingEvents || []).at(-1);
        const trackingStatuses = nextStatuses[lastEvent?.status || lastEvent?.trackingStatus] || [];
        document.getElementById('tracking-status').innerHTML = trackingStatuses.length
            ? trackingStatuses.map(status => `<option value="${status}">${status.replaceAll('_', ' ')}</option>`).join('')
            : '<option value="">Nenhuma transição disponível</option>';
        document.querySelector('#tracking-form button').disabled = !trackingStatuses.length;
        const items = (order.items || []).map(item => `<li class="admin-order-item">
            ${item.imageUrl ? `<img class="admin-product-image" src="${escapeHtml(item.imageUrl)}" alt="${escapeHtml(item.productName)}">` : ''}
            <div><strong>${escapeHtml(item.productName)}</strong>
            <div>Cor: ${escapeHtml(item.color)} · Tamanho: ${escapeHtml(item.size)}</div>
            <div>${item.quantity} × ${money(item.unitPrice)} = ${money(item.subtotal)}</div>
            ${item.description ? `<p><strong>Descrição:</strong> ${escapeHtml(item.description)}</p>` : ''}
            ${item.customizationImageUrl ? `<p><strong>Imagem enviada pelo cliente:</strong><br><a href="${escapeHtml(item.customizationImageUrl)}" target="_blank" rel="noopener"><img class="admin-customization-image" src="${escapeHtml(item.customizationImageUrl)}" alt="Imagem enviada pelo cliente"></a></p>` : ''}
            </div></li>`).join('');
        const tracking = (order.trackingEvents || []).map(event => `<li>${escapeHtml(event.trackingStatus)} — ${new Date(event.occurredAt).toLocaleString('pt-BR')}</li>`).join('');
        container.innerHTML = `<h2>Pedido ${escapeHtml(order.id)}</h2>
            <p><strong>Cliente:</strong> ${escapeHtml(order.customerName)}</p>
            <p><strong>Status:</strong> ${escapeHtml(order.status)}</p>
            <p><strong>Data:</strong> ${new Date(order.createdAt).toLocaleString('pt-BR')}</p>
            <p><strong>Endereço:</strong> ${escapeHtml(order.deliveryAddress)}</p>
            <p><strong>Rastreamento:</strong> ${escapeHtml(order.trackingCode)}</p>
            <h3>Itens</h3><ul>${items || '<li>Nenhum item</li>'}</ul>
            <h3>Valores</h3><p>Produtos: ${money(order.productsValue)} · Frete: ${money(order.shippingPrice)} · Desconto: ${money(order.discountValue)} · <strong>Total: ${money(order.totalValue)}</strong></p>
            <h3>Histórico de rastreamento</h3><ul>${tracking || '<li>Nenhum evento</li>'}</ul>`;
    } catch (error) {
        container.textContent = 'Não foi possível carregar os dados do pedido.';
    }
});