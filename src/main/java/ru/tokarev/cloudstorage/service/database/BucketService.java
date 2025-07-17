package ru.tokarev.cloudstorage.service.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.database.repositorty.BucketRepository;
import ru.tokarev.cloudstorage.dto.BucketCreateEditDto;
import ru.tokarev.cloudstorage.dto.BucketReadDto;
import ru.tokarev.cloudstorage.exception.BucketSizeExceededException;
import ru.tokarev.cloudstorage.mapper.BucketCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.BucketReadMapper;
import ru.tokarev.cloudstorage.service.BucketSizeService;
import ru.tokarev.cloudstorage.service.S3Service;

import java.util.List;
import java.util.Optional;

/**
 * Service return Bucket class instead of BucketReadDto because information is for admin only.
 * User see only it`s size.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class BucketService {

    private final S3Service s3Service;
    private final BucketRepository bucketRepository;
    private final BucketCreateEditMapper bucketCreateEditMapper;
    private final BucketReadMapper bucketReadMapper;
    private final BucketSizeService bucketSizeService;


    public Optional<Bucket> createBucket(User user) {
        String defaultBucketName = "user-bucket-";
        String bucketName = defaultBucketName + user.getId();

        BucketCreateEditDto bucketDto = new BucketCreateEditDto(
                bucketName,
                0L,
                user,
                null);

        Bucket bucket = bucketCreateEditMapper.map(bucketDto);

        user.setBucket(bucket);

        Bucket savedBucket = bucketRepository.saveAndFlush(bucket);

        if (s3Service.createBucket(bucketName)) {
            return Optional.of(savedBucket);
        }
        return Optional.empty();
    }

    public Optional<Bucket> getBucketById(Long id) {
        return bucketRepository.findById(id);
    }

    public Optional<Bucket> saveBucket(Bucket bucket) {
        return Optional.of(bucketRepository.saveAndFlush(bucket));
    }

    public boolean existsByFolder(Folder folder) {
        return bucketRepository.existsByRootFolder(folder);
    }

    public void updateBucketSize(Bucket bucket, long fileSize) throws BucketSizeExceededException {
        if (bucketSizeService.isUpdateAllowed(bucket, fileSize)) {
            bucket.setSize(bucket.getSize() + fileSize);
            bucketRepository.saveAndFlush(bucket);
        }
    }

    public List<BucketReadDto> findAll() {
        return bucketRepository.findAll().stream()
                .map(bucketReadMapper::map)
                .toList();
    }


}
