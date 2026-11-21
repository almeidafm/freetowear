 let userEmail = "";

    function goToStep(stepNumber) {
        document.querySelectorAll('.step').forEach(el => el.classList.remove('active'));
        document.getElementById('step' + stepNumber).classList.add('active');
        document.querySelectorAll('[id$="Message"]').forEach(el => el.textContent = "");
    }

    async function requestCode() {
        userEmail = document.getElementById('emailInput').value;
        const msgEl = document.getElementById('step1Message');

        if (!userEmail.includes('@')) {
            msgEl.textContent = "Por favor, insira um e-mail válido.";
            return;
        }

        try {
            const response = await fetch('/password/request-code', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [document.querySelector('meta[name="_csrf_header"]').content]:
                    document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify({ contact: userEmail })
            });

            if (response.ok) {
                document.getElementById('displayEmail').textContent = userEmail;
                goToStep(2);
            } else {
                msgEl.textContent = "Erro ao enviar código. Tente novamente.";
            }
        } catch (error) {
            msgEl.textContent = "Erro de conexão.";
        }
    }

    async function verifyCode() {
        const code = document.getElementById('codeInput').value;
        const msgEl = document.getElementById('step2Message');

        try {
            const response = await fetch('/password/verify-code', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [document.querySelector('meta[name="_csrf_header"]').content]:
                    document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify({ contact: userEmail, code: code })
            });

            if (response.ok) {
                goToStep(3);
            } else {
                const data = await response.json();
                msgEl.textContent = data.message || "Código inválido ou expirado.";
            }
        } catch (error) {
            msgEl.textContent = "Erro de conexão.";
        }
    }

    async function resetPassword() {
        const newPass = document.getElementById('newPasswordInput').value;
        const confirmPass = document.getElementById('confirmPasswordInput').value;
        const code = document.getElementById('codeInput').value;
        const msgEl = document.getElementById('step3Message');

        if (newPass.length < 6) {
            msgEl.textContent = "A senha deve ter pelo menos 6 caracteres.";
            return;
        }
        if (newPass !== confirmPass) {
            msgEl.textContent = "As senhas não coincidem.";
            return;
        }

        try {
            const response = await fetch('/password/reset', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [document.querySelector('meta[name="_csrf_header"]').content]:
                    document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify({
                    contact: userEmail,
                    code: code,
                    newPassword: newPass
                })
            });

            if (response.ok) {
                msgEl.className = "success";
                msgEl.textContent = "Senha alterada com sucesso! Redirecionando para o login...";
                setTimeout(() => {
                    window.location.href = "/login";
                }, 2000);
            } else {
                const data = await response.json();
                msgEl.textContent = data.message || "Erro ao redefinir a senha.";
            }
        } catch (error) {
            msgEl.textContent = "Erro de conexão.";
        }
    }