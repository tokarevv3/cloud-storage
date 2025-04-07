package ru.tokarev.cloudstorage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.LifecycleConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.database.repositorty.BucketRepository;
import ru.tokarev.cloudstorage.dto.BucketCreateEditDto;
import ru.tokarev.cloudstorage.dto.BucketReadDto;
import ru.tokarev.cloudstorage.mapper.BucketCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.BucketReadMapper;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BucketService {

    private final MinioClient minioClient;
    private final BucketRepository bucketRepository;
    private final BucketCreateEditMapper bucketCreateEditMapper;
    private final BucketReadMapper bucketReadMapper;
    private final String defaultBucketName = "user-bucket-";


    public BucketReadDto createBucket(Long id, User user) {
        String bucketName = defaultBucketName + id;

        BucketCreateEditDto bucket = new BucketCreateEditDto(
                bucketName,
                0L,
                user,
                null);

        BucketReadDto bucketReadDto = Optional.of(bucket)
                .map(bucketCreateEditMapper::map)
                .map(bucketRepository::saveAndFlush)
                .map(bucketReadMapper::map)
                .orElseThrow();

        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            return bucketReadDto;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isBucketExists(Long id) {
        String bucketName = defaultBucketName + id;
        // TODO:: create repository method for find by name

        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ru.tokarev.cloudstorage.database.entity.Bucket getBucket(Long id) {
        return bucketRepository.findById(id).orElseThrow();
    }

    //TODO: maybe shall change to database?
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

    // TODO: create repository method
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

    public Optional<ru.tokarev.cloudstorage.database.entity.Bucket> getBucketById(Long id) {
        return bucketRepository.findById(id);
    }

    public ru.tokarev.cloudstorage.database.entity.Bucket getBucketByName(String bucketName) {
        return bucketRepository.findByName(bucketName);
    }
}
