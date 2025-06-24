package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.service.database.FolderService;

@Service
@RequiredArgsConstructor
public class PathResolverService {
    private final FolderService folderService;

    public Folder resolveFolderByPath(String path, Bucket userBucket) {
        if (path.equals("/")) {
            return folderService.getFolderByNameAndBucket("root-folder", userBucket);
        } else {
            String fullFolderPath = "/root-folder" + path;
            path = fullFolderPath.endsWith("/") ? fullFolderPath.substring(0, fullFolderPath.length() - 1) : fullFolderPath;

            int lastSlash = path.lastIndexOf('/');

            String folderPath = path.substring(0, lastSlash + 1);
            String folderName = path.substring(lastSlash + 1);

            return folderService.getFolderByNameAndPathAndBucket(folderName, folderPath, userBucket);
        }
    }
}
