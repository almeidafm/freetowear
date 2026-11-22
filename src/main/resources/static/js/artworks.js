'use strict';

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-use-art]').forEach(button => {
        button.addEventListener('click', () => {
            const art = button.dataset.useArt?.replace(/^art:/, '');
            if (art) window.location.href = `/studio?art=${encodeURIComponent(art)}`;
        });
    });

    document.querySelectorAll('[data-download-art]').forEach(button => {
        button.addEventListener('click', () => {
            const art = button.dataset.downloadArt?.replace(/^art:/, '');
            if (!art) return;
            const link = document.createElement('a');
            link.href = `/img/${encodeURIComponent(art)}.png`;
            link.download = `${art}.png`;
            link.click();
        });
    });
});