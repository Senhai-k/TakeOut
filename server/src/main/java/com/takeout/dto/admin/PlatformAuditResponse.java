package com.takeout.dto.admin;
import java.time.LocalDateTime;
public record PlatformAuditResponse(Long id,Long adminId,String role,String action,String targetType,String targetId,String detail,LocalDateTime createdAt){}
