package ru.tokarev.cloudstorage.http.controller.rest;

import io.minio.Result;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tokarev.cloudstorage.service.FileService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileFolderController {

    private final FileService fileService;

    //TODO: Actual this method have to return Map? (or array) of name and types
    @GetMapping("/**")
    public ArrayList<String> getListOfFolders(HttpServletRequest request) {
        //String folderPath = request.getRequestURI().split("api/")[1];

        //TODO: change regex to RequestMapping value
        String folderPath = java.net.URLDecoder.decode(request.getRequestURI().split("api/")[1], StandardCharsets.UTF_8);
        if (folderPath.isEmpty()) {
            return null;
        }
        //TODO: change bucketName to user`s bucketName (via SecurityContextHolder)
        Iterable<Result<Item>> folderList = fileService.getFilesList("base-bucket", folderPath);
        ArrayList<String> folderNames = new ArrayList<>();

        try {
            for (Result<Item> folder : folderList) {
                folderNames.add(folder.get().objectName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folderNames;
    }

    @GetMapping("/id")
    public boolean check() {
        return true;
    }

}
