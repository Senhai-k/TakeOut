package com.takeout.repository;
import com.takeout.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{List<AuditLog> findAllByOrderByCreatedAtDesc();}
