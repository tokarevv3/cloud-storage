package ru.tokarev.cloudstorage.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.repositorty.FolderRepository;
import ru.tokarev.cloudstorage.dto.FolderCreateEditDto;
import ru.tokarev.cloudstorage.mapper.FolderCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.FolderReadMapper;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final BucketService bucketService;
    private final FolderCreateEditMapper folderCreateEditMapper;
    private final FolderReadMapper folderReadMapper;
    private final MinioClient minioClient;

    public void createFolder(String folderName, Long folderId) {
        Folder parentFolder = folderRepository.getFolderById(folderId);
        String path = parentFolder.getPath() + "/" + parentFolder.getName();

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
            Optional.of(folderCreateEditDto)
                    .map(folderCreateEditMapper::map)
                    .map(folderRepository::saveAndFlush)
                    .map(folderReadMapper::map);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public Folder createRootFolder(String bucketName) {
        Folder createdFolder = Folder.builder()
                .name("root-folder")
                .path("/")
                .uploadedAt(LocalDateTime.now())
                .parentId(null)
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
}
