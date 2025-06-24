package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.FolderCreateEditDto;
import ru.tokarev.cloudstorage.service.database.FileService;
import ru.tokarev.cloudstorage.service.database.FolderService;
import ru.tokarev.cloudstorage.service.security.AuthService;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileFolderService {

    private final AuthService authService;
    private final S3Service s3Service;
    private final FolderService folderService;
    private final FileService fileService;

    public FolderCreateEditDto createFolder(String folderName, String folderPath) {

        Bucket userBucket = authService.getAuthenticatedUser().getBucket();
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

        log.info("Folder created: {}", folder);
        try {
            folderService.createFolder(folderName, parentFolder.getId());
        } catch (FileNotFoundException e) {
            return null;
        }
        return folder;
    }

    public Boolean deleteFile(Long fileId) throws FileNotFoundException {
        User authenticatedUser = authService.getAuthenticatedUser();

        Bucket userBucket = authenticatedUser.getBucket();
        File deleteFile = fileService.getFile(fileId).orElseThrow(FileNotFoundException::new);
        Bucket deleteFileBucket = deleteFile.getFolder().getBucket();

        if (userBucket.getId().equals(deleteFileBucket.getId())) {
            fileService.deleteFile(fileId);
            String fileFullPath = deleteFile.getFilePath() + deleteFile.getFileName();
            s3Service.deleteFile(deleteFileBucket.getName(), fileFullPath);
            return true;
        } else {
            return false;
        }
    }

    public Boolean deleteFolder(Long folderId) throws FileNotFoundException {
        Long userId = authService.getAuthenticatedUser().getId();
        Folder folderById = folderService.getFolderById(folderId).orElseThrow(FileNotFoundException::new);
        Long folderUserId = folderById.getBucket().getUser().getId();
        if (userId.equals(folderUserId)) {
            log.info("Trying to delete folder: {}", folderId);
            return folderService.deleteFolderById(folderId);
        } else {
            return false;
        }
    }

    public File moveFile(Long fileId, Long newParentFolderId) {
        log.info("Trying to move file {}", fileId);

        User user = authService.getAuthenticatedUser();
        Long userBucketId = user.getBucket().getId();

        File file = fileService.getFile(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        Folder newFolder = folderService.getFolderById(newParentFolderId).orElseThrow(IllegalArgumentException::new);

        Long fileBucketId = file.getFolder().getBucket().getId();
        Long newFolderBucketId = newFolder.getBucket().getId();

        if (!userBucketId.equals(fileBucketId) || !userBucketId.equals(newFolderBucketId)) {
            log.info("Failed to move file — access denied or invalid bucket");
            return null;
        }

        String oldPath = file.getFilePath().substring(1) + file.getFileName();
        String newPath = newFolder.getPath().substring(1) + newFolder.getName() + "/" + file.getFileName();
        String bucketName = file.getFolder().getBucket().getName();

        file.setFolder(newFolder);
        file.setFilePath(newFolder.getPath() + newFolder.getName() + "/");

        s3Service.updateFile(bucketName, oldPath, newPath);

        return fileService.saveFile(file);
    }

    public Folder moveFolder(Long folderId, Long newParentFolderId) throws FileNotFoundException {
        User user = authService.getAuthenticatedUser();
        Bucket userBucket = user.getBucket();
        Long userBucketId = userBucket.getId();
        String bucketName = userBucket.getName();

        Folder folder = folderService.getFolderById(folderId).orElseThrow(FileNotFoundException::new);
        Folder newParent = folderService.getFolderById(newParentFolderId).orElseThrow(IllegalArgumentException::new);

        Long folderBucketId = folder.getBucket().getId();
        Long newParentBucketId = newParent.getBucket().getId();

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

    public boolean renameFile(Long fileId, String newName) {
        File file = fileService.getFile(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String bucketName = file.getFolder().getBucket().getName();
        String oldFileName = file.getFileName();
        String oldExtension = getFileExtension(oldFileName);

        String baseNewName = newName;
        String newExtension = getFileExtension(newName);

        if (newExtension.isEmpty()) {
            baseNewName = newName + oldExtension;
        } else if (!newExtension.equalsIgnoreCase(oldExtension)) {
            baseNewName = newName.substring(0, newName.lastIndexOf('.')) + oldExtension;
        }

        String fileOldFullPath = file.getFilePath().substring(1) + oldFileName;
        String fileNewFullPath = file.getFilePath().substring(1) + baseNewName;

        file.setFileName(baseNewName);
        fileService.saveFile(file);

        return s3Service.updateFile(bucketName, fileOldFullPath, fileNewFullPath);
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1) ? filename.substring(dotIndex) : "";
    }

    public boolean renameFolder(Long folderId, String newName) throws FileNotFoundException {
        Folder folderById = folderService.getFolderById(folderId).orElseThrow(FileNotFoundException::new);
        String bucketName = folderById.getBucket().getName();
        String folderPath = folderById.getPath().substring(1);
        String oldPath = folderPath + folderById.getName() + "/";
        String newPath = folderPath + newName + "/";

        folderById.setName(newName);

        folderService.save(folderById);

        folderService.updateFolderRecursive(folderById);

        return s3Service.updateFolderPath(bucketName, oldPath, newPath);
    }
}
