package ru.tokarev.cloudstorage.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.exception.BucketSizeExceededException;
import ru.tokarev.cloudstorage.service.BucketSizeService;

import static org.junit.jupiter.api.Assertions.*;

class BucketSizeServiceTest {

    private BucketSizeService bucketSizeService;

    @BeforeEach
    void setUp() {
        bucketSizeService = new BucketSizeService();
        // Устанавливаем значение adminMaxBucketSize вручную (иначе @Value не сработает)
        bucketSizeService.setCapacity(2); // 2 GB
        bucketSizeService.toggleCapacity(true);
        bucketSizeService.init(); // вручную вызываем @PostConstruct
    }

    @Test
    void testInit_setsCorrectMaxBucketSize() {
        assertEquals(2L * 8 * 1024 * 1024 * 1024, bucketSizeService.getCapacity());
    }

    @Test
    void testSetCapacity_validValue() {
        assertTrue(bucketSizeService.setCapacity(10));
        bucketSizeService.init();
        assertEquals(10L * 8 * 1024 * 1024 * 1024, bucketSizeService.getCapacity());
    }

    @Test
    void testSetCapacity_invalidValue_zero() {
        assertFalse(bucketSizeService.setCapacity(0));
    }

    @Test
    void testSetCapacity_invalidValue_overLimit() {
        assertFalse(bucketSizeService.setCapacity(51));
    }

    @Test
    void testToggleCapacity_falsePreventsUpdate() {
        bucketSizeService.toggleCapacity(false);
        Bucket bucket = new Bucket();
        bucket.setSize(0L);

        assertDoesNotThrow(() -> {
            boolean allowed = bucketSizeService.isUpdateAllowed(bucket, 1);
            assertFalse(allowed);
        });
    }

    @Test
    void testIsUpdateAllowed_withinLimit_returnsTrue() throws BucketSizeExceededException {
        Bucket bucket = new Bucket();
        bucket.setSize(3L * 1024 * 1024 * 1024); // 1 GB
        long fileSize = 2L * 1024 * 1024 * 1024; // 0.5 GB

        assertTrue(bucketSizeService.isUpdateAllowed(bucket, fileSize));
    }

    @Test
    void testIsUpdateAllowed_exceedsLimit_throwsException() {
        Bucket bucket = new Bucket();
        bucket.setSize(2L * 8 * 1024 * 1024 * 1024); // уже 100% лимит

        long fileSize = 1L; // 1 байт

        assertThrows(BucketSizeExceededException.class, () -> {
            bucketSizeService.isUpdateAllowed(bucket, fileSize);
        });
    }
}
