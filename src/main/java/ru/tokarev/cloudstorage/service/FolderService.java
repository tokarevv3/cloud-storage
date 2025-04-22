package ru.tokarev.cloudstorage.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.Bucket;
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
    private final S3Service s3Service;

    public Optional<FolderReadDto> createFolder(String folderName, Long folderId) {
        Folder parentFolder = folderRepository.getFolderById(folderId);
        String path = parentFolder.getPath() + "/" + folderName;

        FolderCreateEditDto folderCreateEditDto = new FolderCreateEditDto(
                folderName,
                path,
                LocalDateTime.now(),
                parentFolder,
                parentFolder.getBucketId());

        if (s3Service.createFolder(parentFolder.getBucketId().getName(), folderCreateEditDto.getName(), folderCreateEditDto.getPath())) {
            return Optional.of(folderCreateEditDto)
                    .map(folderCreateEditMapper::map)
                    .map(folderRepository::saveAndFlush)
                    .map(folderReadMapper::map);

        }


        return Optional.empty();
    }

    public Folder createRootFolder(String bucketName) {
        Folder createdFolder = Folder.builder()
                .name("root-folder")
                .path("/")
                .uploadedAt(LocalDateTime.now())
                .parent(null)
                .bucketId(bucketService.getBucketByName(bucketName).get()) //may be null
                .build();

        if (s3Service.createRootFolder(bucketName)) {
            folderRepository.saveAndFlush(createdFolder);
            return createdFolder;
        }
        return null;
    }

    public Folder getFolderByPath(String path) {
        return folderRepository.getFolderByPath(path);
    }

    public List<Folder> getFoldersInFolder(Folder folder) {
        return folderRepository.getAllFoldersByParentId(folder);
    }

    public Folder getFolderById(Long id) {
        return folderRepository.getFolderById(id);
    }


}
