'use strict';
(() => {
  const $ = selector => document.querySelector(selector);
  const money = value => new Intl.NumberFormat('pt-BR', {
    style: 'currency', currency: 'BRL'
  }).format(value);
  const storageKey = 'vestelab_cart';

  let cart = [];
  try {
    const saved = JSON.parse(localStorage.getItem(storageKey) || '[]');
    cart = Array.isArray(saved) ? saved : [];
  } catch (_) {}

  function save() {
    localStorage.setItem(storageKey, JSON.stringify(cart));
    render();
  }

  function render() {
    const badge = $('#cart-badge');
    const count = cart.reduce((sum, item) => sum + (Number(item.quantity) || 1), 0);
    if (badge) {
      badge.textContent = count;
      badge.hidden = count === 0;
    }

    const total = $('#cart-total');
    if (total) {
      total.textContent = money(cart.reduce((sum, item) =>
        sum + (Number(item.price) || 0) * (Number(item.quantity) || 1), 0));
    }

    const items = $('#cart-items');
    if (items) {
      items.innerHTML = cart.length ? cart.map((item, index) => `
        <article class="cart-item">
          <img src="${item.image || '/img/favicon.svg'}" alt="${item.name || 'Camiseta'}" width="70" height="70">
          <div class="cart-item-body">
            <h3>${item.name || 'Camiseta'}</h3>
            <p class="small muted">Tam: ${item.size || 'M'} · Qtd: ${item.quantity || 1}</p>
            <strong>${money((item.price || 0) * (item.quantity || 1))}</strong>
          </div>
          <button class="icon-button" data-remove-cart="${index}" aria-label="Remover item">×</button>
        </article>`).join('')
        : '<p class="small muted" style="padding:20px;text-align:center">Seu carrinho está vazio.</p>';
    }

    const checkout = $('#checkout');
    if (checkout) checkout.disabled = cart.length === 0;
  }

  function add(item) {
    cart.push({ ...item, quantity: Number(item.quantity) || 1 });
    save();
  }

  function open() {
    const drawer = $('#cart-drawer');
    if (drawer && typeof drawer.showModal === 'function') {
      render();
      drawer.showModal();
    }
  }

  function close() {
    const drawer = $('#cart-drawer');
    if (drawer && drawer.open) drawer.close();
  }

  window.FtwCart = { add, open, close, getItems: () => [...cart] };

  document.addEventListener('click', event => {
    const remove = event.target.closest('[data-remove-cart]');
    if (remove) {
      cart.splice(Number(remove.dataset.removeCart), 1);
      save();
    }
  });

  $('#open-cart')?.addEventListener('click', open);
  $('#close-cart')?.addEventListener('click', close);
  $('#checkout')?.addEventListener('click', () => {
    close();
    window.location.href = '/checkout';
  });
  $('#full-cart-link')?.addEventListener('click', event => {
    event.preventDefault();
    close();
    window.location.href = '/cart';
  });

  render();
})();