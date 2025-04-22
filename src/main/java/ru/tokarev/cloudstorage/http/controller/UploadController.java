package ru.tokarev.cloudstorage.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.service.FileService;

import java.io.IOException;

@RequestMapping("/api")
@RequiredArgsConstructor
@Controller
public class UploadController {


    private final FileService fileService;

//    @PostMapping("/upload")
//    @ResponseBody
//    public ResponseEntity<?> uploadFiles(
//            @RequestParam("files") List<MultipartFile> files,
//            @RequestParam("folder") String folderPath) {
//        try {
//            List<String> uploadedFiles = fileService.uploadFiles(files, folderPath);
//            return ResponseEntity.ok().body(
//                    Map.of(
//                            "message", "Files uploaded successfully",
//                            "uploadedFiles", uploadedFiles
//                    )
//            );
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(
//                    Map.of("message", "Upload failed: " + e.getMessage())
//            );
//        }
//    }

//    @PostMapping("/upload/file")
//    @ResponseBody
//    public boolean upload(MultipartFile file) {
//        try {
//            return fileService.uploadFile("base-bucket", file.getOriginalFilename(), file.getInputStream(), file.getSize());
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
//        return false;
//    }
//
//    @GetMapping("/upload")
//    public String uploadPage() {
//        return "upload";
//    }
}
