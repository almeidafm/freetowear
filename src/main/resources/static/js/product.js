/*<![CDATA[*/
(() => {
    const product = window.ftwProduct || {};
    const variations = product.variations || [];

    const swatchesContainer = document.querySelector('.swatches');
    const sizesContainer = document.querySelector('.sizes');
    const variationSource = variations;
    const isInStock = variation => Number(variation.stock ?? 0) > 0;

    const uniqueValues = (items, property) => [...new Map(
        items.filter(v => v[property]).map(v => [String(v[property]).trim().toLowerCase(), String(v[property]).trim()])
    ).values()];

    const colors = uniqueValues(variationSource, 'color');
    const sizesFromVariations = ['PP', 'P', 'M', 'G', 'GG', 'XG'];
    const colorHex = {
        cinza: '#808080', cinzento: '#808080', gray: '#808080', grey: '#808080',
        preto: '#18191d', black: '#18191d',
        branco: '#faf7ed', white: '#faf7ed',
        azul: '#abc9ef', blue: '#abc9ef',
        verde: '#5d8c55', green: '#5d8c55',
        vermelho: '#d94b4b', red: '#d94b4b',
        amarelo: '#f0c84b', yellow: '#f0c84b',
        laranja: '#e8893d', orange: '#e8893d',
        rosa: '#e894ad', pink: '#e894ad',
        roxo: '#8c63b8', purple: '#8c63b8',
        marrom: '#8b5e3c', brown: '#8b5e3c'
    };

    const colorStyle = color => colorHex[color.toLowerCase()] || color;
    const makeButton = (tag, attributes, text = '') => {
        const button = document.createElement(tag);
        Object.entries(attributes).forEach(([name, value]) => button.setAttribute(name, value));
        button.textContent = text;
        return button;
    };

    const firstAvailableColorIndex = colors.findIndex(color => variationSource.some(v =>
        String(v.color).trim().toLowerCase() === color.toLowerCase() && isInStock(v)));

    colors.forEach((color, index) => {
        const colorInStock = variationSource.some(v =>
            String(v.color).trim().toLowerCase() === color.toLowerCase() && isInStock(v));
        const swatch = makeButton('button', {
            type: 'button', 'data-shop-color': color,
            class: `swatch${index === (firstAvailableColorIndex >= 0 ? firstAvailableColorIndex : 0) ? ' active' : ''}${colorInStock ? '' : ' unavailable'}`,
            'aria-label': `Cor ${color}${colorInStock ? '' : ' (fora de estoque)'}`,
            'aria-pressed': index === (firstAvailableColorIndex >= 0 ? firstAvailableColorIndex : 0) ? 'true' : 'false',
            'aria-disabled': colorInStock ? 'false' : 'true',
            title: colorInStock ? color : `${color} — Fora de estoque`
        });
        swatch.style.background = colorStyle(color);
        swatchesContainer.appendChild(swatch);
    });

    const renderSizes = selectedColor => {
        const firstAvailableSizeIndex = sizesFromVariations.findIndex(size => variationSource.some(v =>
            String(v.color).trim().toLowerCase() === String(selectedColor || '').toLowerCase() &&
            String(v.size).trim().toUpperCase() === size.toUpperCase() && isInStock(v)));
        sizesContainer.replaceChildren(...sizesFromVariations.map((size, index) => {
            const sizeInStock = variationSource.some(v =>
                String(v.color).trim().toLowerCase() === String(selectedColor || '').toLowerCase() &&
                String(v.size).trim().toUpperCase() === size.toUpperCase() && isInStock(v));
            return makeButton('button', {
                type: 'button', 'data-shop-size': size,
                class: `size-option${index === firstAvailableSizeIndex ? ' active' : ''}${sizeInStock ? '' : ' unavailable'}`,
                'aria-pressed': index === firstAvailableSizeIndex ? 'true' : 'false',
                'aria-disabled': sizeInStock ? 'false' : 'true',
                title: sizeInStock ? size : `${size} — Fora de estoque`
            }, sizeInStock ? size : `${size} — Fora de estoque`);
        }));
    };

    renderSizes(colors[0] || null);

    const swatches = document.querySelectorAll('.swatches [data-shop-color]');
    swatches.forEach(swatch => {
        swatch.addEventListener('click', () => {
            if (swatch.getAttribute('aria-disabled') === 'true') return;
            swatches.forEach(s => {
                s.classList.remove('active');
                s.setAttribute('aria-pressed', 'false');
            });
            swatch.classList.add('active');
            swatch.setAttribute('aria-pressed', 'true');
            renderSizes(swatch.dataset.shopColor);
            bindSizeSelection();
        });
    });

    function bindSizeSelection() {
        const sizes = document.querySelectorAll('.sizes [data-shop-size]');
        sizes.forEach(sizeBtn => {
            sizeBtn.addEventListener('click', () => {
                if (sizeBtn.getAttribute('aria-disabled') === 'true') return;
                sizes.forEach(s => {
                    s.classList.remove('active');
                    s.setAttribute('aria-pressed', 'false');
                });
                sizeBtn.classList.add('active');
                sizeBtn.setAttribute('aria-pressed', 'true');
            });
        });
    }
    bindSizeSelection();

    function getSelectedVariation() {
        const activeSwatch = document.querySelector('.swatches [data-shop-color].active');
        const activeSize = document.querySelector('.sizes [data-shop-size].active');
        const selectedColor = activeSwatch ? activeSwatch.dataset.shopColor : null;
        const selectedSize = activeSize ? activeSize.dataset.shopSize : null;

        if (variations.length === 0) return null;

        return variationSource.find(v =>
            (!selectedColor || String(v.color).toLowerCase() === selectedColor.toLowerCase()) &&
            (!selectedSize || String(v.size).toUpperCase() === selectedSize.toUpperCase()) &&
            isInStock(v)
        );
    }

    async function addToCart(options = { redirectCheckout: false }) {
        const qtyInput = document.getElementById('product-quantity');
        const quantity = Math.max(1, Math.min(20, parseInt(qtyInput ? qtyInput.value : '1', 10) || 1));
        const variation = getSelectedVariation();

        if (!variation) {
            window.alert('Esta combinação está fora de estoque.');
            return;
        }

        const activeSwatch = document.querySelector('.swatches [data-shop-color].active');
        const activeSize = document.querySelector('.sizes [data-shop-size].active');
        const colorName = activeSwatch ? activeSwatch.dataset.shopColor : (variation ? variation.color : '');
        const sizeName = activeSize ? activeSize.dataset.shopSize : (variation ? variation.size : 'M');

        const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
        const csrfToken = csrfTokenMeta ? csrfTokenMeta.content : '';
        const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : '';

        const addButton = document.getElementById('product-add');
        const buyButton = document.getElementById('buy-now');
        const prevAddText = addButton ? addButton.textContent : '';

        if (addButton) addButton.disabled = true;
        if (buyButton) buyButton.disabled = true;

        let addedViaServer = false;

        if (variation && variation.id && product && product.id) {
            try {
                const formData = new FormData();
                formData.append('idProduct', product.id);
                formData.append('idVariation', variation.id);
                formData.append('quantity', quantity);

                const headers = {};
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                const response = await fetch('/order/item', {
                    method: 'POST',
                    headers: headers,
                    body: formData
                });

                if (response.ok) {
                    addedViaServer = true;
                }
            } catch (err) {
                console.warn('Could not add item through server endpoint, falling back to local cart drawer.', err);
            }
        }

        if (!addedViaServer) {
            if (window.FtwCart && typeof window.FtwCart.add === 'function') {
                window.FtwCart.add({
                    idProduct: product.id,
                    idVariation: variation ? variation.id : null,
                    name: product.name,
                    productName: product.name,
                    price: product.price,
                    imageUrl: product.imageUrl,
                    image: product.imageUrl,
                    size: sizeName,
                    color: colorName,
                    variation: `${colorName ? colorName + ' / ' : ''}${sizeName}`,
                    quantity: quantity
                });
            }
        }

        if (addButton) {
            addButton.disabled = false;
            addButton.textContent = 'Adicionado!';
            setTimeout(() => { addButton.textContent = prevAddText; }, 1800);
        }
        if (buyButton) buyButton.disabled = false;

        if (options.redirectCheckout) {
            window.location.href = '/checkout';
        } else if (window.FtwCart && typeof window.FtwCart.open === 'function') {
            window.FtwCart.open();
        }
    }

    const addBtn = document.getElementById('product-add');
    if (addBtn) {
        addBtn.addEventListener('click', () => addToCart({ redirectCheckout: false }));
    }

    const buyBtn = document.getElementById('buy-now');
    if (buyBtn) {
        buyBtn.addEventListener('click', () => addToCart({ redirectCheckout: true }));
    }
})();
/*]]>*/