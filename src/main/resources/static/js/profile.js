(() => {
  const onlyDigits = (value) => value.replace(/\D/g, '');

  const formatPhone = (value) => {
    const digits = onlyDigits(value).slice(0, 11);

    if (digits.length <= 2) return digits.length ? `(${digits}` : '';
    if (digits.length <= 6) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
    if (digits.length <= 10) {
      return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
    }
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
  };

  const formatCpf = (value) => {
    const digits = onlyDigits(value).slice(0, 11);
    let formatted = digits;

    if (digits.length > 3) formatted = `${digits.slice(0, 3)}.${digits.slice(3)}`;
    if (digits.length > 6) formatted = `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`;
    if (digits.length > 9) formatted = `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`;

    return formatted;
  };

  const phoneInput = document.querySelector('input[name="phone"]');
  const cpfInput = document.querySelector('input[name="cpf"]');
  const birthDateInput = document.querySelector('input[name="birthDate"]');

  const formatBirthDate = (value) => {
    const digits = onlyDigits(value).slice(0, 8);
    if (digits.length <= 2) return digits;
    if (digits.length <= 4) return `${digits.slice(0, 2)}/${digits.slice(2)}`;
    return `${digits.slice(0, 2)}/${digits.slice(2, 4)}/${digits.slice(4)}`;
  };

  const birthDateToIso = (value) => {
    const match = value.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    return match ? `${match[3]}-${match[2]}-${match[1]}` : value;
  };

  if (phoneInput) phoneInput.addEventListener('input', () => {
    phoneInput.value = formatPhone(phoneInput.value);
  });

  if (cpfInput) cpfInput.addEventListener('input', () => {
    cpfInput.value = formatCpf(cpfInput.value);
  });

  if (birthDateInput) birthDateInput.addEventListener('input', () => {
    birthDateInput.value = formatBirthDate(birthDateInput.value);
  });

  const form = document.getElementById('profile-edit-form');
  if (form) form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (phoneInput) phoneInput.value = onlyDigits(phoneInput.value);
    if (cpfInput) cpfInput.value = onlyDigits(cpfInput.value);
    if (birthDateInput) birthDateInput.value = birthDateToIso(birthDateInput.value);

    const feedback = form.querySelector('.profile-edit-feedback');
    const button = form.querySelector('button');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const headers = {'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'};
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    button.disabled = true;
    feedback.textContent = 'Salvando...';

    try {
      const response = await fetch('/account', {
        method: 'PATCH',
        headers,
        body: new URLSearchParams(new FormData(form))
      });
      if (!response.ok) throw new Error('Não foi possível salvar os dados.');
      window.location.href = '/account';
    } catch (error) {
      feedback.textContent = error.message;
      button.disabled = false;
    }
  });

  const addMissingMarker = (element, before = null) => {
    if (!element || element.querySelector('.missing-data-marker')) return;
    const marker = document.createElement('span');
    marker.className = 'missing-data-marker';
    marker.textContent = '*';
    marker.setAttribute('aria-label', 'Dado não preenchido');
    if (before) element.insertBefore(marker, before);
    else element.appendChild(marker);
  };

  document.querySelectorAll('.profile-data div').forEach((row) => {
    const value = row.querySelector('dd');
    const rawValue = value?.textContent.trim() || '';

    if (!value.textContent.trim() || value.textContent.trim() === '—') {
      addMissingMarker(row.querySelector('dt'));
    }
  });

  document.querySelectorAll('.profile-phone').forEach((value) => {
    if (value.textContent.trim() !== '—') value.textContent = formatPhone(value.textContent);
  });

  document.querySelectorAll('.profile-cpf').forEach((value) => {
    if (value.textContent.trim() !== '—') value.textContent = formatCpf(value.textContent);
  });

  document.querySelectorAll('.profile-edit-form label').forEach((label) => {
    const input = label.querySelector('input');
    const fieldLabel = label.querySelector('.field-label');
    if (input && fieldLabel && (!input.value.trim() || input.value === 'null')) {
      addMissingMarker(fieldLabel);
    }
  });
})();