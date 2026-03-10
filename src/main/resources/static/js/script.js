document.addEventListener("DOMContentLoaded", function () {
    const toggleButtons = document.querySelectorAll('.toggle-password');

    toggleButtons.forEach(button => {
        button.addEventListener('click', function () {
            const targetId = this.getAttribute('data-target');
            const passwordInput = document.getElementById(targetId);

            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                this.textContent = '🙈'; // Змінюємо іконку на "сховати"
            } else {
                passwordInput.type = 'password';
                this.textContent = '👁️'; // Змінюємо іконку на "показати"
            }
        });
    });

    const passwordInput = document.getElementById('registerPassword');
    const submitBtn = document.getElementById('submitBtn');

    if (passwordInput) {
        const reqLength = document.getElementById('req-length');
        const reqDigit = document.getElementById('req-digit');
        const reqSpec = document.getElementById('req-spec');

        if (submitBtn) submitBtn.disabled = true;

        function updateRequirement(el, isValid, text) {
            if (!el) return;
            if (isValid) {
                el.className = 'text-success mb-1';
                el.innerHTML = '✅ ' + text;
            } else {
                el.className = 'text-danger mb-1';
                el.innerHTML = '❌ ' + text;
            }
        }

        passwordInput.addEventListener('input', function () {
            const val = this.value;

            const isDigitValid = /\d/.test(val);
            const isLengthValid = val.length >= 8 && val.length <= 20;
            const isSpecValid = /[!_@#$%^&*()+=^.\-]/.test(val);

            updateRequirement(reqLength, isLengthValid, 'Від 8 до 20 символів');
            updateRequirement(reqDigit, isDigitValid, 'Мінімум одна цифра');
            updateRequirement(reqSpec, isSpecValid, 'Мінімум один спецсимвол (! _ @ # $ % ^ & *)');

            if (submitBtn) {
                submitBtn.disabled = !(isDigitValid && isLengthValid && isSpecValid);
            }
        });
    }
});