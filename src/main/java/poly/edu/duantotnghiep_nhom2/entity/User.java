package poly.edu.duantotnghiep_nhom2.entity;
import poly.edu.duantotnghiep_nhom2.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User { // Bỏ extends BaseEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(precision = 18, scale = 2)
    private BigDecimal balance; // Số dư ví

    @Column(name = "avatar_url")
    private String avatarUrl;

    // --- TÍCH ĐIỂM ---
    @Column(name = "loyalty_points", columnDefinition = "int default 0")
    private Integer loyaltyPoints = 0; // Điểm hiện tại (Max 10)

    @Column(name = "accumulated_minutes", columnDefinition = "bigint default 0")
    private Long accumulatedMinutes = 0L; // Tổng phút tích lũy để đổi điểm

    @Column(name = "last_point_update")
    private LocalDateTime lastPointUpdate; // Thời gian cập nhật điểm cuối cùng

    // --- GETTERS AND SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints == null ? 0 : loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public Long getAccumulatedMinutes() {
        return accumulatedMinutes == null ? 0L : accumulatedMinutes;
    }

    public void setAccumulatedMinutes(Long accumulatedMinutes) {
        this.accumulatedMinutes = accumulatedMinutes;
    }

    public LocalDateTime getLastPointUpdate() {
        return lastPointUpdate;
    }

    public void setLastPointUpdate(LocalDateTime lastPointUpdate) {
        this.lastPointUpdate = lastPointUpdate;
    }
}
