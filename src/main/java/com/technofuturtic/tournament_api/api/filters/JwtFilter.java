package com.technofuturtic.tournament_api.api.filters;

import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.api.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            if (!jwtUtils.validateToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired access token");
                return;
            }
            var entity = userRepository.findWithRoleById(jwtUtils.getId(token)).orElse(null);
            if (entity == null || entity.isDeleted() || !jwtUtils.matchesTokenVersion(token, entity)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User no longer exists");
                return;
            }
            UserContext user = new UserContext(entity.getId(), entity.getUsername(), entity.getRole().getName());

            UsernamePasswordAuthenticationToken upt = new UsernamePasswordAuthenticationToken(
                    user,
                    token,
                    user.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(upt);
        }

        filterChain.doFilter(request, response);
    }
}