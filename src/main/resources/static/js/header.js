'use strict';
(() => {
  const activeRoutes = new Set(['/', '/artworks', '/shop', '/universes']);
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

  const accountAvatar = document.querySelector('.account-avatar');
  if (accountAvatar) {
    const avatarColors = [
      '#587c15',
      '#7a5aa6',
      '#2f6f6a',
      '#8a6d12',
      '#a8493a',
      '#6b4a82',
      '#3d5a3a',
      '#5f6b2a',
    ];
    accountAvatar.style.backgroundColor = avatarColors[Math.floor(Math.random() * avatarColors.length)];
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