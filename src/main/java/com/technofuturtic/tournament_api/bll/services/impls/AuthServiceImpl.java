package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.role.RoleNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserAlreadyExistException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserInvalidPasswordException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.AuthService;
import com.technofuturtic.tournament_api.dal.repositories.RoleRepository;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dl.entities.RoleEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserEntity register(UserEntity user) {
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        RoleEntity role = roleRepository.findByName("user")
                .orElseThrow(() -> new RoleNotFoundException("Role 'user' not found"));

        user.setRole(role);

        return userRepository.save(user);
    }

    @Override
    public UserEntity login(String username, String password) {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new UserInvalidPasswordException("Invalid password for user " + username);
        }

        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return (UserDetails) userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
    }
}
