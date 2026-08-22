fetch('/account/address')
    .then(res => res.json())
    .then(addresses => {
        const select = document.querySelector('select[name="idAddress"]');

        addresses.forEach(address => {
            const option = document.createElement('option');
            option.value = address.id;
            option.text = `${address.street}, ${address.number} - ${address.neighborhood}, ${address.city} - ${address.state}, ${address.cep}`;
            option.selected = address.defaultAddress;
            select.appendChild(option);
        });
    });

fetch('/category')
    .then(res => res.json())
    .then(categories => {
        const select = document.querySelector('select[name="categoryId"]');

        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.text = category.name;
            select.appendChild(option);
        });
    });

fetch('/order/cart')
    .then(res => res.json())
    .then(order => {
        console.log('Cart order:', order);
        document.getElementById('formFinishOrder').action = `/order/${order.id}/finish`;
    });
document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
});

async function loadCategories() {
    const select = document.querySelector('select[name="categoryId"]');

    if (!select) return;

    try {
        const response = await fetch('/category');

        if (!response.ok) {
            throw new Error('Failed to load categories');
        }

        const categories = await response.json();

        categories.forEach(category => {
            const option = document.createElement('option');

            option.value = category.id;
            option.textContent = category.name;

            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}