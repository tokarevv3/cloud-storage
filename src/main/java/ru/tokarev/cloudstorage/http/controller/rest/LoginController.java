package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.tokarev.cloudstorage.dto.JwtResponse;
import ru.tokarev.cloudstorage.dto.LoginRequest;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.provider.JwtTokenProvider;
import ru.tokarev.cloudstorage.service.LoginService;
import ru.tokarev.cloudstorage.service.UserService;

/**
 *  Front-end server send to /api/auth/login end-point loginRequest object with username and password.
 *  Controller trying to authenticate that user, and if success then set authentication in SecurityContextHolder
 *  and generate token of user and return it.
 */

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        log.info("User with login: " + loginRequest.getUsername() + " is trying to log in.");
        Authentication authentication = null;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            log.info("Successfully logged in for user: " + loginRequest.getUsername());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Wrong username or password.");
        }

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateEditDto userDto) {
        log.info("Registering user with login: " + userDto.getUsername());

        try {
            loginService.registerUser(userDto);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDto.getUsername(),
                            userDto.getRawPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            log.error("Registration error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Ошибка регистрации: " + e.getMessage());
        }
    }

}
