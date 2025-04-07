package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import ru.tokarev.cloudstorage.CloudStorageApplication;
import ru.tokarev.cloudstorage.database.entity.Bucket;

import java.util.Optional;

@SpringBootTest(classes = CloudStorageApplication.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class FolderServiceTest {

    private final FolderService folderService;

    private final BucketService bucketService;

    @Test
    void createRootFolder() {

        String bucketName = bucketService.getBucketById(1L).get().getName();

        folderService.createRootFolder(bucketName);



    }

    @Test
    void createFolder() {
        folderService.createFolder("test-folder-again", 2L);
    }
}
