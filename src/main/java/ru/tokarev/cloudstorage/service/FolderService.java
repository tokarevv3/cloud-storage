package ru.tokarev.cloudstorage.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.repositorty.FolderRepository;
import ru.tokarev.cloudstorage.dto.FolderCreateEditDto;
import ru.tokarev.cloudstorage.dto.FolderReadDto;
import ru.tokarev.cloudstorage.mapper.FolderCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.FolderReadMapper;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final BucketService bucketService;
    private final FolderCreateEditMapper folderCreateEditMapper;
    private final FolderReadMapper folderReadMapper;
    private final MinioClient minioClient;

    public Optional<FolderReadDto> createFolder(String folderName, Long folderId) {
        Folder parentFolder = folderRepository.getFolderById(folderId);
        String path = parentFolder.getPath() + "/" + folderName;

        FolderCreateEditDto folderCreateEditDto = new FolderCreateEditDto(
                folderName,
                path,
                LocalDateTime.now(),
                parentFolder,
                parentFolder.getBucketId());

        try {
            minioClient.putObject(PutObjectArgs.builder()
                            .bucket(parentFolder.getBucketId().getName())
                            .object( path + "/" + folderName+ "/confirmation")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, 0)
                    .build());
            return Optional.of(folderCreateEditDto)
                    .map(folderCreateEditMapper::map)
                    .map(folderRepository::saveAndFlush)
                    .map(folderReadMapper::map);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Folder createRootFolder(String bucketName) {
        Folder createdFolder = Folder.builder()
                .name("root-folder")
                .path("/")
                .uploadedAt(LocalDateTime.now())
                .parent(null)
                .bucketId(bucketService.getBucketByName(bucketName)) //may be null
                .build();
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object("root-folder/confirmation")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, 0)
                    .build());
            folderRepository.saveAndFlush(createdFolder);
            return createdFolder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Folder getFolderByPath(String path) {
        return folderRepository.getFolderByPath(path);
    }

    public Map<Long, String> getListInCurrentFolder(Folder folder) {
        return Stream.concat(
                folderRepository.getAllFoldersByParentId(folder).stream(),
                folderRepository.getAllFilesByParentId(folder).stream()
        ).collect(Collectors.toMap(
                this::getIdFromObject, // метод, извлекающий ID
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

    public Folder getFolderById(Long id) {
        return folderRepository.getFolderById(id);
    }


}
