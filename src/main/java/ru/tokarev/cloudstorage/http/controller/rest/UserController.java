package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.tokarev.cloudstorage.dto.UserReadDto;
import ru.tokarev.cloudstorage.mapper.UserReadMapper;
import ru.tokarev.cloudstorage.service.UserService;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserReadMapper userReadMapper;

    @GetMapping("/settings")
    public ResponseEntity<?> getUser() {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<UserReadDto> userReadDto = userService.findByUsername(principal.getUsername()).map(userReadMapper::map);
        return ResponseEntity.ok(userReadDto);
    }
}
