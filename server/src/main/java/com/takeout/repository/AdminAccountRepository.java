package com.takeout.repository;

import com.takeout.domain.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {

    Optional<AdminAccount> findByUsernameAndStatus(String username, Integer status);

    List<AdminAccount> findByRoleOrderByIdAsc(String role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AdminAccount> findLockedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AdminAccount a where a.username = :username")
    Optional<AdminAccount> findLockedByUsername(@Param("username") String username);
}
