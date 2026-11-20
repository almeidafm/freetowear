'use strict';
(() => {
  const activeRoutes = new Set(['/', '/artworks', '/products', '/universes']);
  const currentPath = window.location.pathname.replace(/\/$/, '') || '/';
  const primaryNav = document.querySelector('.header nav[aria-label*="Navega"]');

  if (primaryNav && activeRoutes.has(currentPath)) {
    primaryNav.querySelectorAll('a').forEach(link => {
      const linkPath = new URL(link.href, window.location.origin).pathname.replace(/\/$/, '') || '/';
      if (activeRoutes.has(linkPath) && linkPath === currentPath) {
        link.setAttribute('aria-current', 'page');
      } else {
        link.removeAttribute('aria-current');
      }
    });
  } else if (primaryNav) {
    primaryNav.querySelectorAll('a[aria-current]').forEach(link => link.removeAttribute('aria-current'));
  }

  const accountButton = document.querySelector('.account-button');
  const accountMenu = document.querySelector('#account-menu');
  if (accountButton && accountMenu) {
    accountButton.addEventListener('click', () => {
      const isOpen = !accountMenu.hidden;
      accountMenu.hidden = isOpen;
      accountButton.setAttribute('aria-expanded', String(!isOpen));
    });

    document.addEventListener('click', event => {
      if (!event.target.closest('.account-menu-container')) {
        accountMenu.hidden = true;
        accountButton.setAttribute('aria-expanded', 'false');
      }
    });
  }
})();