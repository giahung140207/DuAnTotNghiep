document.addEventListener("DOMContentLoaded", function () {
    
    // 1. Navbar Scroll Effect
    const navbar = document.querySelector('.navbar');
    
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });

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

});
