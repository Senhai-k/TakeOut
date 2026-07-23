package com.takeout.config;

import com.takeout.service.SeedDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(prefix = "takeout.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
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
