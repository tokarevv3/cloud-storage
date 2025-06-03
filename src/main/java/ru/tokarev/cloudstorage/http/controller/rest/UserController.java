package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.dto.UserReadDto;
import ru.tokarev.cloudstorage.mapper.UserReadMapper;
import ru.tokarev.cloudstorage.service.LoginService;
import ru.tokarev.cloudstorage.service.PreviewService;
import ru.tokarev.cloudstorage.service.UserService;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserReadMapper userReadMapper;
    private final LoginService loginService;

    @GetMapping("/user")
    public ResponseEntity<?> getUser() {
        User currentUser = loginService.getAuthenticatedUser();

        Optional<UserReadDto> userReadDto = userService.findByUsername(currentUser.getEmail()).map(userReadMapper::map);
        return ResponseEntity.ok(userReadDto);
    }

    @PutMapping("/user/update")
    public ResponseEntity<?> updateUserName(@RequestBody UserCreateEditDto userCreateEditDto) {
        User currentUser = loginService.getAuthenticatedUser();
        return ResponseEntity.ok(userService.update(currentUser.getId(), userCreateEditDto));
    }

    @PatchMapping("/user/password")
    public ResponseEntity<?> updateUserPassword(@RequestBody String password) {
        log.info("Trying to update user password");
        User currentUser = loginService.getAuthenticatedUser();

        return ResponseEntity.ok(userService.updatePassword(currentUser.getId(), password));
    }
}
