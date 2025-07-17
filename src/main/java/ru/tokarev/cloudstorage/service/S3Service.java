package ru.tokarev.cloudstorage.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.database.entity.Folder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final MinioClient minioClient;

    public boolean createBucket(String bucketName) {

        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Bucket> getBucketsList() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean uploadFile(Long userId, String filePathName, InputStream inputStream, long size) {

        String defaultBucketName = "user-bucket-";
        String bucketName = defaultBucketName + userId;

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePathName)
                    .stream(inputStream, size, -1)
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createFolder(String bucketName, String folderName, String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path + "/" + folderName + "/confirmation")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, 0)
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createRootFolder(String bucketName) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object("root-folder/confirmation")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, 0)
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // TODO:: remake
    public byte[] downloadFile(String bucketName, String fileName) {
        try {
            log.info("Trying to download file: " + fileName);
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build()).readAllBytes();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteFolder(Folder deleteFolder) {
        String deleteFolderBucketName = deleteFolder.getBucket().getName();
        String deleteFolderFullPath = deleteFolder.getPath() + deleteFolder.getName() + "/";
        log.info("Trying to delete folder: " + deleteFolderFullPath);

        try {
            deleteObjectInFolder(deleteFolderBucketName, deleteFolderFullPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteObjectInFolder(String bucketName, String prefix) throws Exception {
        if (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        log.info("Trying to delete folder: " + prefix + " in bucket: " + bucketName);

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .recursive(true)
                .bucket(bucketName)
                .prefix(prefix)
                .build());

        for (Result<Item> result : results) {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .object(result.get().objectName())
                    .bucket(bucketName)
                    .build());
        }
        log.info("Deleted list of object");
    }


    public boolean deleteFile(String bucketName, String fileFullPath) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileFullPath)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateFile(String bucketName, String oldObjectPath, String newObjectPath) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(newObjectPath)
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(oldObjectPath)
                            .build())
                    .build());

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(oldObjectPath)
                    .build());

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFolderPath(String bucketName, String oldPath, String newPath) {

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(oldPath)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                String oldObjectName = item.objectName();
                String newObjectName = oldObjectName.replace(oldPath, newPath);
                log.info("Trying to put: " + newObjectName);

                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucketName)
                                .object(newObjectName)
                                .source(CopySource.builder()
                                        .bucket(bucketName)
                                        .object(oldObjectName)
                                        .build())
                                .build()
                );

                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(oldObjectName)
                                .build()
                );
            }

            return true;

        } catch (Exception e) {
            throw new RuntimeException("Failed to update folder path: " + e.getMessage(), e);
        }
    }
}