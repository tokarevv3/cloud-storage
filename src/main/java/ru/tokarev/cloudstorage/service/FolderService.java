package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.repositorty.FolderRepository;
import ru.tokarev.cloudstorage.dto.FolderCreateEditDto;
import ru.tokarev.cloudstorage.dto.FolderReadDto;
import ru.tokarev.cloudstorage.mapper.FolderCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.FolderReadMapper;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
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
        String path = parentFolder.getPath() + parentFolder.getName() + "/";

        FolderCreateEditDto folderCreateEditDto = new FolderCreateEditDto(
                folderName,
                path,
                LocalDateTime.now(),
                parentFolder,
                parentFolder.getBucketId());

        log.info("Trying to create folder with part : " + folderCreateEditDto.getPath());

        if (s3Service.createFolder(
                parentFolder.getBucketId().getName(),
                folderCreateEditDto.getName(),
                folderCreateEditDto.getPath())) {
            return Optional.of(folderCreateEditDto)
                    .map(folderCreateEditMapper::map)
                    .map(folderRepository::saveAndFlush)
                    .map(folderReadMapper::map);

        }


        return Optional.empty();
    }

    public Optional<Folder> createRootFolder(String bucketName) {

        Bucket bucket = bucketService.getBucketByName(bucketName).get();

        Optional<Folder> createdFolder = Optional.of(Folder.builder()
                .name("root-folder")
                .path("/")
                .uploadedAt(LocalDateTime.now())
                .parent(null)
                .bucketId(bucket) //may be null
                .build());

        bucket.setRootFolder(createdFolder.orElse(null));

        bucketService.saveBucket(bucket);

        if (s3Service.createRootFolder(bucketName)) {
            return createdFolder
                    .map(folderRepository::saveAndFlush);
        }
        return Optional.empty();
    }

    public Folder getFolderByPath(String path) {
        return folderRepository.getFolderByPath(path);
    }

    public Folder getFolderByPathAndBucket(String path, Bucket bucket) {
        return folderRepository.getFolderByPathAndBucketId(path, bucket);
    }

    public Folder getFolderByNameAndBucket(String folderName, Bucket bucket) {
        return folderRepository.getFolderByNameAndBucketId(folderName, bucket);
    }

    public Folder getFolderByNameAndPathAndBucket(String folderName, String path, Bucket bucket) {
        return folderRepository.getFolderByNameAndPathAndBucketId(folderName, path, bucket);
    }

    public List<Folder> getFoldersInFolder(Folder folder) {
        return folderRepository.getAllFoldersByParentId(folder);
    }

    public Folder getFolderById(Long id) {
        return folderRepository.getFolderById(id);
    }


}
