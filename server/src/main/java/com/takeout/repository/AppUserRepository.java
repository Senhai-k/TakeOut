package com.takeout.repository;

import com.takeout.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByOpenIdAndStatus(String openId, Integer status);
    Optional<AppUser> findByOpenId(String openId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AppUser> findLockedById(Long id);
}
