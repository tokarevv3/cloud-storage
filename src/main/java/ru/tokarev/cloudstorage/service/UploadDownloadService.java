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
import ru.tokarev.cloudstorage.service.database.FileService;
import ru.tokarev.cloudstorage.service.security.AuthService;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadDownloadService {

    private final FileService fileService;
    private final S3Service s3Service;
    private final AuthService authService;
    private final PathResolverService pathResolverService;

    public ResponseEntity<byte[]> downloadFile(Long previewFileId) {
        log.info("Trying to download file with id: {}", previewFileId);
        File file = fileService.getFile(previewFileId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Bucket fileBucket = file.getFolder().getBucket();
        String filePath = file.getFilePath().substring(1) + file.getFileName();
        byte[] fileBytes = s3Service.downloadFile(fileBucket.getName(), filePath);

        log.info(MediaType.parseMediaType(file.getContentType()).toString());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(fileBytes);

    }

    public boolean uploadFile(MultipartFile file, String path)  {

        User currentUser = authService.getAuthenticatedUser();
        Folder folderByPath = pathResolverService.resolveFolderByPath(path, currentUser.getBucket());

        try {
            log.info("Uploading file: {}", file.getOriginalFilename());
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
}
