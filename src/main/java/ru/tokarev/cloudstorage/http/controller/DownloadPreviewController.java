package ru.tokarev.cloudstorage.http.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.service.FileService;
import ru.tokarev.cloudstorage.service.S3Service;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class DownloadPreviewController {

    private final FileService fileService;
    private final S3Service s3Service;


    @GetMapping("/preview")
    public ResponseEntity<byte[]> previewFile(@RequestParam Long previewFileId) throws IOException {
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
