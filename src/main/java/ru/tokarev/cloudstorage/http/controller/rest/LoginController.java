package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tokarev.cloudstorage.dto.LoginRequest;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.exception.creation.CreationException;
import ru.tokarev.cloudstorage.service.security.AuthService;
import ru.tokarev.cloudstorage.service.security.RegisterService;

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


    private final RegisterService registerService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        log.info("User with login: {} is trying to log in.", loginRequest.getEmail());

        var response = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body("Wrong username or password");

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateEditDto userDto) {
        log.info("Registering user with login: {}", userDto.getEmail());

        try {
            registerService.registerUser(userDto);

            var response =  authService.authenticateUser(userDto.getEmail(), userDto.getRawPassword());
            if (response != null) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.badRequest().body("Wrong username or password");
        } catch (CreationException ce) {
            log.error("Registration error: {}", ce.getMessage());
            return ResponseEntity.badRequest().body("Ошибка регистрации: " + ce.getMessage());
        }
    }

}
