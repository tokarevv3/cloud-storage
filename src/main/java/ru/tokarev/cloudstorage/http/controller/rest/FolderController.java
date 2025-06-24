package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.service.FileFolderService;
import ru.tokarev.cloudstorage.service.PreviewService;
import ru.tokarev.cloudstorage.service.UploadDownloadService;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
public class FolderController {

    private final PreviewService previewService;
    private final UploadDownloadService uploadDownloadService;
    private final FileFolderService fileFolderService;

    @GetMapping("{folderId}")
    public ResponseEntity<?> getFolder(@PathVariable("folderId") Long folderId) {
        return ResponseEntity.ok(previewService.getFolder(folderId));
    }

    @PostMapping("{folderId}/upload")
    public ResponseEntity<?> uploadFile(@RequestBody MultipartFile file, @PathVariable String folderId) {
        return ResponseEntity.ok(uploadDownloadService.uploadFile(file, folderId));
    }

    @DeleteMapping("{folderId}/delete")
    public ResponseEntity<?> deleteFolder(@PathVariable Long folderId) throws FileNotFoundException {
        return ResponseEntity.ok(fileFolderService.deleteFolder(folderId));
    }

    @PatchMapping("{folderId}/move")
    public ResponseEntity<?> moveFolder(@PathVariable Long folderId, @RequestParam("newParentFolderId") Long newParentFolderId) throws FileNotFoundException {
        return ResponseEntity.ok(fileFolderService.moveFolder(folderId, newParentFolderId));
    }

    @PatchMapping("{folderId}/rename")
    public ResponseEntity<?> renameFolder(@PathVariable Long folderId, @RequestParam("newName") String newName) throws FileNotFoundException {
        return ResponseEntity.ok(fileFolderService.renameFolder(folderId, newName));
    }
}
