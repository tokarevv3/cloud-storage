package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.JwtResponse;
import ru.tokarev.cloudstorage.dto.LoginRequest;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.provider.JwtTokenProvider;
import ru.tokarev.cloudstorage.service.LoginService;

import java.util.Optional;

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


    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        log.info("User with login: " + loginRequest.getEmail() + " is trying to log in.");

        return loginService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateEditDto userDto) {
        log.info("Registering user with login: " + userDto.getEmail());

        try {
            loginService.registerUser(userDto).orElseThrow(RuntimeException::new);

            return loginService.authenticateUser(userDto.getEmail(), userDto.getRawPassword());
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Ошибка регистрации: " + e.getMessage());
        }
    }

}
