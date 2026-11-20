document.addEventListener('DOMContentLoaded', () => {
    const items = document.querySelectorAll('.review-item-data');
    const list = document.getElementById('product-review-list');
    const products = new Map();

    items.forEach(item => {
        const id = item.dataset.productId;
        if (!products.has(id)) {
            products.set(id, { id, name: item.dataset.productName, imageUrl: item.dataset.imageUrl, variations: new Set() });
        }
        products.get(id).variations.add(item.dataset.variation);
    });

    document.getElementById('no-review-products').hidden = products.size > 0;
    products.forEach(async product => {
        list.appendChild(createReviewCard(product));
    });
});

function createReviewCard(product) {
    const card = document.createElement('article');
    card.className = 'product-review-card';
    card.dataset.productId = product.id;

    const info = document.createElement('div');
    info.className = 'product-review-product';
    info.innerHTML = `<img class="product-review-image" src="${product.imageUrl}" alt="${safe(product.name)} product image" onerror="this.replaceWith(document.createTextNode('No image'))">
        <div><p class="product-id">Product ID ${safe(product.id)}</p><h3>${safe(product.name)}</h3>
        <p class="purchased-label">Purchased:</p><p class="purchased-variations">${[...product.variations].map(safe).join(', ')}</p></div>`;

    const form = document.createElement('div');
    form.className = 'product-review-form';
    form.innerHTML = `<p class="review-label">Review your product</p>
        <div class="star-rating" role="radiogroup" aria-label="Rate ${safe(product.name)} from 1 to 5 stars">
        ${[1, 2, 3, 4, 5].map(n => `<button type="button" data-rating="${n}" aria-label="${n} star">★</button>`).join('')}</div>
        <textarea placeholder="Write your review..." aria-label="Review for ${safe(product.name)}"></textarea>
        <label class="image-upload">＋ Add photos <input type="file" accept="image/*" multiple></label>
        <div class="image-previews"></div><button type="button" class="submit-review">Submit review</button><p class="review-message"></p>`;

    const files = form.querySelector('input[type=file]');
    const previews = form.querySelector('.image-previews');
    files.addEventListener('change', () => {
        previews.replaceChildren(...[...files.files].map(file => {
            const image = document.createElement('img'); image.src = URL.createObjectURL(file); image.alt = 'Selected review image'; return image;
        }));
    });
    form.querySelector('.submit-review').addEventListener('click', async () => {
        const rating = form.dataset.rating;
        const message = form.querySelector('.review-message');
        if (!rating) { message.textContent = 'Please select a rating.'; return; }
        const data = new FormData(); data.append('productId', product.id); data.append('rating', rating); data.append('text', form.querySelector('textarea').value);
        [...files.files].forEach(file => data.append('images', file));
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
        const response = await fetch('/reviews', {
            method: 'POST',
            headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {},
            body: data
        });
        if (response.ok) {
            message.textContent = 'Review submitted.';
            form.querySelector('.submit-review').disabled = true;
        } else {
            const error = await response.json().catch(() => null);
            message.textContent = error?.message || `Could not submit review (${response.status}).`;
        }
    });

    form.querySelectorAll('.star-rating button').forEach(button => button.addEventListener('click', () => {
        form.dataset.rating = button.dataset.rating;
        form.querySelectorAll('.star-rating button').forEach(star => {
            star.classList.toggle('selected', star.dataset.rating <= button.dataset.rating);
        });
    }));

    card.append(info, form);
    return card;
}

function safe(value) {
    const element = document.createElement('span');
    element.textContent = value || '';
    return element.innerHTML;
}

(function () {
    const btn = document.getElementById('cancel-order-btn');
    if (!btn) return;

    btn.addEventListener('click', function () {
        if (!confirm('Are you sure you want to cancel this order?')) return;

        const isPaid = btn.dataset.paid === 'true';
        if (isPaid) {
            if (!confirm('Since this order has already been paid, your refund will be reviewed and processed by our team. Do you still want to cancel?')) return;
        }

        const orderId = btn.dataset.orderId;
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        btn.disabled = true;
        btn.textContent = 'Cancelling…';

        fetch('/order/cancel', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: 'orderId=' + encodeURIComponent(orderId)
        })
        .then(function (res) {
            return res.json().then(function (data) {
                return { ok: res.ok, data: data };
            });
        })
        .then(function (result) {
            if (result.ok) {
                window.location.href = '/orders';
            } else {
                alert('Could not cancel order: ' + (result.data.message || 'Unknown error'));
                btn.disabled = false;
                btn.textContent = 'Cancel Order';
            }
        })
        .catch(function () {
            alert('An error occurred. Please try again.');
            btn.disabled = false;
            btn.textContent = 'Cancel Order';
        });
    });
})();