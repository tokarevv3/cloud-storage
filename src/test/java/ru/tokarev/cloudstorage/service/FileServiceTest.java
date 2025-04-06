package ru.tokarev.cloudstorage.service;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import ru.tokarev.cloudstorage.TestCloudStorateApplication;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(classes = TestCloudStorateApplication.class)
public class FileServiceTest {

    private final FileService fileService;

    private final MinioClient minioClient;

    private final String defaultBucketName = "base-bucket";

    @Test
    void buckerExist() {
        boolean bucketExists = fileService.isBucketExists(defaultBucketName);
        assertTrue(bucketExists);
    }

    @Test
    void checkBucketInfo() {
        var bucket = fileService.getBucket(defaultBucketName);
        System.out.println(bucket);
        assertTrue(true);
    }

    @Test
    void checkBucketList() {
        var buckets = fileService.getBucketsList();
        for (var bucket : buckets) {
            System.out.println(bucket.name());
        }
        assertTrue(true);
    }

    @Test
    @SneakyThrows
    void checkin() {
        String defaultFolderName = "mods/";

        fileService.createFolder(defaultBucketName, "", "test");

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(defaultBucketName)
                //.prefix(defaultFolderName)
                .build());

        for (var result : results) {
            System.out.println(result.get().objectName());
        }
    }

    @Test
    @SneakyThrows
    void createFolderInBucketRoot() {

        boolean isFolderCreated = fileService.createFolder(defaultBucketName, "", "test");

        ArrayList<Result<Item>> folderList = fileService.getFolderList(defaultBucketName);

        for (var result : folderList) {
            System.out.println(result.get().objectName());
        }
        assertEquals(3, folderList.size());
        assertTrue(isFolderCreated);

    }
}
