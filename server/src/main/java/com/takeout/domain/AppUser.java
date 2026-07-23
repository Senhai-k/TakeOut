package com.takeout.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "openid", nullable = false, unique = true, length = 64) private String openId;
    @Column(name = "unionid", length = 64) private String unionId;
    @Column(nullable = false, length = 50) private String nickname;
    @Column(length = 20) private String phone;
    @Column(nullable = false) private Integer status;
    @Column(nullable = false, columnDefinition = "integer default 0") private Integer tokenVersion = 0;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getOpenId() { return openId; } public void setOpenId(String openId) { this.openId = openId; }
    public String getUnionId() { return unionId; } public void setUnionId(String unionId) { this.unionId = unionId; }
    public String getNickname() { return nickname; } public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public Integer getTokenVersion() { return tokenVersion; } public void setTokenVersion(Integer tokenVersion) { this.tokenVersion = tokenVersion; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
