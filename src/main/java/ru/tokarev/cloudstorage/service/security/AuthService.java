package ru.tokarev.cloudstorage.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.JwtResponse;
import ru.tokarev.cloudstorage.provider.JwtTokenProvider;
import ru.tokarev.cloudstorage.service.database.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public User getAuthenticatedUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        UserDetails currentUserPrincipal = (UserDetails) securityContext.getAuthentication().getPrincipal();

        return userService.findByUsername(currentUserPrincipal.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public JwtResponse authenticateUser(String email, String password) {

        if (email == null || password == null) {
            throw new UsernameNotFoundException("Missing email or password");
        }

        Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
        );
        log.info("Successfully logged in for user: {}", email);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return new JwtResponse(jwt);
    }
}