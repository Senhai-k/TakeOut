package com.takeout;

import com.takeout.repository.AdminAccountRepository;
import com.takeout.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
abstract class BaseIntegrationTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AdminAccountRepository adminAccountRepository;

    @BeforeEach
    void resetTokenVersions() {
        var users = appUserRepository.findAll();
        users.forEach(user -> user.setTokenVersion(0));
        appUserRepository.saveAllAndFlush(users);
        var accounts = adminAccountRepository.findAll();
        accounts.forEach(account -> account.setTokenVersion(0));
        accounts.forEach(account -> { account.setFailedLoginAttempts(0); account.setLockedUntil(null); });
        adminAccountRepository.saveAllAndFlush(accounts);
    }
}
