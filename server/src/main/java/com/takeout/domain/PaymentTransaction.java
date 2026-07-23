package com.takeout.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction", uniqueConstraints = @UniqueConstraint(name = "uk_payment_request_id", columnNames = "request_id"))
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 64) private String requestId;
    @Column(nullable = false) private Long orderId;
    @Column(nullable = false, length = 32) private String orderNo;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 20) private String channel;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getRequestId() { return requestId; } public void setRequestId(String requestId) { this.requestId = requestId; }
    public Long getOrderId() { return orderId; } public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; } public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getChannel() { return channel; } public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
