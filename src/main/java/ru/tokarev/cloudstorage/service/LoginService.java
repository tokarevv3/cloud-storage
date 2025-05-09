package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final BucketService bucketService;
    private final UserService userService;
    private final FolderService folderService;


    public void registerUser(UserCreateEditDto newUser) {

        log.info("Registering new user: " + newUser.getUsername());
        Optional<User> createdUser = userService.create(newUser);
        createdUser.ifPresent(user -> log.info("Successfully created user with id: " + user.getId()));

        log.info("Creating bucket for user id: " + createdUser.get().getId());
        Optional<Bucket> createdBucket = bucketService.createBucket(createdUser.get());
        createdBucket.ifPresent(bucket -> log.info("Successfully created bucket with id: " + bucket.getId()));

        log.info("Creating root folder for bucket: " + createdBucket.get().getName());
        Optional<Folder> createdRootFolder = folderService.createRootFolder(createdBucket.get());
        createdRootFolder.ifPresent(folder -> log.info("Successfully created root folder with id: " + folder.getId()));

    }

    public User getAuthenticatedUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        UserDetails currentUserPrincipal = (UserDetails) securityContext.getAuthentication().getPrincipal();

        return userService.findByUsername(currentUserPrincipal.getUsername()).get();
    }


}
