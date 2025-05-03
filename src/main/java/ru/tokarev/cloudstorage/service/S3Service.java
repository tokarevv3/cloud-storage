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
    private final String defaultBucketName = "user-bucket-";

    public boolean createBucket(Long id) {

        String bucketName = defaultBucketName + id;

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

    public boolean uploadFile(String bucketName, String filePathName, InputStream inputStream, long size) {

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

    public boolean uploadFile(String bucketName, String fileName, String path, InputStream inputStream, long size) {

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path + "/" + fileName)
                    .stream(inputStream, size, -1)
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    //TODO: remake
    public List<String> uploadFiles(List<MultipartFile> files, String folderPath) throws Exception {

        List<String> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String objectName = folderPath + file.getOriginalFilename();

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket("base-bucket")
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
                uploadedFiles.add(objectName);
            } catch (MinioException e) {
                throw new Exception("Failed to upload file to MinIO: " + e.getMessage());
            }
        }

        return uploadedFiles;
    }

    public boolean createFolder(String bucketName, String folderName, String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object( path + "/" + folderName+ "/confirmation")
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

    public MultipartFile downloadFile(String bucketName, String fileName) {
        try {
            log.info("Trying to download file: " + fileName);
            return (MultipartFile) minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteFolder(Folder deleteFolder) {
        String deleteFolderBucketName = deleteFolder.getBucketId().getName();
        String deleteFolderFullPath = deleteFolder.getPath() + deleteFolder.getName() + "/";
        log.info("Trying to delete folder: " + deleteFolderFullPath);

        try {
            // Удаляем все объекты в папке и её подкаталогах рекурсивно
            deleteObjectInFolder(deleteFolderBucketName, deleteFolderFullPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteObjectInFolder(String bucketName, String prefix) {

        if (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        log.info("Trying to delete folder: " + prefix + " in bucket: " + bucketName);

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
