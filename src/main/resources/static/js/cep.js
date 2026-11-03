/**
 * CEP address autocompletion helper with fallback support.
 *
 * Primary provider: ViaCEP (https://viacep.com.br/ws/{cep}/json/)
 * Fallback provider: BrasilAPI (https://brasilapi.com.br/api/cep/v1/{cep})
 *
 * Non-blocking:
 * Fields are never disabled or locked. If both APIs are down or fail,
 * the user can freely fill all address fields manually.
 */

async function fetchFromViaCep(cep, signal) {
    const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`, { signal });
    if (!response.ok) {
        throw new Error(`ViaCEP returned status ${response.status}`);
    }
    const data = await response.json();
    if (data.erro) {
        throw new Error('ViaCEP: CEP not found');
    }

    return {
        street: data.logradouro || '',
        neighborhood: data.bairro || '',
        city: data.localidade || '',
        state: (data.uf || '').toUpperCase()
    };
}

async function fetchFromBrasilApi(cep, signal) {
    const response = await fetch(`https://brasilapi.com.br/api/cep/v1/${cep}`, { signal });
    if (!response.ok) {
        throw new Error(`BrasilAPI returned status ${response.status}`);
    }
    const data = await response.json();

    return {
        street: data.street || '',
        neighborhood: data.neighborhood || '',
        city: data.city || '',
        state: (data.state || '').toUpperCase()
    };
}

async function lookupAddressByCep(cleanCep, signal) {
    // 1. Try Primary Provider (ViaCEP)
    try {
        return await fetchFromViaCep(cleanCep, signal);
    } catch (primaryError) {
        if (primaryError.name === 'AbortError') {
            throw primaryError;
        }
        console.warn('Primary CEP provider (ViaCEP) failed, attempting fallback (BrasilAPI)...', primaryError);
    }

    // 2. Try Fallback Provider (BrasilAPI)
    try {
        return await fetchFromBrasilApi(cleanCep, signal);
    } catch (fallbackError) {
        if (fallbackError.name === 'AbortError') {
            throw fallbackError;
        }
        console.warn('Fallback CEP provider (BrasilAPI) failed:', fallbackError);
        throw fallbackError;
    }
}

function setupCepAutocomplete({
    cepInput,
    streetInput,
    neighborhoodInput,
    cityInput,
    stateInput,
    numberInput
}) {
    if (!cepInput) return;

    let abortController = null;

    async function handleCepLookup() {
        const rawValue = cepInput.value || '';
        const cleanCep = rawValue.replace(/\D/g, '');

        if (cleanCep.length !== 8) {
            return;
        }

        if (abortController) {
            abortController.abort();
        }
        abortController = new AbortController();

        try {
            const address = await lookupAddressByCep(cleanCep, abortController.signal);
            if (!address) return;

            if (streetInput && address.street) {
                streetInput.value = address.street;
            }
            if (neighborhoodInput && address.neighborhood) {
                neighborhoodInput.value = address.neighborhood;
            }
            if (cityInput && address.city) {
                cityInput.value = address.city;
            }
            if (stateInput && address.state) {
                stateInput.value = address.state;
            }

            if (numberInput) {
                numberInput.focus();
            }
        } catch (error) {
            if (error.name !== 'AbortError') {
                console.warn('Could not autocomplete address via CEP services:', error.message);
            }
        }
    }

    cepInput.addEventListener('input', () => {
        const cleanCep = (cepInput.value || '').replace(/\D/g, '');
        if (cleanCep.length === 8) {
            handleCepLookup();
        }
    });

    cepInput.addEventListener('blur', () => {
        handleCepLookup();
    });
}

document.addEventListener('DOMContentLoaded', () => {
    // 1. Account page (#formAddAddress)
    const formAddAddress = document.getElementById('formAddAddress');
    if (formAddAddress) {
        setupCepAutocomplete({
            cepInput: formAddAddress.querySelector('input[name="cep"]'),
            streetInput: formAddAddress.querySelector('input[name="street"]'),
            neighborhoodInput: formAddAddress.querySelector('input[name="neighborhood"]'),
            cityInput: formAddAddress.querySelector('input[name="city"]'),
            stateInput: formAddAddress.querySelector('input[name="state"]'),
            numberInput: formAddAddress.querySelector('input[name="number"]')
        });
    }

    // 2. Checkout page (#new-address-form)
    const newAddressForm = document.getElementById('new-address-form');
    if (newAddressForm) {
        setupCepAutocomplete({
            cepInput: document.getElementById('cep'),
            streetInput: document.getElementById('street'),
            neighborhoodInput: document.getElementById('neighborhood'),
            cityInput: document.getElementById('city'),
            stateInput: document.getElementById('state'),
            numberInput: document.getElementById('number')
        });
    }
});