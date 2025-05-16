package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.dto.UserReadDto;
import ru.tokarev.cloudstorage.mapper.UserReadMapper;
import ru.tokarev.cloudstorage.service.LoginService;
import ru.tokarev.cloudstorage.service.PreviewService;
import ru.tokarev.cloudstorage.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserReadMapper userReadMapper;
    private final LoginService loginService;
    private final PreviewService previewService;

    @GetMapping("/settings")
    public ResponseEntity<?> getUser() {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<UserReadDto> userReadDto = userService.findByUsername(principal.getUsername()).map(userReadMapper::map);
        return ResponseEntity.ok(userReadDto);
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateUser(@RequestBody UserCreateEditDto userCreateEditDto) {
        User currentUser = loginService.getAuthenticatedUser();

//        return previewService.updateUser(currentUser.getId(), userCreateEditDto);

        return ResponseEntity.ok(userService.update(currentUser.getId(), userCreateEditDto));
    }
}
