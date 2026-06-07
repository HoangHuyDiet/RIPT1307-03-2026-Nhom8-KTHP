package com.smartfinance.smart_finance_hub.config;

import com.smartfinance.smart_finance_hub.entity.Role;
import com.smartfinance.smart_finance_hub.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiRoleInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        createRoleIfMissing("PRO", "Nguoi dung goi Pro");
        createRoleIfMissing("VIP", "Nguoi dung goi VIP");
    }

    private void createRoleIfMissing(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(Role.builder()
                .name(name)
                .description(description)
                .build());
        }
    }
}
