function closeEmailVerificationModal() {
    const modal = document.getElementById("emailVerificationModal");
    if (modal) {
        modal.style.display = "none";
    }
}

async function sendVerificationCode() {
    const button = document.querySelector("#verificationStepOne .verify-email-button");
    const metaCsrf = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');

    if (!metaCsrf || !metaHeader) return;

    const csrfToken = metaCsrf.content;
    const csrfHeader = metaHeader.content;

    if (button) {
        button.disabled = true;
        button.textContent = "Enviando...";
    }

    try {
        const response = await fetch("/email-verification/send", {
            method: "POST",
            headers: {
                [csrfHeader]: csrfToken
            }
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Não foi possível enviar o código.");
        }

        const stepOne = document.getElementById("verificationStepOne");
        const stepTwo = document.getElementById("verificationStepTwo");
        const codeInput = document.getElementById("verificationCode");

        if (stepOne) stepOne.style.display = "none";
        if (stepTwo) stepTwo.style.display = "block";
        if (codeInput) codeInput.focus();

    } catch (error) {
        console.error("Verification email error:", error);
        alert(error.message);
    } finally {
        if (button) {
            button.disabled = false;
            button.textContent = "Verificar e-mail";
        }
    }
}

async function verifyEmailCode() {
    const codeInput = document.getElementById("verificationCode");
    const errorElement = document.getElementById("verificationError");
    const metaCsrf = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');

    if (!codeInput || !metaCsrf || !metaHeader) return;

    const code = codeInput.value.trim();
    const csrfToken = metaCsrf.content;
    const csrfHeader = metaHeader.content;

    if (errorElement) errorElement.style.display = "none";

    if (!/^\d{6}$/.test(code)) {
        if (errorElement) {
            errorElement.textContent = "Digite um código válido de 6 dígitos.";
            errorElement.style.display = "block";
        }
        return;
    }

    try {
        const response = await fetch("/email-verification/verify", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ code: code })
        });

        const data = await response.json();

        if (!response.ok) {
            if (errorElement) {
                errorElement.textContent = data.message || "Código inválido.";
                errorElement.style.display = "block";
            }
            return;
        }

        closeEmailVerificationModal();
        alert("E-mail verificado com sucesso.");

    } catch (error) {
        console.error("Verification error:", error);
        if (errorElement) {
            errorElement.textContent = "Ocorreu um erro ao verificar o código.";
            errorElement.style.display = "block";
        }
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const termsLink = document.getElementById("termsLink");
    const termsModal = document.getElementById("termsModal");
    const closeTermsModal = document.getElementById("closeTermsModal");
    const termsFrame = document.getElementById("termsFrame");
    const termsCheckbox = document.getElementById("termsAccepted");
    const confirmTermsRead = document.getElementById("confirmTermsRead");

    const privacyLink = document.getElementById("privacyLink");
    const privacyModal = document.getElementById("privacyModal");
    const closePrivacyModal = document.getElementById("closePrivacyModal");
    const privacyFrame = document.getElementById("privacyFrame");
    const privacyCheckbox = document.getElementById("privacyAccepted");
    const confirmPrivacyRead = document.getElementById("confirmPrivacyRead");

    if (termsCheckbox && termsLink) {
        let termsConfirmed = false;

        termsCheckbox.addEventListener("click", (event) => {
            if (!termsConfirmed) {
                event.preventDefault();
                alert("Você precisa ler e confirmar os Termos de Uso antes de aceitá-los.");
                termsLink.focus();
            }
        });

        termsLink.addEventListener("click", (event) => {
            event.preventDefault();
            if (termsModal) {
                termsModal.hidden = false;
                if (closeTermsModal) closeTermsModal.focus();
            }
        });

        if (closeTermsModal) {
            closeTermsModal.addEventListener("click", () => {
                if (termsModal) termsModal.hidden = true;
            });
        }

        if (termsModal) {
            termsModal.addEventListener("click", (event) => {
                if (event.target === termsModal) {
                    termsModal.hidden = true;
                }
            });
        }

        if (termsFrame) {
            termsFrame.addEventListener("load", () => {
                try {
                    const termsDocument = termsFrame.contentDocument;
                    const termsWindow = termsFrame.contentWindow;

                    termsDocument.addEventListener("scroll", () => {
                        const reachedBottom = termsWindow.scrollY + termsWindow.innerHeight
                            >= termsDocument.documentElement.scrollHeight - 8;

                        if (reachedBottom && confirmTermsRead) {
                            confirmTermsRead.disabled = false;
                        }
                    });
                } catch (e) {
                    // Caso o iframe seja de origem cruzada ou haja restrição
                    if (confirmTermsRead) confirmTermsRead.disabled = false;
                }
            });
        }

        if (confirmTermsRead) {
            confirmTermsRead.addEventListener("click", () => {
                termsConfirmed = true;
                if (termsModal) termsModal.hidden = true;
                termsCheckbox.focus();
            });
        }
    }

    if (privacyCheckbox && privacyLink) {
        let privacyConfirmed = false;

        privacyCheckbox.addEventListener("click", (event) => {
            if (!privacyConfirmed) {
                event.preventDefault();
                alert("Você precisa ler e confirmar a Política de Privacidade antes de aceitá-la.");
                privacyLink.focus();
            }
        });

        privacyLink.addEventListener("click", (event) => {
            event.preventDefault();
            if (privacyModal) {
                privacyModal.hidden = false;
                if (closePrivacyModal) closePrivacyModal.focus();
            }
        });

        if (closePrivacyModal) {
            closePrivacyModal.addEventListener("click", () => {
                if (privacyModal) privacyModal.hidden = true;
            });
        }

        if (privacyModal) {
            privacyModal.addEventListener("click", (event) => {
                if (event.target === privacyModal) {
                    privacyModal.hidden = true;
                }
            });
        }

        if (privacyFrame) {
            privacyFrame.addEventListener("load", () => {
                try {
                    const privacyDocument = privacyFrame.contentDocument;
                    const privacyWindow = privacyFrame.contentWindow;

                    privacyDocument.addEventListener("scroll", () => {
                        const reachedBottom = privacyWindow.scrollY + privacyWindow.innerHeight
                            >= privacyDocument.documentElement.scrollHeight - 8;

                        if (reachedBottom && confirmPrivacyRead) {
                            confirmPrivacyRead.disabled = false;
                        }
                    });
                } catch (e) {
                    if (confirmPrivacyRead) confirmPrivacyRead.disabled = false;
                }
            });
        }

        if (confirmPrivacyRead) {
            confirmPrivacyRead.addEventListener("click", () => {
                privacyConfirmed = true;
                if (privacyModal) privacyModal.hidden = true;
                privacyCheckbox.focus();
            });
        }
    }
});