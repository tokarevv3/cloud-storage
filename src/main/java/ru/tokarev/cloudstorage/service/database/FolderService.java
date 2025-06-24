package ru.tokarev.cloudstorage.service.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.repositorty.FolderRepository;
import ru.tokarev.cloudstorage.dto.FolderCreateEditDto;
import ru.tokarev.cloudstorage.dto.FolderReadDto;
import ru.tokarev.cloudstorage.dto.FolderTreeNode;
import ru.tokarev.cloudstorage.mapper.FolderCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.FolderReadMapper;
import ru.tokarev.cloudstorage.service.S3Service;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final BucketService bucketService;
    private final FolderCreateEditMapper folderCreateEditMapper;
    private final FolderReadMapper folderReadMapper;
    private final S3Service s3Service;
    private final FileService fileService;

    @Transactional
    public Optional<FolderReadDto> createFolder(String folderName, Long folderId) throws FileNotFoundException {
        Folder parentFolder = folderRepository.getFolderById(folderId).orElseThrow(FileNotFoundException::new);
        String path = parentFolder.getPath() + parentFolder.getName() + "/";

        FolderCreateEditDto folderCreateEditDto = new FolderCreateEditDto(
                folderName,
                path,
                LocalDateTime.now(),
                parentFolder,
                parentFolder.getBucket());

        log.info("Trying to create folder with part : {}", folderCreateEditDto.getPath());

        if (s3Service.createFolder(
                parentFolder.getBucket().getName(),
                folderCreateEditDto.getName(),
                folderCreateEditDto.getPath())) {
            return Optional.of(folderCreateEditDto)
                    .map(folderCreateEditMapper::map)
                    .map(folderRepository::saveAndFlush)
                    .map(folderReadMapper::map);

        }
        return Optional.empty();
    }

    @Transactional
    public Optional<Folder> createRootFolder(Bucket bucket) {

        Optional<Folder> createdFolder = Optional.of(Folder.builder()
                .name("root-folder")
                .path("/")
                .uploadedAt(LocalDateTime.now())
                .parent(null)
                .bucket(bucket) //may be null
                .build());

        bucket.setRootFolder(createdFolder.orElse(null));

        folderRepository.saveAndFlush(createdFolder.orElse(null));

        bucketService.saveBucket(bucket);

        if (s3Service.createRootFolder(bucket.getName())) {
            return createdFolder;
        }
        return Optional.empty();
    }

    public Folder getFolderByNameAndBucket(String folderName, Bucket bucket) {
        return folderRepository.getFolderByNameAndBucketId(folderName, bucket.getId());
    }

    public Folder getFolderByNameAndPathAndBucket(String folderName, String path, Bucket bucket) {
        return folderRepository.getFolderByNameAndPathAndBucketId(folderName, path, bucket.getId());
    }

    public List<Folder> getFoldersInFolder(Folder folder) {
        return folderRepository.getAllFoldersByParentId(folder);
    }

    public Optional<Folder> getFolderById(Long id) {
        return folderRepository.getFolderById(id);
    }

    @Transactional
    public boolean deleteFolderById(Long id) {
        Optional<Folder> deleteFolder = folderRepository.findById(id);
        deleteFolder.ifPresent(
                this::deleteFolderRecursive);
        return s3Service.deleteFolder(deleteFolder.get());
    }

    private void deleteFolderRecursive(@NotNull Folder folder) {

        if (bucketService.existsByFolder(folder)) {
            log.warn("Skipping deletion of folder {} — linked to a bucket", folder.getId());
            return;
        }
        fileService.getFilesInFolder(folder)
                .forEach(file -> fileService.deleteFile(file.getId()));

        folderRepository.getAllFoldersByParentId(folder)
                .forEach(this::deleteFolderRecursive);

        log.warn("Trying to delete folder {}", folder.getId());
        folderRepository.deleteById(folder.getId());
        folderRepository.flush();
    }

    public List<FolderTreeNode> getUserFolderTree(Bucket userBucket) {

        List<Folder> userFolders = folderRepository.findByBucketId(userBucket.getId());

        Map<Long, List<Folder>> byParentId = userFolders.stream()
                .collect(Collectors.groupingBy(f -> f.getParent() == null ? 0L : f.getParent().getId()));

        return buildTree(0L, byParentId);
    }

    public List<FolderTreeNode> buildTree(Long parentId, Map<Long, List<Folder>> byParentId) {
        List<FolderTreeNode> result = new ArrayList<>();
        List<Folder> folders = byParentId.getOrDefault(parentId, Collections.emptyList());

        for (Folder folder : folders) {
            FolderTreeNode node = new FolderTreeNode(folder.getId(), folder.getName());
            node.setChildren(buildTree(folder.getId(), byParentId));
            result.add(node);
        }
        return result;
    }

    public Folder save(Folder folder) {
        return folderRepository.saveAndFlush(folder);
    }

    public void updateFolderRecursive(Folder folder) {
        String currentNewPath = folder.getPath() + folder.getName() + "/";
        List<File> filesInFolder = fileService.getFilesInFolder(folder);
        for (File file : filesInFolder) {
            file.setFilePath(currentNewPath);
            fileService.saveFile(file);
        }

        List<Folder> foldersInFolder = getFoldersInFolder(folder);

        for (Folder folderInFolder : foldersInFolder) {
            folderInFolder.setPath(currentNewPath);
            updateFolderRecursive(folderInFolder);
        }
    }
}
