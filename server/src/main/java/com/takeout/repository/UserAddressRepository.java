package com.takeout.repository;

import com.takeout.domain.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserIdAndIsDeletedOrderByIsDefaultDescUpdatedAtDesc(Long userId, Integer isDeleted);

    List<UserAddress> findByUserIdAndIsDeleted(Long userId, Integer isDeleted);

    Optional<UserAddress> findByIdAndUserIdAndIsDeleted(Long id, Long userId, Integer isDeleted);
}
