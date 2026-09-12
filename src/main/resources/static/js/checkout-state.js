// ============================================
// GLOBAL STATE VARIABLES
// ============================================

let currentStep = 1;

let selectedAddressId = null;

let selectedAddress = null;

let addresses = [];

let selectedPaymentMethod = null;

let currentCart = null;


// ============================================
// UTILITY FUNCTIONS
// ============================================
function formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(value || 0);
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