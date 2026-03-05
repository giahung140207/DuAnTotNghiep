// Admin Global Scripts

document.addEventListener("DOMContentLoaded", function() {
    
    // Hàm cập nhật Badge thông báo
    function updateBadges() {
        fetch('/admin/api/notifications')
            .then(response => response.json())
            .then(data => {
                // Cập nhật Badge Tin nhắn
                updateBadge('badge-support', data.unreadMessages);
                
                // Cập nhật Badge Booking
                updateBadge('badge-booking', data.pendingBookings);
            })
            .catch(error => console.error('Error fetching notifications:', error));
    }

    function updateBadge(id, count) {
        var badge = document.getElementById(id);
        if (badge) {
            if (count > 0) {
                badge.innerText = count;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        }
    }

    // Gọi ngay khi load trang
    updateBadges();

    // Polling mỗi 5 giây
    setInterval(updateBadges, 5000);
});
