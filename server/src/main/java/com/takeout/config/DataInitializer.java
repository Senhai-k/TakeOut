package com.takeout.config;

import com.takeout.service.SeedDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SeedDataService seedDataService;

    public DataInitializer(SeedDataService seedDataService) {
        this.seedDataService = seedDataService;
    }

    @Override
    public void run(String... args) {
        seedDataService.seedIfMissing();
    }
}
