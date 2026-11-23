document.addEventListener("DOMContentLoaded", () => {
    const password = document.getElementById("password");
    const passwordRequirements = document.getElementById("passwordRequirements");

    if (password && passwordRequirements) {
        const rules = {
            length: value => value.length >= 8,
            uppercase: value => /[A-Z]/.test(value),
            lowercase: value => /[a-z]/.test(value),
            number: value => /\d/.test(value),
            special: value => /[^a-zA-Z\d]/.test(value)
        };

        const updatePasswordRequirements = () => {
            const value = password.value;
            Object.entries(rules).forEach(([name, test]) => {
                const item = passwordRequirements.querySelector(`[data-requirement="${name}"]`);
                const valid = test(value);
                item.classList.toggle("is-valid", valid);
                item.querySelector(".requirement-icon").textContent = valid ? "✓" : "○";
            });
        };

        password.addEventListener("input", updatePasswordRequirements);
        updatePasswordRequirements();
    }

    const passwordToggle = document.getElementById("passwordToggle");
    if (password && passwordToggle) {
        passwordToggle.addEventListener("click", () => {
            const showingPassword = password.type === "text";
            password.type = showingPassword ? "password" : "text";
            passwordToggle.setAttribute("aria-label", showingPassword ? "Mostrar senha" : "Ocultar senha");
            passwordToggle.setAttribute("aria-pressed", String(!showingPassword));
            passwordToggle.querySelector("span").textContent = showingPassword ? "◉" : "◌";
        });
    }

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