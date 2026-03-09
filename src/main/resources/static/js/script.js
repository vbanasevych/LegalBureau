document.addEventListener("DOMContentLoaded", function() {
    const toggleButtons = document.querySelectorAll('.toggle-password');

    toggleButtons.forEach(button => {
        button.addEventListener('click', function() {
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
});