package com.technofuturtic.tournament_api.dal.initializers;

import com.technofuturtic.tournament_api.dal.repositories.RoleRepository;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dl.entities.RoleEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Seed implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

        if(userRepository.count() == 0) {
            RoleEntity userRole = new RoleEntity("user");
            RoleEntity adminRole = new RoleEntity("admin");

            roleRepository.save(userRole);
            roleRepository.save(adminRole);

            String email = "test@test.be";

            String password = passwordEncoder.encode("Test1234=");

            List<UserEntity> users = List.of(
                    new UserEntity(
                            "user",
                            email,
                            password,
                            userRole
                    ),
                    new UserEntity(
                            "admin",
                            email,
                            password,
                            adminRole
                    )
            );

            userRepository.saveAll(users);
        }
    }
}