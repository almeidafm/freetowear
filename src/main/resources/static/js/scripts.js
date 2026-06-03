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