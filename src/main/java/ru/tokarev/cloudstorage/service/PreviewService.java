package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.FileReadDto;
import ru.tokarev.cloudstorage.dto.FolderCreateEditDto;
import ru.tokarev.cloudstorage.mapper.FileReadMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreviewService {

    private final FolderService folderService;
    private final FileService fileService;
    private final S3Service s3Service;
    private final FileReadMapper fileReadMapper;
    private final LoginService loginService;

    public Map<Long, String> getListOfFilesAndFoldersInFolder(String path) {

        User currentUser = loginService.getAuthenticatedUser();
        Folder folderByPath;

        if (path.equals("/")) {
            folderByPath = folderService.getFolderByNameAndBucket("root-folder", currentUser.getBucket());

        } else {

            String fullFolderPath = "/root-folder" + path;

            path = fullFolderPath.endsWith("/") ? fullFolderPath.substring(0, fullFolderPath.length() - 1) : fullFolderPath;
            int lastSlash = path.lastIndexOf('/');
            String folderPath = path.substring(0, lastSlash + 1);
            String folderName = path.substring(lastSlash + 1);
            folderByPath = folderService.getFolderByNameAndPathAndBucket(folderName, folderPath, currentUser.getBucket());
        }



//        Folder folderByPath = folderService.getFolderByPathAndBucket("/root-folder" + path, currentUser.getBucket());

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
        Folder folderByPath;

        if (path.equals("/")) {
            folderByPath = folderService.getFolderByNameAndBucket("root-folder", currentUser.getBucket());

        } else {

            String fullFolderPath = "/root-folder" + path;

            path = fullFolderPath.endsWith("/") ? fullFolderPath.substring(0, fullFolderPath.length() - 1) : fullFolderPath;
            int lastSlash = path.lastIndexOf('/');
            String folderPath = path.substring(0, lastSlash + 1);
            String folderName = path.substring(lastSlash + 1);
            folderByPath = folderService.getFolderByNameAndPathAndBucket(folderName, folderPath, currentUser.getBucket());
        }

        try {
            if (fileService.uploadFile(
                    file.getOriginalFilename(),
                    path + "/",
                    file.getSize(),
                    file.getContentType(),
                    folderByPath) &&
            s3Service.uploadFile(
                    currentUser.getId(),
                    path + "/" + file.getOriginalFilename(),
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


    public FileReadDto getFile(Long fileId) {
        return fileService.getFile(fileId)
                .map(fileReadMapper::map)
                .get();
    }

    public MultipartFile downloadFile(Long fileId) {
        log.info("Trying to download file: " + fileId);
        Optional<File> file = fileService.getFile(fileId);
        String fileName = "root-folder/" +  file.get().getFilePath()  + file.get().getFileName();
        String bucket = file.get().getFolder().getBucketId().getName();
        return s3Service.downloadFile(bucket,fileName);
    }

    public FolderCreateEditDto createFolder(String folderName, String folderPath) {

        Bucket userBucket = loginService.getAuthenticatedUser().getBucket();
        String fullFolderPath = "/root-folder" + folderPath;

        String path = fullFolderPath.endsWith("/") ? fullFolderPath.substring(0, fullFolderPath.length() - 1) : fullFolderPath;
        int lastSlash = path.lastIndexOf('/');
        String parentFolderPath = path.substring(0, lastSlash + 1);
        String parentFolderName = path.substring(lastSlash + 1);
        Folder parentFolder = folderService.getFolderByNameAndPathAndBucket(parentFolderName, parentFolderPath, userBucket);

        log.info(parentFolder.getName());

        FolderCreateEditDto folder = new FolderCreateEditDto(
                folderName,
                fullFolderPath,
                LocalDateTime.now(),
                parentFolder,
                userBucket
        );

        log.info(String.valueOf(folder));

        log.info("Folder created: " + folder);
        folderService.createFolder(folderName, parentFolder.getId());
         return folder;
    }
}
