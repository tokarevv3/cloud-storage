package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PreviewService {

    private final FolderService folderService;
    private final FileService fileService;
    private final S3Service s3Service;
    private final UserService userService;

    public Map<Long, String> getListOfFilesAndFoldersInFolder(String path) {

        System.out.println(path);

        Folder folderByPath = folderService.getFolderByPath(path);
        System.out.println(folderByPath.getName());

        return Stream.concat(
                folderService.getFoldersInFolder(folderByPath).stream(),
                fileService.getFilesInFolder(folderByPath).stream()
        ).collect(Collectors.toMap(
                this::getIdFromObject,
                this::getNameFromObject
        ));
    }

    public boolean uploadFile(MultipartFile file, String path)  {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userService.findByUsername(principal.getUsername()).get().getId();

        try {
            if (fileService.uploadFile(
                    file.getOriginalFilename(),
                    path,
                    file.getSize(),
                    file.getContentType()) &&
            s3Service.uploadFile(
                    currentUserId,
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
