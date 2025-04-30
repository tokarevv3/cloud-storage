package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreviewService {

    private final FolderService folderService;
    private final FileService fileService;
    private final S3Service s3Service;
    private final UserService userService;
    private final LoginService loginService;

    public Map<Long, String> getListOfFilesAndFoldersInFolder(String path) {

        User currentUser = loginService.getAuthenticatedUser();

        log.info("Trying to get path: " + path);

        Folder folderByPath = folderService.getFolderByPathAndBucket(path, currentUser.getBucket());

        if (folderByPath == null) {
            log.info("Cannot find folder owner. Maybe path is wrong or belongs to other bucket?");
            return null;
        }

        log.info("Found folder owner of path: " + folderByPath);



        return Stream.concat(
                folderService.getFoldersInFolder(folderByPath).stream(),
                fileService.getFilesInFolder(folderByPath).stream()
        ).collect(Collectors.toMap(
                this::getIdFromObject,
                this::getNameFromObject
        ));
    }

    public boolean uploadFile(MultipartFile file, String path)  {

        User currentUser = loginService.getAuthenticatedUser();

        try {
            if (fileService.uploadFile(
                    file.getOriginalFilename(),
                    path,
                    file.getSize(),
                    file.getContentType()) &&
            s3Service.uploadFile(
                    currentUser.getId(),
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize())
            ) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private Long getIdFromObject(Object obj) {
        if (obj instanceof Folder) {
            return ((Folder) obj).getId();
        } else if (obj instanceof File) {
            return ((File) obj).getId();
        } else {
            throw new IllegalArgumentException("Unknown object type: " + obj.getClass());
        }
    }

    private String getNameFromObject(Object obj) {
        if (obj instanceof Folder) {
            return ((Folder) obj).getName() + "/";
        } else if (obj instanceof File) {
            return ((File) obj).getFileName();
        } else {
            throw new IllegalArgumentException("Unknown object type: " + obj.getClass());
        }
    }



}
