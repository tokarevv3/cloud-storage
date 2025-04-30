package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import ru.tokarev.cloudstorage.CloudStorageApplication;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.BucketCreateEditDto;

@SpringBootTest(classes = CloudStorageApplication.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class BucketServiceTest {
    private final BucketService bucketService;
    private final UserService userService;


    @Test
    void createBucket() {

        User user = userService.findById(1L).get();
        BucketCreateEditDto createdBucket = new BucketCreateEditDto(
                "test",
                0L,
                user,
                null
        );

        bucketService.createBucket(user);
    }
}
