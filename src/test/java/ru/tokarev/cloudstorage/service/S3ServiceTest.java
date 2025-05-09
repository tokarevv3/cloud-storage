package ru.tokarev.cloudstorage.service;

import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import ru.tokarev.cloudstorage.CloudStorageApplication;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CloudStorageApplication.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class S3ServiceTest {

    private final S3Service s3Service;

    @Test
    void createBucket() {
        String bucketName = "test-bucket-1";
        s3Service.createBucket(bucketName);

        List<Bucket> bucketsList = s3Service.getBucketsList();

        bucketsList.forEach(bucket -> {
            if (bucket.name().equals(bucketName)) {
                assertTrue(true);
            }
        });
    }
}
