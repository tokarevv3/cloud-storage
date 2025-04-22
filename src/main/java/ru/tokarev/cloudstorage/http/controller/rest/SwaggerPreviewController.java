package ru.tokarev.cloudstorage.http.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.service.FileService;
import ru.tokarev.cloudstorage.service.PreviewService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SwaggerPreviewController {

    private final PreviewService previewService;

    @GetMapping("/browse")
    public Map<Long, String> getListOfFilesAndFolders(@RequestParam(required = false, defaultValue = "") String path) {
        return previewService.getListOfFilesAndFoldersInFolder(path);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public boolean uploadFileToFolder(@RequestParam(required = false, defaultValue = "") String path,
                                      @RequestPart("file") MultipartFile file) {
        return previewService.uploadFile(file, path);
    }

    private String getPath(HttpServletRequest request) {
        try {
            String folderPath = java.net.URLDecoder.decode(request.getRequestURI().split("api")[1], StandardCharsets.UTF_8);
            log.info("Extracted path: {}", folderPath);
            return folderPath;
        } catch (Exception e) {
            return "";
        }
    }
}
