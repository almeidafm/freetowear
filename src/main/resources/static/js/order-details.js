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
    info.innerHTML = `<img class="product-review-image" src="${product.imageUrl}" alt="Imagem do produto ${safe(product.name)}" onerror="this.replaceWith(document.createTextNode('Sem imagem'))">
        <div><p class="product-id">ID do produto: ${safe(product.id)}</p><h3>${safe(product.name)}</h3>
        <p class="purchased-label">Comprado:</p><p class="purchased-variations">${[...product.variations].map(safe).join(', ')}</p></div>`;

    const form = document.createElement('div');
    form.className = 'product-review-form';
    form.innerHTML = `<p class="review-label">Avalie seu produto</p>
        <div class="star-rating" role="radiogroup" aria-label="Dê uma nota de 1 a 5 estrelas para ${safe(product.name)}">
        ${[1, 2, 3, 4, 5].map(n => `<button type="button" data-rating="${n}" aria-label="${n} estrela">★</button>`).join('')}</div>
        <textarea placeholder="Escreva sua avaliação..." aria-label="Avaliação para ${safe(product.name)}"></textarea>
        <label class="image-upload">＋ Adicionar fotos <input type="file" accept="image/*" multiple></label>
        <div class="image-previews"></div><button type="button" class="submit-review">Enviar avaliação</button><p class="review-message"></p>`;

    const files = form.querySelector('input[type=file]');
    const previews = form.querySelector('.image-previews');
    files.addEventListener('change', () => {
        previews.replaceChildren(...[...files.files].map(file => {
            const image = document.createElement('img'); image.src = URL.createObjectURL(file); image.alt = 'Imagem selecionada para avaliação'; return image;
        }));
    });
    form.querySelector('.submit-review').addEventListener('click', async () => {
        const rating = form.dataset.rating;
        const message = form.querySelector('.review-message');
        if (!rating) { message.textContent = 'Selecione uma avaliação.'; return; }
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
            message.textContent = 'Avaliação enviada.';
            form.querySelector('.submit-review').disabled = true;
        } else {
            const error = await response.json().catch(() => null);
            message.textContent = error?.message || `Não foi possível enviar a avaliação (${response.status}).`;
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
        if (!confirm('Tem certeza de que deseja cancelar este pedido?')) return;

        const isPaid = btn.dataset.paid === 'true';
        if (isPaid) {
            if (!confirm('Como este pedido já foi pago, o reembolso será analisado e processado pela nossa equipe. Deseja cancelar mesmo assim?')) return;
        }

        const orderId = btn.dataset.orderId;
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        btn.disabled = true;
        btn.textContent = 'Cancelando…';

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
                alert('Não foi possível cancelar o pedido: ' + (result.data.message || 'Erro desconhecido'));
                btn.disabled = false;
                btn.textContent = 'Cancelar pedido';
            }
        })
        .catch(function () {
            alert('Ocorreu um erro. Tente novamente.');
            btn.disabled = false;
            btn.textContent = 'Cancelar pedido';
        });
    });
})();