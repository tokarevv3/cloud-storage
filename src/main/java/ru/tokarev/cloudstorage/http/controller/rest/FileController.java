package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tokarev.cloudstorage.service.PreviewService;

import java.io.FileNotFoundException;


@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final PreviewService previewService;

    @GetMapping
    public ResponseEntity<?> findFiles(@RequestParam("search") String search) {
        return ResponseEntity.ok(previewService.findFiles(search));
    }

    @GetMapping("{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("fileId") Long downloadFileId) {
        return previewService.getFileToClient(downloadFileId);
    }

    @GetMapping("{fileId}")
    public ResponseEntity<?> getFile(@PathVariable("fileId") Long fileId) {
        return ResponseEntity.ok(previewService.getFile(fileId));
    }

    @DeleteMapping("{fileId}/delete")
    public ResponseEntity<?> deleteFile(@PathVariable("fileId") Long fileId) throws FileNotFoundException {
        return ResponseEntity.ok(previewService.deleteFile(fileId));
    }

    @PatchMapping("{fileId}/move")
    public ResponseEntity<?> moveFile(@PathVariable Long fileId, @RequestParam("newParentFolderId") Long newParentFolderId) {
        return ResponseEntity.ok(previewService.moveFile(fileId, newParentFolderId));
    }

    @PatchMapping("{fileId}/rename")
    public ResponseEntity<?> renameFile(@PathVariable Long fileId, @RequestParam("newName") String newName) {
        return ResponseEntity.ok(previewService.renameFile(fileId, newName));
    }
}
