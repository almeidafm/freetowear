const links = document.querySelectorAll('nav a');

function show(id) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(id).classList.add('active');

    links.forEach(a => {
        a.classList.toggle('active', a.getAttribute('onclick')?.includes(id));
    });
}

// ── TOAST ────────────────────────────────────────────────────────────────────

function toast(message, type = 'success') {
    const existing = document.getElementById('admin-toast');
    if (existing) existing.remove();

    const el = document.createElement('div');
    el.id = 'admin-toast';
    el.className = `admin-toast admin-toast--${type}`;
    el.textContent = message;
    document.body.appendChild(el);

    requestAnimationFrame(() => el.classList.add('admin-toast--visible'));

    setTimeout(() => {
        el.remove();
    }, 3500);
}

// ── PRODUCT ACTIONS ─────────────────────────────────────────────────────────

async function submitAdminAction(containerId, action) {
    const container = document.getElementById(containerId);
    const button = container.querySelector('button');
    const body = new FormData();

    container.querySelectorAll('input, select, textarea').forEach(field => {
        if (field.name && (field.type !== 'file' || field.files.length > 0)) {
            if (field.type === 'file') body.append(field.name, field.files[0]);
            else body.append(field.name, field.value);
        }
    });

    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = 'Loading...';

    try {
        const response = await fetch(action, {
            method: 'POST',
            body,
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });

        const message = await response.text();
        if (!response.ok) throw new Error(message || `Error ${response.status}`);

        toast(message || 'Done!', 'success');
        container.querySelectorAll('input:not([type="hidden"]), select, textarea')
            .forEach(field => field.value = '');
    } catch (error) {
        console.error(error);
        toast(error.message || 'Request failed.', 'error');
    } finally {
        button.disabled = false;
        button.textContent = originalText;
    }
}

function createProduct() {
    submitAdminAction('create-product', '/products/create');
}

function createVariation() {
    submitAdminAction('create-variation', '/products/variation/create');
}

// ── FORM INTERCEPTOR ─────────────────────────────────────────────────────────

document.addEventListener('submit', async (e) => {
    const form = e.target.closest('form');
    if (!form) return;

    e.preventDefault();

    const method = (form.querySelector('input[name="_method"]')?.value || form.method).toUpperCase();
    const action = form.action;
    const btn = form.querySelector('button[type="submit"]');

    if (btn) { btn.disabled = true; btn.style.opacity = '0.6'; }

    try {
        const body = new FormData(form);

        const res = await fetch(action, {
            method,
            body,
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });

        const text = await res.text();

        if (res.ok) {
            toast(text || 'Done!', 'success');
            form.reset();
        } else {
            toast(text || `Error ${res.status}`, 'error');
        }
    } catch (err) {
        console.error(err);
        toast('Network error — please try again.', 'error');
    } finally {
        if (btn) { btn.disabled = false; btn.style.opacity = ''; }
    }
});

// ── ORDER TRACKING HELPER ─────────────────────────────────────────────────────

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