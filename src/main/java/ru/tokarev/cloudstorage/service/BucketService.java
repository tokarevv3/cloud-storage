package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.database.repositorty.BucketRepository;
import ru.tokarev.cloudstorage.dto.BucketCreateEditDto;
import ru.tokarev.cloudstorage.mapper.BucketCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.BucketReadMapper;

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
    private final String defaultBucketName = "user-bucket-";

    // What it really should to return? boolean or DTO?
    public Optional<Bucket> createBucket( User user) {
        String bucketName = defaultBucketName + user.getId();

        BucketCreateEditDto bucket = new BucketCreateEditDto(
                bucketName,
                0L,
                user,
                null);



        Optional<Bucket> bucketReadDto = Optional.of(bucket).map(bucketCreateEditMapper::map);

        user.setBucket(bucketReadDto.get());

        bucketReadDto.map(bucketRepository::saveAndFlush).orElseThrow();

        if (s3Service.createBucket(bucketName)) {
            return bucketReadDto;
        }
        return Optional.empty();
    }

    //TODO: maybe shall change to database?
    public Long getBucketSize(Long id) {
        return bucketRepository.getSize(id);
    }

    // TODO: create repository method
    public List<Bucket> getBucketsList() {
        return bucketRepository.findAll()
//                .stream()
//                .map(bucketReadMapper::map)
//                .collect(toList())
                ;
    }

    //TODO:: create normal getBucket method
    public Optional<Bucket> getBucketByName(String bucketName) {
        return bucketRepository.findByName(bucketName);
    }

    public Optional<Bucket> getBucketById(Long id) {
        return bucketRepository.findById(id);
    }

    public Optional<Bucket> saveBucket(Bucket bucket) {
        return Optional.of(bucketRepository.saveAndFlush(bucket));
    }

}
