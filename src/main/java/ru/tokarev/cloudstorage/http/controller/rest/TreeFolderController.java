package ru.tokarev.cloudstorage.http.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.dto.FolderTreeNode;
import ru.tokarev.cloudstorage.service.security.AuthService;
import ru.tokarev.cloudstorage.service.database.FolderService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TreeFolderController {

    private final FolderService folderService;
    private final AuthService authService;

    @GetMapping("/folder-tree")
    public ResponseEntity<List<FolderTreeNode>> getFoldersInTree() {

        Bucket bucket = authService.getAuthenticatedUser().getBucket();
        log.info("Trying to get tree of folders for userbucket: {}", bucket.getName());
        List<FolderTreeNode> tree = folderService.getUserFolderTree(bucket);
        return ResponseEntity.ok(tree);
    }
}
