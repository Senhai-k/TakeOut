package com.takeout.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_review")
public class OrderReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) private Long orderId;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private Long shopId;
    @Column(nullable = false) private Integer rating;
    @Column(length = 500) private String content;
    @Column(nullable = false) private LocalDateTime createdAt;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Long getShopId() { return shopId; } public void setShopId(Long shopId) { this.shopId = shopId; }
    public Integer getRating() { return rating; } public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
