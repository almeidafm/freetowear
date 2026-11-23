let selectedAddressId = null;

let selectedAddress = null;

let addresses = [];

let selectedPaymentMethod = null;

let currentCart = null;

let appliedCoupon = null;

function formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(value || 0);
}

function parseCurrency(value) {
    if (!value) return 0;
    const normalized = String(value)
        .replace(/[^\d,.-]/g, '')
        .replace(/\./g, '')
        .replace(',', '.');
    const number = Number(normalized);
    return Number.isFinite(number) ? number : 0;
}

function renderCheckoutSummary() {
    const summary = document.getElementById('checkout-summary');
    if (!summary) return;

    const products = [...document.querySelectorAll('.bag-row')]
        .reduce((total, row) => total + parseCurrency(row.querySelector('strong')?.textContent), 0);

    const selectedShipping = document.querySelector('input[name="shippingMethod"]:checked');
    const shippingType = selectedShipping?.value;
    const shippingPriceElement = document.getElementById(`${shippingType}-delivery-price`);
    const shippingTimeElement = document.getElementById(`${shippingType}-delivery-time`);
    const shippingPrice = parseCurrency(shippingPriceElement?.textContent);
    const shippingLabel = shippingType === 'standard' ? 'Entrega padrão' : 'Entrega expressa';

    let discount = 0;
    if (appliedCoupon) {
        const couponValue = Number(appliedCoupon.discountValue) || 0;
        discount = appliedCoupon.discountType === 'PERCENTAGE'
            ? products * couponValue / 100
            : couponValue;
        discount = Math.min(products, discount);
    }

    const total = Math.max(0, products + shippingPrice - discount);
    const values = summary.querySelectorAll('.summary-line strong');
    values[0].textContent = formatCurrency(products);
    values[1].textContent = shippingPrice ? formatCurrency(shippingPrice) : 'Selecione o endereço';
    values[2].textContent = discount ? `- ${formatCurrency(discount)}` : formatCurrency(0);
    values[3].textContent = formatCurrency(total);
    document.getElementById('summary-shipping-label').textContent = shippingLabel;
    const shippingTime = shippingTimeElement?.textContent || 'Selecione o endereço.';
    document.getElementById('summary-delivery-time').innerHTML =
        `Produção: até 7 dias úteis.<br>Entrega: ${shippingTime.replace(/^Receba em /, '')}`;
}

function escapeHtml(value) {
    if (value == null) {
        return '';
    }

    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]').content;
}

function getCsrfHeaderName() {
    return document.querySelector('meta[name="_csrf_header"]').content;
}

document.getElementById('checkout-form')?.addEventListener('submit', async function (event) {
    event.preventDefault();

    try {
        // Aguarda a validação automática antes de enviar o pedido.
        await couponValidationPromise;

        const response = await fetch(this.action, {
            method: 'POST',
            body: new FormData(this)
        });
        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(data.message || 'Could not finish order.');
        }

        if (!data.redirectUrl) throw new Error('Mercado Pago não retornou a URL de pagamento.');
        window.location.href = data.redirectUrl;
    } catch (error) {
        alert(error.message || 'Could not finish order.');
    }
});

document.getElementById('save-address-btn')?.addEventListener('click', saveNewAddress);

const shippingRanges = {
    southeast: { standard: [10, 20, 5, 10], express: [25, 50, 1, 3] },
    south: { standard: [12, 25, 6, 11], express: [30, 60, 2, 4] },
    central: { standard: [15, 30, 7, 12], express: [35, 70, 2, 5] },
    northeast: { standard: [18, 35, 8, 15], express: [40, 80, 3, 6] },
    north: { standard: [25, 45, 10, 20], express: [50, 100, 4, 8] }
};

const stateRegions = {
    SP: 'southeast', RJ: 'southeast', ES: 'southeast', MG: 'southeast',
    PR: 'south', SC: 'south', RS: 'south',
    GO: 'central', MT: 'central', MS: 'central', DF: 'central',
    BA: 'northeast', SE: 'northeast', AL: 'northeast', PE: 'northeast', PB: 'northeast', RN: 'northeast', CE: 'northeast', PI: 'northeast', MA: 'northeast',
    AM: 'north', RR: 'north', AP: 'north', PA: 'north', RO: 'north', AC: 'north', TO: 'north'
};

function updateShippingOptions(state) {
    const region = stateRegions[state];
    const ranges = shippingRanges[region];
    if (!ranges) return;
    const formatPrice = value => value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    ['standard', 'express'].forEach(type => {
        const [min, max, daysMin, daysMax] = ranges[type];
        const value = (min + max) / 2;
        document.getElementById(`${type}-delivery-price`).textContent = formatPrice(value);
        document.getElementById(`${type}-delivery-time`).textContent =
            `Receba em ${daysMin} a ${daysMax} dias úteis após a produção.`;
    });
    renderCheckoutSummary();
}

document.querySelectorAll('input[name="idAddress"]').forEach(input => {
    input.addEventListener('change', () => {
        const address = input.closest('[data-state]');
        updateShippingOptions(address?.dataset.state || document.getElementById('state')?.value);
    });
});
document.getElementById('state')?.addEventListener('change', event => updateShippingOptions(event.target.value));
document.querySelectorAll('input[name="shippingMethod"]').forEach(input => {
    input.addEventListener('change', renderCheckoutSummary);
});

let couponValidationTimer = null;
let couponValidationRequest = 0;
let couponValidationPromise = Promise.resolve();

function setCouponFeedback(message, isError = false) {
    const feedback = document.getElementById('coupon-feedback');
    if (!feedback) return;
    feedback.textContent = message;
    feedback.classList.toggle('form-error', isError);
}

async function validateCouponAutomatically(value) {
    const requestId = ++couponValidationRequest;
    const couponInput = document.getElementById('coupon');
    const couponIdInput = document.getElementById('idCoupon');
    const code = value.trim();

    appliedCoupon = null;
    if (couponIdInput) couponIdInput.value = '';
    setCouponFeedback('');
    renderCheckoutSummary();

    if (code.length < 5) return;

    setCouponFeedback('Validando cupom...');
    try {
        const response = await fetch(`/order/coupon?code=${encodeURIComponent(code)}`, {
            headers: { Accept: 'application/json' }
        });
        const data = await response.json().catch(() => ({}));
        if (requestId !== couponValidationRequest || couponInput?.value.trim() !== code) return;
        if (!response.ok) throw new Error(data.message || 'Cupom inválido.');

        appliedCoupon = data;
        // O backend aceita o código e assim evita depender do ID retornado pela API.
        if (couponIdInput) couponIdInput.value = data.code || code;
        setCouponFeedback('Cupom aplicado.');
        renderCheckoutSummary();
    } catch (error) {
        if (requestId !== couponValidationRequest) return;
        setCouponFeedback(error.message || 'Cupom inválido.', true);
        renderCheckoutSummary();
    }
}

document.getElementById('coupon')?.addEventListener('input', event => {
    clearTimeout(couponValidationTimer);
    const value = event.target.value;
    couponValidationPromise = new Promise(resolve => {
        couponValidationTimer = setTimeout(async () => {
            await validateCouponAutomatically(value);
            resolve();
        }, 350);
    });
    renderCheckoutSummary();
});
renderCheckoutSummary();
const initialAddress = document.querySelector('input[name="idAddress"]:checked');
if (initialAddress) {
    const address = initialAddress.closest('[data-state]');
    updateShippingOptions(address?.dataset.state || document.getElementById('state')?.value);
}

function syncNewAddressForm() {
    const addressOptions = document.querySelectorAll('input[name="idAddress"]');
    const newAddressForm = document.getElementById('new-address-form');

    if (!newAddressForm) return;

    const selectedAddress = document.querySelector('input[name="idAddress"]:checked');
    const isNewAddress = selectedAddress && selectedAddress.value === 'new';

    if (selectedAddress) selectedAddressId = selectedAddress.value;

    newAddressForm.classList.toggle('hidden', !isNewAddress);
    newAddressForm.querySelectorAll('input, select').forEach(field => {
        field.disabled = !isNewAddress;
    });

    addressOptions.forEach(option => {
        option.addEventListener('change', syncNewAddressForm, { once: true });
    });
}

syncNewAddressForm();

async function loadAddresses() {
    try {
        const response = await fetch('/account/address', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error loading addresses:', errorText);
            throw new Error(`GET /account/address returned ${response.status}`);
        }

        addresses = await response.json();
        renderAddresses(addresses);

    } catch (error) {
        console.error('Error in loadAddresses():', error);

        const addressList = document.getElementById('address-list');
        addressList.innerHTML = `
            <div class="error-message">
                Could not load your addresses.
            </div>
        `;
    }
}

function renderAddresses(addresses) {
    const addressList = document.getElementById('address-list');

    if (!addresses || addresses.length === 0) {
        selectedAddressId = null;
        selectedAddress = null;
        document.getElementById('selectedAddressId').value = '';

        addressList.innerHTML = `
            <div class="empty-address-message">
                You don't have any saved addresses yet.
            </div>
        `;

        toggleNewAddressForm(true);
        return;
    }

    const defaultAddress = addresses.find(addr => addr.defaultAddress === true);
    selectedAddress = defaultAddress || addresses[0];
    selectedAddressId = selectedAddress.id;

    document.getElementById('selectedAddressId').value = selectedAddressId;

    addressList.innerHTML = addresses
        .map(address => {
            const checked = String(address.id) === String(selectedAddressId);

            return `
                <div class="address-option">
                    <input
                        type="radio"
                        id="address-${escapeHtml(address.id)}"
                        name="shippingAddress"
                        value="${escapeHtml(address.id)}"
                        ${checked ? 'checked' : ''}
                        onchange="selectAddress(this.value)"
                    >
                    <label for="address-${escapeHtml(address.id)}" class="address-label">
                        <div class="address-title">
                            ${escapeHtml(address.street)}
                            ${address.number ? ', ' + escapeHtml(address.number) : ''}
                            ${address.defaultAddress ? '<span class="default-badge">Default</span>' : ''}
                        </div>
                        <div class="address-details">
                            ${address.neighborhood ? escapeHtml(address.neighborhood) + '<br>' : ''}
                            ${address.city ? escapeHtml(address.city) : ''}
                            ${address.state ? ' - ' + escapeHtml(address.state) : ''}
                            ${address.cep ? '<br>CEP: ' + escapeHtml(address.cep) : ''}
                            ${address.complement ? '<br>' + escapeHtml(address.complement) : ''}
                        </div>
                    </label>
                </div>
            `;
        })
        .join('');
}

function selectAddress(addressId) {
    selectedAddressId = addressId;
    document.getElementById('selectedAddressId').value = addressId;

    selectedAddress = addresses.find(
        address => String(address.id) === String(addressId)
    );

    renderReview();
}

function toggleNewAddressForm(forceOpen = null) {
    const form = document.getElementById('new-address-form');
    const button = document.getElementById('add-address-btn');

    if (!form) return;

    let shouldOpen;

    if (forceOpen !== null) {
        shouldOpen = forceOpen;
    } else {
        shouldOpen = form.style.display === 'none';
    }

    form.style.display = shouldOpen ? 'block' : 'none';
    if (button) button.style.display = shouldOpen ? 'none' : 'block';
}

async function saveNewAddress() {
    const data = {
        cep: document.getElementById('cep').value.replace(/\D/g, ''),
        street: document.getElementById('street').value.trim(),
        number: document.getElementById('number').value.trim(),
        complement: document.getElementById('complement').value.trim(),
        neighborhood: document.getElementById('neighborhood').value.trim(),
        city: document.getElementById('city').value.trim(),
        state: document.getElementById('state').value,
        defaultAddress: false
    };

    if (!data.cep || !data.street || !data.neighborhood || !data.city || !data.state) {
        alert('Please fill in all required fields.');
        return;
    }

    try {
        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeaderName();

        const response = await fetch('/account/address', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'text/plain',
                [csrfHeader]: csrfToken
            },
            body: new URLSearchParams(data)
        });

        if (!response.ok) {
            const responseText = await response.text();
            throw new Error(`Error ${response.status}: ${responseText}`);
        }

        alert('Address saved successfully!');
        window.location.reload();

    } catch (error) {
        console.error('Error saving address:', error);
        alert(error.message);
    }
}

function clearNewAddressForm() {
    document.getElementById('cep').value = '';
    document.getElementById('street').value = '';
    document.getElementById('number').value = '';
    document.getElementById('complement').value = '';
    document.getElementById('neighborhood').value = '';
    document.getElementById('city').value = '';
    document.getElementById('state').value = '';
}

function renderReview() {
    const reviewContent = document.getElementById('review-content');

    if (!currentCart) {
        reviewContent.innerHTML = `
            <div class="placeholder-message">
                Loading order information...
            </div>
        `;
        return;
    }

    const address = selectedAddress;
    const addressHtml = address
        ? `
            <div class="review-address">
                ${escapeHtml(address.street)}
                ${address.number ? ', ' + escapeHtml(address.number) : ''}
                <br>
                ${address.neighborhood ? escapeHtml(address.neighborhood) + '<br>' : ''}
                ${escapeHtml(address.city)}
                ${address.state ? ' - ' + escapeHtml(address.state) : ''}
                <br>
                ${address.cep ? 'CEP: ' + escapeHtml(address.cep) : ''}
                ${address.complement ? '<br>' + escapeHtml(address.complement) : ''}
            </div>
        `
        : `
            <div class="error-message">
                No shipping address selected.
            </div>
        `;

    const paymentLabels = {
        CREDIT_CARD: 'Credit Card',
        PIX: 'PIX',
        BOLETO: 'Boleto'
    };

    const paymentLabel = paymentLabels[selectedPaymentMethod] || selectedPaymentMethod || 'Not selected';

    let couponHtml = `
        <div class="review-item">
            <div class="review-item-info">
                <div class="review-item-name">
                    No coupon
                </div>
            </div>
        </div>
    `;

    const couponInput = document.getElementById('coupon');
    const couponVal = couponInput ? couponInput.value.trim() : '';

    if (appliedCoupon) {
        const displayCode = (couponVal || appliedCoupon.code).toUpperCase();

        let discountDisplay = '';
        if (appliedCoupon.discountType === 'PERCENTAGE') {
            discountDisplay = `${Number(appliedCoupon.discountValue)}%`;
        } else if (appliedCoupon.discountType === 'FIXED') {
            discountDisplay = formatCurrency(appliedCoupon.discountValue);
        } else if (appliedCoupon.discountValue != null) {
            discountDisplay = `${appliedCoupon.discountValue}`;
        }

        couponHtml = `
            <div class="review-item">
                <div class="review-item-info">
                    <div class="review-item-name">
                        ${escapeHtml(displayCode)}
                    </div>
                </div>
                <div class="review-item-price">
                    ${discountDisplay}
                </div>
            </div>
        `;
    } else if (couponVal) {
        couponHtml = `
            <div class="review-item">
                <div class="review-item-info">
                    <div class="review-item-name">
                        ${escapeHtml(couponVal)}
                    </div>
                </div>
            </div>
        `;
    }

    const shippingPrice = Number(currentCart.shippingPrice || 0);
    const shippingHtml = `
        <div class="review-item">
            <div class="review-item-info">
                <div class="review-item-name">
                    Shipping
                </div>
            </div>
            <div class="review-item-price">
                ${shippingPrice > 0 ? formatCurrency(shippingPrice) : 'Free'}
            </div>
        </div>
    `;

    const itemsHtml = currentCart.items && currentCart.items.length > 0
        ? currentCart.items
            .map(item => `
                <div class="review-item">
                    <div class="review-item-info">
                        <div class="review-item-name">
                            ${escapeHtml(item.productName)}
                        </div>
                        <div class="review-item-variation">
                            ${escapeHtml(item.variation)} · Qty: ${item.quantity}
                        </div>
                    </div>
                    <div class="review-item-price">
                        ${formatCurrency(item.subtotal)}
                    </div>
                </div>
            `)
            .join('') + shippingHtml
        : `
            <div class="placeholder-message">
                Your cart is empty.
            </div>
        `;

   let finalTotal = Number(currentCart.totalValue || 0);
   let originalTotal = null;

   if (appliedCoupon && currentCart.productsValue != null) {
       const productsVal = Number(currentCart.productsValue);
       const shipping = Number(currentCart.shippingPrice || 0);

       originalTotal = productsVal + shipping;

       if (currentCart.discountValue != null && Number(currentCart.discountValue) > 0) {
           finalTotal = Number(currentCart.totalValue || 0);
       } else {
           let discount = 0;

           if (appliedCoupon.discountType === 'PERCENTAGE') {
               discount = (productsVal * Number(appliedCoupon.discountValue)) / 100;
           } else if (appliedCoupon.discountType === 'FIXED') {
               discount = Number(appliedCoupon.discountValue);
           }

           if (discount > productsVal) {
               discount = productsVal;
           }

           finalTotal = Math.max(0, productsVal - discount + shipping);
       }
   }

    reviewContent.innerHTML = `
        <div class="review-section">
            <div class="review-section-title">Shipping Address</div>
            <div class="review-card">
                ${addressHtml}
            </div>
        </div>

        <div class="review-section">
            <div class="review-section-title">Payment Method</div>
            <div class="review-card review-payment">
                ${escapeHtml(paymentLabel)}
            </div>
        </div>

        <div class="review-section">
            <div class="review-section-title">Coupon</div>
            <div class="review-card review-payment">
                ${couponHtml}
            </div>
        </div>

        <div class="review-section">
            <div class="review-section-title">Order Items</div>
            <div class="review-card">
                <div class="review-items">
                    ${itemsHtml}
                </div>
                <div class="review-total">
                    <span>Total</span>
                    <span>
                        ${originalTotal != null && originalTotal > finalTotal
                            ? `<span class="review-total-original">${formatCurrency(originalTotal)}</span> `
                            : ''}
                        ${formatCurrency(finalTotal)}
                    </span>
                </div>
            </div>
        </div>
    `;
}
