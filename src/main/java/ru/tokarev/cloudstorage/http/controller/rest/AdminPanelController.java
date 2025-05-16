package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tokarev.cloudstorage.dto.BucketReadDto;
import ru.tokarev.cloudstorage.dto.UserReadDto;
import ru.tokarev.cloudstorage.service.BucketService;
import ru.tokarev.cloudstorage.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminPanelController {

    private final UserService userService;
    private final BucketService bucketService;

    @GetMapping("/users")
    public List<UserReadDto> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/buckets")
    public List<BucketReadDto> getAllBuckets() {
        return bucketService.findAll();
    }

    @PatchMapping("{userId}/admin")
    public boolean makeUserAdmin(@PathVariable Long userId) {
        return userService.makeUserAdmin(userId);
    }

    @DeleteMapping("{userId}/delete")
    public boolean deleteUser(@PathVariable Long userId) {
        return userService.delete(userId);
    }
}
