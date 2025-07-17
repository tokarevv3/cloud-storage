package ru.tokarev.cloudstorage.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.exception.BucketSizeExceededException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BucketSizeService {

    @Value("${minio.user.capacity}")
    private Integer adminMaxBucketSize;
    private Long maxBucketSize;
    private boolean toggleCapacity = true;

    @PostConstruct
    public void init() {
        this.maxBucketSize = adminMaxBucketSize * 8L * 1024 * 1024 * 1024;
    }

    public boolean setCapacity(Integer capacity) {
        if ((capacity > 0) && (capacity <=50)) {
            adminMaxBucketSize = capacity;
            return true;
        }
        return false;
    }

    public void toggleCapacity(Boolean toggle) {
        toggleCapacity = toggle;
    }

    public Long getCapacity() {
        return maxBucketSize;
    }

    public boolean isUpdateAllowed(Bucket bucket, long fileSize) throws BucketSizeExceededException {
        if (!toggleCapacity) return false;

        if (bucket.getSize() + fileSize > maxBucketSize) {
            throw new BucketSizeExceededException("Bucket size exceeded over limit.");
        } else {
            return true;
        }
    }
}
