package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.FileReadDto;
import ru.tokarev.cloudstorage.dto.FolderReadDto;
import ru.tokarev.cloudstorage.mapper.FileReadMapper;
import ru.tokarev.cloudstorage.mapper.FolderReadMapper;
import ru.tokarev.cloudstorage.service.database.FileService;
import ru.tokarev.cloudstorage.service.database.FolderService;
import ru.tokarev.cloudstorage.service.security.AuthService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreviewService {

    private final FolderService folderService;
    private final FileService fileService;
    private final FileReadMapper fileReadMapper;
    private final FolderReadMapper folderReadMapper;
    private final AuthService authService;
    private final PathResolverService pathResolverService;

    public Map<Long, String> getListOfFilesAndFoldersInFolder(String path) {

        User currentUser = authService.getAuthenticatedUser();
        Folder folderByPath = pathResolverService.resolveFolderByPath(path, currentUser.getBucket());

        if (folderByPath == null) {
            log.error("Cannot find folder owner. Maybe path is wrong or belongs to other bucket?");
            return null;
        }

        log.info("Found folder owner of path: {}", folderByPath);

        return Stream.concat(
                folderService.getFoldersInFolder(folderByPath).stream(),
                fileService.getFilesInFolder(folderByPath).stream()
        ).collect(Collectors.toMap(
                this::getIdFromObject,
                this::getNameFromObject
        ));
    }

    public Map<Long, String> findFiles(String search) {
        User currentUser = authService.getAuthenticatedUser();

        List<File> filesByUserIdAndName = fileService.findFilesByUserIdAndName(currentUser.getId(), search);

        return filesByUserIdAndName.stream()
                .collect(Collectors.toMap(
                        this::getIdFromObject,
                        this::getNameFromObject
                ));
    }

    private Long getIdFromObject(Object obj) {
        if (obj instanceof Folder) {
            return ((Folder) obj).getId();
        } else if (obj instanceof File) {
            return ((File) obj).getId();
        } else {
            throw new IllegalArgumentException("Unknown object type: " + obj.getClass());
        }
    }

    private String getNameFromObject(Object obj) {
        if (obj instanceof Folder) {
            return ((Folder) obj).getName() + "/";
        } else if (obj instanceof File) {
            return ((File) obj).getFileName();
        } else {
            throw new IllegalArgumentException("Unknown object type: " + obj.getClass());
        }
    }

    public FileReadDto getFile(Long fileId) {
        return fileService.getFile(fileId)
                .map(fileReadMapper::map)
                .orElse(null);
    }

    public FolderReadDto getFolder(Long folderId) {
        return folderService.getFolderById(folderId)
                .map(folderReadMapper::map)
                .orElse(null);
    }
}
