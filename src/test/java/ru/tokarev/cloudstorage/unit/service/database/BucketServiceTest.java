package ru.tokarev.cloudstorage.unit.service.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.database.repositorty.BucketRepository;
import ru.tokarev.cloudstorage.dto.BucketReadDto;
import ru.tokarev.cloudstorage.exception.BucketSizeExceededException;
import ru.tokarev.cloudstorage.mapper.BucketCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.BucketReadMapper;
import ru.tokarev.cloudstorage.service.BucketSizeService;
import ru.tokarev.cloudstorage.service.S3Service;
import ru.tokarev.cloudstorage.service.database.BucketService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class BucketServiceTest {

    @Mock
    private BucketRepository bucketRepository;
    @Mock
    private BucketCreateEditMapper bucketCreateEditMapper;
    @Mock
    private BucketReadMapper bucketReadMapper;
    @Mock
    private S3Service s3Service;
    @Mock
    private BucketSizeService bucketSizeService;

    @InjectMocks
    private BucketService bucketService;

    @Test
    void createBucketSuccess() {

        User user = new User();
        user.setId(1L);

        Bucket bucket = new Bucket();
        bucket.setName("user-bucket-1");

        when(bucketCreateEditMapper.map(any())).thenReturn(bucket);
        when(bucketRepository.saveAndFlush(bucket)).thenReturn(bucket);
        when(s3Service.createBucket(any())).thenReturn(true);

        Optional<Bucket> createdBucket = bucketService.createBucket(user);


        Assertions.assertTrue(createdBucket.isPresent());
        Assertions.assertEquals(bucket.getName(), createdBucket.get().getName());
    }

    @Test
    void getBucketByIdSuccess() {
        Long bucketId = 1L;
        Bucket bucket = new Bucket();
        bucket.setId(bucketId);

        when(bucketRepository.findById(bucketId)).thenReturn(Optional.of(bucket));

        Optional<Bucket> bucketById = bucketService.getBucketById(bucketId);

        Assertions.assertTrue(bucketById.isPresent());
        Assertions.assertEquals(bucket.getId(), bucketById.get().getId());
    }

    @Test
    void saveBucketSuccess() {
        Bucket bucket = new Bucket();
        bucket.setId(1L);

        when(bucketRepository.saveAndFlush(bucket)).thenReturn(bucket);

        Optional<Bucket> savedBucket = bucketService.saveBucket(bucket);

        Assertions.assertTrue(savedBucket.isPresent());
        Assertions.assertEquals(bucket.getId(), savedBucket.get().getId());
    }

    @Test
    void existsByFolderReturnsTrue() {
        Folder folder = new Folder();

        when(bucketRepository.existsByRootFolder(folder)).thenReturn(true);

        boolean exists = bucketService.existsByFolder(folder);

        Assertions.assertTrue(exists);
    }

    @Test
    void updateBucketSizeAllowed() throws BucketSizeExceededException {
        Bucket bucket = new Bucket();
        bucket.setSize(100L);

        long fileSize = 50L;

        when(bucketSizeService.isUpdateAllowed(bucket, fileSize)).thenReturn(true);
        when(bucketRepository.saveAndFlush(bucket)).thenReturn(bucket);

        bucketService.updateBucketSize(bucket, fileSize);

        Assertions.assertEquals(150L, bucket.getSize());
        verify(bucketRepository).saveAndFlush(bucket);
    }

    @Test
    void updateBucketSizeNotAllowedThrowsException() throws BucketSizeExceededException {
        Bucket bucket = new Bucket();
        bucket.setSize(100L);

        long fileSize = 1000L;

        when(bucketSizeService.isUpdateAllowed(bucket, fileSize)).thenThrow(BucketSizeExceededException.class);

        Assertions.assertThrows(BucketSizeExceededException.class,
                () -> bucketService.updateBucketSize(bucket, fileSize));
        verify(bucketRepository, never()).saveAndFlush(any());
    }

    @Test
    void findAllReturnsMappedDto() {
        Bucket bucket1 = new Bucket();
        bucket1.setId(1L);

        Bucket bucket2 = new Bucket();
        bucket2.setId(2L);

        BucketReadDto dto1 = new BucketReadDto(1L, "dummy-bucket-1", 1L, 1L);
        BucketReadDto dto2 = new BucketReadDto(2L, "dummy-bucket-2", 1L, 1L);

        when(bucketRepository.findAll()).thenReturn(List.of(bucket1, bucket2));
        when(bucketReadMapper.map(bucket1)).thenReturn(dto1);
        when(bucketReadMapper.map(bucket2)).thenReturn(dto2);


        List<BucketReadDto> result = bucketService.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(dto1, result.get(0));
        Assertions.assertEquals(dto2, result.get(1));

    }

}
