package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
