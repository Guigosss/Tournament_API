package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.LogoutService;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {
    private final UserRepository users;

    @Override
    @Transactional
    public void logout(Integer userId) {
        var user = users.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setTokenVersion(user.getTokenVersion() + 1);
        users.saveAndFlush(user);
    }
}