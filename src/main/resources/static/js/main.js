document.addEventListener("DOMContentLoaded", function () {
    
    // 1. Navbar Scroll Effect
    const navbar = document.querySelector('.navbar');
    
    if (navbar) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        });
    }

    // 2. Initialize AOS (Animate On Scroll)
    // Note: Cần import thư viện AOS trong HTML trước
    if (typeof AOS !== 'undefined') {
        AOS.init({
            duration: 1000, // Thời gian animation (ms)
            once: true,     // Chỉ chạy 1 lần khi cuộn xuống
            offset: 100,    // Khoảng cách kích hoạt
            easing: 'ease-out-cubic'
        });
    }

    // 3. Password Toggle (Ẩn/Hiện Mật khẩu)
    const togglePasswordButtons = document.querySelectorAll('.password-toggle');
    
    togglePasswordButtons.forEach(button => {
        button.addEventListener('click', function (e) {
            // Ngăn chặn hành vi mặc định (nếu là thẻ a hoặc button trong form)
            e.preventDefault();
            
            // Tìm input liên quan (giả sử input nằm cùng cha hoặc ngay trước button)
            // Cách 1: Tìm input trong cùng parent (input-group)
            const input = this.parentElement.querySelector('input');
            
            if (input) {
                // Toggle type
                const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                input.setAttribute('type', type);
                
                // Toggle icon
                const icon = this.querySelector('i');
                if (icon) {
                    icon.classList.toggle('fa-eye');
                    icon.classList.toggle('fa-eye-slash');
                }
            }
        });
    });

});
