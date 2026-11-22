'use strict';
(() => {
  const $ = selector => document.querySelector(selector);
  const money = value => new Intl.NumberFormat('pt-BR', {
    style: 'currency', currency: 'BRL'
  }).format(Number(value) || 0);
  const storageKey = 'vestelab_cart';
  let cart = [];
  let serverCart = false;

  function render() {
    const entries = serverCart ? (cart.items || []) : cart;
    const count = entries.reduce((sum, item) => sum + (Number(item.quantity) || 1), 0);
    const badge = $('#cart-badge');
    if (badge) {
      badge.textContent = count;
      badge.hidden = count === 0;
    }

    const total = $('#cart-total');
    if (total) total.textContent = money(serverCart ? cart.productsValue : cart.reduce(
      (sum, item) => sum + (Number(item.price) || 0) * (Number(item.quantity) || 1), 0));

    const items = $('#cart-items');
    if (items) {
      items.innerHTML = entries.length ? entries.map((item, index) => `
        <article class="cart-item">
          <img src="${item.imageUrl || item.image || '/img/favicon.svg'}" alt="${item.productName || item.name || 'Camiseta'}" width="70" height="70">
          <div class="cart-item-body">
            <h3>${item.productName || item.name || 'Camiseta'}</h3>
            <p class="small muted">${item.variation || `Tam: ${item.size || 'M'}`} · Qtd: ${item.quantity || 1}</p>
            <strong>${money(item.subtotal != null ? item.subtotal : (item.price || 0) * (item.quantity || 1))}</strong>
          </div>
          <button class="icon-button" data-remove-cart="${index}" aria-label="Remover item">×</button>
        </article>`).join('')
        : '<p class="small muted" style="padding:20px;text-align:center">Seu carrinho está vazio.</p>';
    }
    const checkout = $('#checkout');
    if (checkout) checkout.disabled = entries.length === 0;
  }

  function loadLocalCart() {
    try {
      const saved = JSON.parse(localStorage.getItem(storageKey) || '[]');
      cart = Array.isArray(saved) ? saved : [];
    } catch (_) { cart = []; }
    serverCart = false;
    render();
  }

  async function loadCart() {
    try {
      const response = await fetch('/order/current', { headers: { Accept: 'application/json' } });
      if (!response.ok) throw new Error('No server cart');
      cart = await response.json();
      serverCart = true;
      render();
    } catch (_) {
      loadLocalCart();
    }
  }

  async function remove(index) {
    if (!serverCart) {
      cart.splice(index, 1);
      localStorage.setItem(storageKey, JSON.stringify(cart));
      render();
      return;
    }
    const item = cart.items[index];
    if (!item) return;
    const body = new URLSearchParams({ idProduct: item.idProduct, idVariation: item.idVariation });
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const headers = {};
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    const response = await fetch('/order/cart/remove', { method: 'POST', headers, body });
    if (!response.ok) throw new Error('Não foi possível remover o item.');
    await loadCart();
  }

  function add(item) {
    cart.push({ ...item, quantity: Number(item.quantity) || 1 });
    localStorage.setItem(storageKey, JSON.stringify(cart));
    serverCart = false;
    render();
  }

  function open() {
    const drawer = $('#cart-drawer');
    if (drawer && typeof drawer.showModal === 'function') {
      loadCart();
      drawer.showModal();
    }
  }

  function close() {
    const drawer = $('#cart-drawer');
    if (drawer && drawer.open) drawer.close();
  }

  window.FtwCart = { add, open, close, getItems: () => [...(serverCart ? cart.items || [] : cart)] };

  document.addEventListener('click', event => {
    const removeButton = event.target.closest('[data-remove-cart]');
    if (removeButton) {
      removeButton.disabled = true;
      remove(Number(removeButton.dataset.removeCart)).catch(() => {
        removeButton.disabled = false;
        window.alert('Não foi possível remover o item do carrinho.');
      });
    }
  });
  $('#open-cart')?.addEventListener('click', open);
  $('#close-cart')?.addEventListener('click', close);
  $('#checkout')?.addEventListener('click', () => { close(); window.location.href = '/checkout'; });
  $('#full-cart-link')?.addEventListener('click', event => { event.preventDefault(); close(); window.location.href = '/cart'; });

  loadCart();
})();