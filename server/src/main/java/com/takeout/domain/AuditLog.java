package com.takeout.domain;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="audit_log")
public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long adminId;
 @Column(nullable=false,length=50) private String role;
 @Column(nullable=false,length=50) private String action;
 @Column(nullable=false,length=50) private String targetType;
 @Column(length=100) private String targetId;
 @Column(length=500) private String detail;
 @Column(nullable=false) private LocalDateTime createdAt;
 public Long getId(){return id;} public Long getAdminId(){return adminId;} public void setAdminId(Long v){adminId=v;}
 public String getRole(){return role;} public void setRole(String v){role=v;} public String getAction(){return action;} public void setAction(String v){action=v;}
 public String getTargetType(){return targetType;} public void setTargetType(String v){targetType=v;} public String getTargetId(){return targetId;} public void setTargetId(String v){targetId=v;}
 public String getDetail(){return detail;} public void setDetail(String v){detail=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
