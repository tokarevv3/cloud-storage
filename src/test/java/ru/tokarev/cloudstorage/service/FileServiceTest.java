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
import ru.tokarev.cloudstorage.database.repositorty.FileRepository;
import ru.tokarev.cloudstorage.database.repositorty.FolderRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(classes = TestCloudStorateApplication.class)
public class FileServiceTest {

    private final FileService fileService;

    private final FileRepository fileRepository;

    private final FolderRepository folderRepository;

    private final BucketService bucketService;

    private final MinioClient minioClient;

    private final String defaultBucketName = "base-bucket";


    @Test
    void checkBucketInfo() {
        var bucket = bucketService.getBucketByName(defaultBucketName);
        System.out.println(bucket);
        assertTrue(true);
    }

    @Test
    void checkBucketList() {
        var buckets = bucketService.getBucketsList();
        for (var bucket : buckets) {
            System.out.println(bucket.getName());
        }
        assertTrue(true);
    }

    @Test
    @SneakyThrows
    void checkin() {
        String defaultFolderName = "mods/";

        //fileService.createFolder(defaultBucketName, "", "test");

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(defaultBucketName)
                //.prefix(defaultFolderName)
                .build());

        for (var result : results) {
            System.out.println(result.get().objectName());
        }
    }

//    @Test
//    @SneakyThrows
//    void createFolderInBucketRoot() {
//
//        boolean isFolderCreated = fileService.createFolder(defaultBucketName, "", "test");
//
//        ArrayList<Result<Item>> folderList = fileService.get(defaultBucketName);
//
//        for (var result : folderList) {
//            System.out.println(result.get().objectName());
//        }
//        assertEquals(3, folderList.size());
//        assertTrue(isFolderCreated);
//
//    }
//
//    @Test
//    @SneakyThrows
//    void getFolderList() {
//        ArrayList<Result<Item>> folderList = fileService.getFolderList(defaultBucketName, "test");
//        for (var result : folderList) {
//            System.out.println(result.get().objectName());
//        }
//    }


    @Test
    @SneakyThrows
    void gestGetFilesList() {
        Iterable<Result<Item>> filesList = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(defaultBucketName)
                      //  .recursive(true)
                        .prefix("test/кормёжка/")
                .build());

        for (var result : filesList) {
            System.out.println(result.get().objectName());
        }
    }
}
