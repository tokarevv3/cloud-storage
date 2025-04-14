package ru.tokarev.cloudstorage.http.controller.rest;

import io.minio.Result;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.service.FileService;
import ru.tokarev.cloudstorage.service.PreviewService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PreviewController {

    private final FileService fileService;
    private final PreviewService previewService;

    //TODO: Actual this method have to return Map? (or array) of name and types
//    @GetMapping("/**")
//    public ArrayList<String> getListOfFolders(HttpServletRequest request) {
//        //String folderPath = request.getRequestURI().split("api/")[1];
//
//        //TODO: change regex to RequestMapping value
//        String folderPath = java.net.URLDecoder.decode(request.getRequestURI().split("api/")[1], StandardCharsets.UTF_8);
//        if (folderPath.isEmpty()) {
//            return null;
//        }
//        //TODO: change bucketName to user`s bucketName (via SecurityContextHolder)
//        Iterable<Result<Item>> folderList = fileService.getFilesList("base-bucket", folderPath);
//        ArrayList<String> folderNames = new ArrayList<>();
//
//        try {
//            for (Result<Item> folder : folderList) {
//                folderNames.add(folder.get().objectName());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return folderNames;
//    }

    @GetMapping("/**")
    public Map<Long, String> getListOfFilesAndFolders(HttpServletRequest request) {

        String folderPath;

        try {
            folderPath = java.net.URLDecoder.decode(request.getRequestURI().split("api")[1], StandardCharsets.UTF_8);
            log.info(folderPath);
        } catch (Exception e) {
            folderPath = "";
        }
        return previewService.getListOfFilesAndFoldersInFolder(folderPath);

    }

    @PostMapping("/**")
    public boolean uploadFileToFolder(HttpServletRequest request, MultipartFile file) {
        try {
            return fileService.uploadFile("base-bucket", file.getOriginalFilename(), file.getInputStream(), file.getSize());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @GetMapping("/id")
    public boolean check() {
        return true;
    }

}
