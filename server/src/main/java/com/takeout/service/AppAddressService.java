package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.domain.UserAddress;
import com.takeout.dto.app.AddressRequest;
import com.takeout.dto.app.AddressResponse;
import com.takeout.exception.BusinessException;
import com.takeout.repository.UserAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppAddressService {

    private static final Long DEFAULT_USER_ID = 1L;

    private final UserAddressRepository userAddressRepository;

    public AppAddressService(UserAddressRepository userAddressRepository) {
        this.userAddressRepository = userAddressRepository;
    }

    public List<AddressResponse> listAddresses() {
        return userAddressRepository.findByUserIdAndIsDeletedOrderByIsDefaultDescUpdatedAtDesc(DEFAULT_USER_ID, 0)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        LocalDateTime now = LocalDateTime.now();
        UserAddress address = new UserAddress();
        address.setUserId(DEFAULT_USER_ID);
        applyRequest(address, request);
        address.setIsDeleted(0);
        address.setCreatedAt(now);
        address.setUpdatedAt(now);
        if (address.getIsDefault() == 1) {
            clearDefault();
        }
        return toResponse(userAddressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        UserAddress address = getAddressEntity(id);
        applyRequest(address, request);
        address.setUpdatedAt(LocalDateTime.now());
        if (address.getIsDefault() == 1) {
            clearDefaultExcept(address.getId());
        }
        return toResponse(userAddressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id) {
        UserAddress address = getAddressEntity(id);
        address.setIsDeleted(1);
        address.setUpdatedAt(LocalDateTime.now());
        userAddressRepository.save(address);
    }

    @Transactional
    public AddressResponse setDefault(Long id) {
        UserAddress address = getAddressEntity(id);
        clearDefaultExcept(address.getId());
        address.setIsDefault(1);
        address.setUpdatedAt(LocalDateTime.now());
        return toResponse(userAddressRepository.save(address));
    }

    UserAddress getAddressEntity(Long id) {
        return userAddressRepository.findByIdAndUserIdAndIsDeleted(id, DEFAULT_USER_ID, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "地址不存在"));
    }

    private void applyRequest(UserAddress address, AddressRequest request) {
        address.setReceiverName(request.receiverName());
        address.setReceiverPhone(request.receiverPhone());
        address.setProvince(request.province());
        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setDetail(request.detail());
        address.setHouseNumber(request.houseNumber());
        address.setIsDefault(Boolean.TRUE.equals(request.isDefault()) ? 1 : 0);
    }

    private void clearDefault() {
        clearDefaultExcept(null);
    }

    private void clearDefaultExcept(Long keepId) {
        userAddressRepository.findByUserIdAndIsDeleted(DEFAULT_USER_ID, 0)
                .stream()
                .filter(address -> keepId == null || !address.getId().equals(keepId))
                .forEach(address -> {
                    address.setIsDefault(0);
                    address.setUpdatedAt(LocalDateTime.now());
                    userAddressRepository.save(address);
                });
    }

    private AddressResponse toResponse(UserAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getReceiverName(),
                address.getReceiverPhone(),
                address.getProvince(),
                address.getCity(),
                address.getDistrict(),
                address.getDetail(),
                address.getHouseNumber(),
                address.getIsDefault() == 1
        );
    }
}
