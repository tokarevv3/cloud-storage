package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
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
                .orElse(null);
    }

    public MultipartFile downloadFile(Long fileId) {
        log.info("Trying to download file: " + fileId);
        Optional<File> file = fileService.getFile(fileId);
        String fileName = file.get().getFilePath()  + file.get().getFileName();
        fileName = fileName.substring(1);
        String bucket = file.get().getFolder().getBucketId().getName();
//        return s3Service.downloadFile(bucket,fileName);
        return null;
    }

    //TODO:: add logic to check if folder with that name already exist
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

    public Boolean deleteFile(Long fileId) {
        User authenticatedUser = loginService.getAuthenticatedUser();

        Long userBucketId = authenticatedUser.getBucket().getId();
        Long fileBucketId = fileService.getFile(fileId).get().getFolder().getBucketId().getId();
        if (userBucketId.equals(fileBucketId)) {
            fileService.deleteFile(fileId);
            return true;
        } else {
            return false;
        }
    }

    public Boolean deleteFolder(Long folderId) {
        Long userBucketId = loginService.getAuthenticatedUser().getBucket().getId();
        Folder folderById = folderService.getFolderById(folderId);
        Long folderBucketId = folderById.getBucketId().getId();
        if (userBucketId.equals(folderBucketId)) {
            log.info("Trying to delete folder: " + folderId);
            return folderService.deleteFolderById(folderId);
        } else {
            return false;
        }
    }

    public File moveFile(Long fileId, Long newParentFolderId) {
        log.info("Trying to move file {}", fileId);

        User user = loginService.getAuthenticatedUser();
        Long userBucketId = user.getBucket().getId();

        File file = fileService.getFile(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        Folder newFolder = folderService.getFolderById(newParentFolderId);

        Long fileBucketId = file.getFolder().getBucketId().getId();
        Long newFolderBucketId = newFolder.getBucketId().getId();

        if (!userBucketId.equals(fileBucketId) || !userBucketId.equals(newFolderBucketId)) {
            log.info("Failed to move file — access denied or invalid bucket");
            return null;
        }

        String oldPath = file.getFilePath().substring(1) + file.getFileName();
        String newPath = newFolder.getPath().substring(1) + newFolder.getName() + "/" + file.getFileName();
        String bucketName = file.getFolder().getBucketId().getName();

        file.setFolder(newFolder);
        file.setFilePath(newFolder.getPath() + newFolder.getName() + "/");

        s3Service.updateFilePath(bucketName, oldPath, newPath);

        return fileService.saveFile(file);
    }

    @Deprecated
    //TODO: Need update files in folder
    public Folder moveFolder(Long folderId, Long newParentFolderId) {
        User user = loginService.getAuthenticatedUser();
        Bucket userBucket = user.getBucket();
        Long userBucketId = userBucket.getId();
        String bucketName = userBucket.getName();

        Folder folder = folderService.getFolderById(folderId);
        Folder newParent = folderService.getFolderById(newParentFolderId);

        Long folderBucketId = folder.getBucketId().getId();
        Long newParentBucketId = newParent.getBucketId().getId();

        if (!userBucketId.equals(folderBucketId) || !userBucketId.equals(newParentBucketId)) {
            log.info("Failed to move folder — access denied or invalid bucket");
            return null;
        }

        String oldPath = folder.getPath().substring(1) + folder.getName() + "/";
        String newPath = newParent.getPath().substring(1) + newParent.getName() + "/";

        folder.setParent(newParent);
        folder.setPath("/" + newPath);

        folderService.updateFolderRecursive(folder);


        s3Service.updateFolderPath(bucketName, oldPath, newPath + folder.getName() + "/");

        return folderService.save(folder);
    }

    public ResponseEntity<byte[]> previewFile(Long previewFileId) throws IOException {
        log.info("Trying to preview file with id: {}", previewFileId);
        File file = fileService.getFile(previewFileId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Bucket fileBucket = file.getFolder().getBucketId();
        String filePath = file.getFilePath().substring(1) + file.getFileName();
        byte[] fileBytes = s3Service.downloadFile(fileBucket.getName(), filePath);

        log.info(MediaType.parseMediaType(file.getContentType()).toString());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(fileBytes);

    }
}
