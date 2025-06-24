package ru.tokarev.cloudstorage.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.exception.creation.BucketCreationException;
import ru.tokarev.cloudstorage.exception.creation.CreationException;
import ru.tokarev.cloudstorage.exception.creation.RootFolderCreationException;
import ru.tokarev.cloudstorage.exception.creation.UserCreationException;
import ru.tokarev.cloudstorage.service.database.BucketService;
import ru.tokarev.cloudstorage.service.database.FolderService;
import ru.tokarev.cloudstorage.service.database.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {

    private final BucketService bucketService;
    private final UserService userService;
    private final FolderService folderService;


    public void registerUser(UserCreateEditDto newUserDto) throws CreationException {
        User user = userService.create(newUserDto)
                .orElseThrow(() -> new UserCreationException("User creation failed"));

        Bucket bucket = bucketService.createBucket(user)
                .orElseThrow(() -> new BucketCreationException("Bucket creation failed"));

        Folder rootFolder = folderService.createRootFolder(bucket)
                .orElseThrow(() -> new RootFolderCreationException("Root folder creation failed"));

        log.info("User {} registered successfully with bucket {} and folder {}", user.getEmail(), bucket.getName(), rootFolder.getName());
    }

}
