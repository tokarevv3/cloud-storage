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
public class WildcardController {

    private final PreviewService previewService;

    @GetMapping("/**")
    public ResponseEntity<?> getListOfFilesAndFolders(HttpServletRequest request) {
        String folderPath = getPath(request);
        log.info("Trying to get to folder with path {}", folderPath);
        return ResponseEntity.ok(previewService.getListOfFilesAndFoldersInFolder(folderPath));
    }

    @PostMapping(value = "/**")
    public ResponseEntity<?> uploadFileToFolder(HttpServletRequest request,
                                      @RequestPart(required = false) MultipartFile file,
                                      @RequestParam(required = false) String folderName) {
        if (file != null) {
            log.info("Trying to upload file");
            return ResponseEntity.ok(previewService.uploadFile(file, getPath(request)));
        } else if (folderName != null) {
            String folderPath = getPath(request);
            return ResponseEntity.ok(previewService.createFolder(folderName, folderPath));
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
