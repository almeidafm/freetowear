// ============================================
// ADDRESS MANAGEMENT
// ============================================
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

    let shouldOpen;

    if (forceOpen !== null) {
        shouldOpen = forceOpen;
    } else {
        shouldOpen = form.style.display === 'none';
    }

    form.style.display = shouldOpen ? 'block' : 'none';
    button.style.display = shouldOpen ? 'none' : 'block';
}

async function saveNewAddress() {
    const data = {
        cep: document.getElementById('cep').value.trim(),
        street: document.getElementById('street').value.trim(),
        number: document.getElementById('number').value.trim(),
        complement: document.getElementById('complement').value.trim(),
        neighborhood: document.getElementById('neighborhood').value.trim(),
        city: document.getElementById('city').value.trim(),
        state: document.getElementById('state').value,
        defaultAddress: document.getElementById('defaultAddress').checked
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

        clearNewAddressForm();
        toggleNewAddressForm(false);
        await loadAddresses();

        alert('Address saved successfully!');

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
    document.getElementById('defaultAddress').checked = false;
}

// ============================================
// CART LOADING
// ============================================
async function loadCurrentCart() {
    try {
        const response = await fetch('/order/current', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Unable to load cart.');
        }

        currentCart = await response.json();
        renderReview();

    } catch (error) {
        console.error('Error loading cart:', error);

        document.getElementById('review-content').innerHTML = `
            <div class="error-message">
                Could not load order information.
            </div>
        `;
    }
}


// ============================================
// REVIEW RENDERING
// ============================================
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
            .join('')
        : `
            <div class="placeholder-message">
                Your cart is empty.
            </div>
        `;

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
                ${document.getElementById('coupon').value.trim() || 'No coupon'}
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
                    <span>${formatCurrency(currentCart.totalValue)}</span>
                </div>
            </div>
        </div>
    `;
}