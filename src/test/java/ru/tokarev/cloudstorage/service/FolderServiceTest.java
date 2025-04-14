package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import ru.tokarev.cloudstorage.CloudStorageApplication;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.dto.FolderReadDto;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CloudStorageApplication.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class FolderServiceTest {

    private final FolderService folderService;

    private final BucketService bucketService;

    @Test
    void createRootFolder() {

        String bucketName = bucketService.getBucketById(1L).get().getName();

        folderService.createRootFolder(bucketName);



    }

    @Test
    void createFolder() {
        Optional<FolderReadDto> folder = folderService.createFolder("tryfolder", 2L);

        Folder folderById = folderService.getFolderById(folder.get().getId());

        assertEquals(folder.get().getId(), folderById.getId());
    }

    @Test
    void getListOfFilesAndFoldersInFolder() {
        Folder folderById = folderService.getFolderById(2L);

        Map<Long, String> listInCurrentFolder = folderService.getListInCurrentFolder(folderById);

        assertEquals(2, listInCurrentFolder.size());

    }
}
