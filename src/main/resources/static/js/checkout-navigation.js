// ============================================
// STEP NAVIGATION
// ============================================
function nextStep() {
    if (currentStep === 1) {
        if (!selectedAddressId) {
            alert('Please select a shipping address.');
            return;
        }
    }

    if (currentStep === 2) {
        if (!selectedPaymentMethod) {
            alert('Please select a payment method.');
            return;
        }
    }

    if (currentStep === 3) {
        const coupon = document.getElementById('coupon').value.trim();
        document.getElementById('selectedCouponId').value = coupon;
    }

    if (currentStep === 4) {
        const form = document.getElementById('checkout-form');
        form.requestSubmit();
        return;
    }

    updateStep(currentStep + 1);
}

function previousStep() {
    if (currentStep > 1) {
        updateStep(currentStep - 1);
    }
}


// ============================================
// STEP UPDATE & RENDERING
// ============================================
function updateStep(step) {
    document.querySelectorAll('.form-section').forEach(section => {
        section.classList.remove('active');
    });

    const currentSection = document.getElementById(`step${step}`);
    if (currentSection) {
        currentSection.classList.add('active');
    }

    updateProgressIndicators(step);

    updateProgressTitle(step);

    updateNavigationButtons(step);

    if (step === 4) {
        loadCurrentCart();
    }

    currentStep = step;
}

function updateProgressIndicators(step) {
    document.querySelectorAll('.step').forEach((element, index) => {
        const stepNumber = index + 1;

        element.classList.remove('active', 'completed');

        if (stepNumber < step) {
            element.classList.add('completed');
        } else if (stepNumber === step) {
            element.classList.add('active');
        }
    });
}

function updateProgressTitle(step) {
    const titles = [
        'Shipping Address',
        'Payment Information',
        'Coupon',
        'Order Review'
    ];

    document.getElementById('progressTitle').textContent = titles[step - 1];
}

function updateNavigationButtons(step) {
    const backButton = document.getElementById('backBtn');
    const nextButton = document.getElementById('nextBtn');

    backButton.style.display = step > 1 ? 'block' : 'none';

    nextButton.textContent = step === 4 ? 'Complete Order' : 'Continue';
}

// ============================================
// PAYMENT METHOD SELECTION
// ============================================
function selectPaymentMethod(paymentMethod) {
    selectedPaymentMethod = paymentMethod;
    renderReview();
}