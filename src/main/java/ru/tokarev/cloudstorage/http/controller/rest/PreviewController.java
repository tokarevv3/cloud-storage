package ru.tokarev.cloudstorage.http.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.service.PreviewService;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PreviewController {

    private final PreviewService previewService;

    @GetMapping("/**")
    public ResponseEntity<?> getListOfFilesAndFolders(HttpServletRequest request,
                                                      @RequestParam(required = false) Long fileId,
                                                      @RequestParam(required = false) Long downloadFileId,
                                                      @RequestParam(required = false) Long previewFileId) {

        String folderPath = getPath(request);
        log.info("Trying to get to folder with path {}", folderPath);

        if (fileId != null) {
            return ResponseEntity.ok(previewService.getFile(fileId));
        } else if (downloadFileId != null) {
            return ResponseEntity.ok(previewService.downloadFile(downloadFileId));
        } else if (previewFileId != null) {
            try {
                log.info("Trying to preview file with id {}", previewFileId);
                return previewService.previewFile(previewFileId);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        } else {
            return ResponseEntity.ok(previewService.getListOfFilesAndFoldersInFolder(folderPath));
        }
    }

    @PostMapping(value = "/**")
    public ResponseEntity<?> uploadFileToFolder(HttpServletRequest request,
                                      @RequestPart(required = false) MultipartFile file,
                                      @RequestParam(required = false) String folderName) {
        if (file != null) {
            return ResponseEntity.ok(previewService.uploadFile(file, getPath(request)));
        } else if (folderName != null) {
            String folderPath = getPath(request);
            return ResponseEntity.ok(previewService.createFolder(folderName, folderPath));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/**")
    public ResponseEntity<?> deleteFileFromFolder(@RequestParam(required = false) Long fileId,
                                                  @RequestParam(required = false) Long folderId) {

        if (fileId != null) {
            return ResponseEntity.ok(previewService.deleteFile(fileId));
        } else if (folderId != null) {
            return ResponseEntity.ok(previewService.deleteFolder(folderId));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/**")
    public ResponseEntity<?> moveFileOrFolder(@RequestParam(required = false) Long fileId,
                                              @RequestParam(required = false) Long folderId,
                                              @RequestParam Long newParentFolderId) {
        if (fileId != null) {
            return ResponseEntity.ok(previewService.moveFile(fileId, newParentFolderId));
        } else if (folderId != null) {
            return ResponseEntity.ok(previewService.moveFolder(folderId, newParentFolderId));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    private String getPath(HttpServletRequest request) {

        String folderPath;

        try {
            folderPath = java.net.URLDecoder.decode(request.getRequestURI().split("api")[1], StandardCharsets.UTF_8);
        } catch (Exception e) {
            folderPath = "";
        }
        return folderPath;
    }

}
