package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest
public class FileServiceTest {

    private final FileService fileService;

    @Test
    void buckerExist() {

        String defaultBucketName = "base-bucket";

        boolean bucketExists = fileService.isBucketExists(defaultBucketName);
        assertTrue(bucketExists);
    }
}
