package ru.tokarev.cloudstorage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.LifecycleConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BucketService {

    private final MinioClient minioClient;

    private final String defaultBucketName = "user-bucket-";

    public Long createBucket(Long id) {
        String bucketName = defaultBucketName + id;

        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isBucketExists(Long id) {
        String bucketName = defaultBucketName + id;

        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //TODO: create method
    public Long getBucketSize(Long id) {
        String bucketName = defaultBucketName + id;
        Long totalSize = 0L;

        var objects = minioClient.listObjects(ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build());
        for (var object : objects) {
            try {
                totalSize += object.get().size();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return totalSize;
    }

    public List<Bucket> getBucketsList() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //TODO:: create normal getBucket method
    public LifecycleConfiguration getBucket(String bucketName) {
        try {
            return minioClient.getBucketLifecycle(GetBucketLifecycleArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
