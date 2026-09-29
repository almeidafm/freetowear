const links = document.querySelectorAll('nav a');

function show(id) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(id).classList.add('active');

    links.forEach(a => {
        a.classList.toggle('active', a.getAttribute('onclick')?.includes(id));
    });
}

async function loadValidTrackingStatuses(orderId) {
    const statusSelect = document.getElementById('trackingStatus');
    statusSelect.innerHTML = '<option value="" selected>Loading statuses...</option>';
    statusSelect.disabled = true;

    if (!orderId.trim()) {
        statusSelect.innerHTML = '<option value="" selected>Enter an order ID first</option>';
        return;
    }

    try {
        const response = await fetch('/order/' + encodeURIComponent(orderId) + '/tracking');
        if (!response.ok) throw new Error('Order not found');

        const tracking = await response.json();
        const currentStatus = tracking.events?.at(-1)?.status;
        const validStatuses = {
            PAYMENT_RECEIVED: ['PREPARING_ORDER'],
            PREPARING_ORDER: ['READY_TO_SHIP'],
            READY_TO_SHIP: ['SHIPPED'],
            SHIPPED: ['IN_TRANSIT'],
            IN_TRANSIT: ['OUT_FOR_DELIVERY'],
            OUT_FOR_DELIVERY: ['DELIVERED', 'DELIVERY_ATTEMPTED'],
            DELIVERY_ATTEMPTED: ['OUT_FOR_DELIVERY', 'AWAITING_PICKUP'],
            AWAITING_PICKUP: ['DELIVERED']
        };
        const nextStatuses = validStatuses[currentStatus] || [];

        statusSelect.innerHTML = nextStatuses.length
            ? '<option value="" disabled selected>Select a status</option>'
            : '<option value="" selected>No valid next status</option>';

        nextStatuses.forEach(status => {
            const option = document.createElement('option');
            option.value = status;
            option.textContent = status.replaceAll('_', ' ');
            statusSelect.appendChild(option);
        });
        statusSelect.disabled = nextStatuses.length === 0;
    } catch (error) {
        statusSelect.innerHTML = '<option value="" selected>Unable to load order status</option>';
    }
}
