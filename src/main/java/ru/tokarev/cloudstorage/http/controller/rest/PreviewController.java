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
public class PreviewController {

    private final FileService fileService;
    private final PreviewService previewService;

    @GetMapping("/**")
    public Map<Long, String> getListOfFilesAndFolders(HttpServletRequest request) {

        String folderPath = getPath(request);

        return previewService.getListOfFilesAndFoldersInFolder(folderPath);

    }

    @PostMapping(value = "/**")
    public boolean uploadFileToFolder(HttpServletRequest request, @RequestPart MultipartFile file) {
        return previewService.uploadFile(file, getPath(request));
    }


    private String getPath(HttpServletRequest request) {

        String folderPath;

        try {
            folderPath = java.net.URLDecoder.decode(request.getRequestURI().split("api")[1], StandardCharsets.UTF_8);
            log.info(folderPath);
        } catch (Exception e) {
            folderPath = "";
        }
        return folderPath;
    }

}
