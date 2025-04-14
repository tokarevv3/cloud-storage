package ru.tokarev.cloudstorage.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.messages.LifecycleConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.repositorty.FolderRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;
    private final FolderRepository folderRepository;

    public Iterable<Result<Item>> getFilesList(String bucketName, String folderName) {

        folderName = isSlashEnded(folderName);
        try {
            return minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(folderName)
                    .build());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

//    public ArrayList<Result<Item>> getFolderList(String bucketName) {
//
//        ArrayList<Result<Item>> folderList = new ArrayList<>();
//
//        try {
//            Iterable<Result<Item>> objectsList = minioClient.listObjects(ListObjectsArgs.builder()
//                    .bucket(bucketName)
//                    .build());
//            for (var object : objectsList) {
//                if (object.get().isDir()) {
//                    folderList.add(object);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return folderList;
//    }
//
//    public ArrayList<Result<Item>> getFolderList(String bucketName, String folderName) {
//
//        folderName = isSlashEnded(folderName);
//
//        ArrayList<Result<Item>> folderList = new ArrayList<>();
//
//        try {
//            Iterable<Result<Item>> objectsList = minioClient.listObjects(ListObjectsArgs.builder()
//                    .bucket(bucketName)
//                    .prefix(folderName)
//                    .build());
//            for (var object : objectsList) {
//                if (object.get().isDir()) {
//                    folderList.add(object);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return folderList;
//    }

    public boolean createFolder(String bucketName, String parentFolderName, String folderName) {

        folderName = isSlashEnded(folderName);
        String path = parentFolderName + "/" + folderName;

        Folder createdFolder = Folder.builder()
                .name(folderName)
                .path(path)
                .parent(folderRepository.getFolderByPath(path))
                .build();

        try {
            minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(parentFolderName + folderName)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, 0)
                    .build());
            folderRepository.saveAndFlush(createdFolder);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //TODO: pure SQL method, disctruct to s3service upload method
    public boolean uploadFile(String bucketName, String fileName, InputStream inputStream, long size) {

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, size, -1)
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

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

    public static String isSlashEnded(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path = path + '/';
        }
        return path;
    }
}
