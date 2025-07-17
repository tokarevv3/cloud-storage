package ru.tokarev.cloudstorage.unit.service.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tokarev.cloudstorage.database.entity.*;
import ru.tokarev.cloudstorage.database.repositorty.FileRepository;
import ru.tokarev.cloudstorage.exception.BucketSizeExceededException;
import ru.tokarev.cloudstorage.service.database.BucketService;
import ru.tokarev.cloudstorage.service.database.FileService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private BucketService bucketService;

    @InjectMocks
    private FileService fileService;

    @Test
    void getFilesInFolderCallsRepositoryAndReturnsFiles() {
        Folder folder = new Folder();
        File file1 = new File();
        File file2 = new File();
        List<File> expectedFiles = List.of(file1, file2);

        when(fileRepository.findAllByParentFolder(folder)).thenReturn(expectedFiles);

        List<File> actualFiles = fileService.getFilesInFolder(folder);

        assertEquals(expectedFiles, actualFiles);
        verify(fileRepository).findAllByParentFolder(folder);
    }

    @Test
    void getFileReturnsFileOptional() {
        Long fileId = 1L;
        File file = new File();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        Optional<File> result = fileService.getFile(fileId);

        assertTrue(result.isPresent());
        assertEquals(file, result.get());
        verify(fileRepository).findById(fileId);
    }

    @Test
    void uploadFileSuccessfulUpload_ReturnsTrue() throws BucketSizeExceededException {
        Folder folder = new Folder();
        Bucket bucket = new Bucket();
        folder.setBucket(bucket);

        String fileName = "test.txt";
        String filePath = "/files/test.txt";
        long size = 100L;
        String contentType = "text/plain";

        // bucketService.updateBucketSize не бросает исключение
        doNothing().when(bucketService).updateBucketSize(bucket, size);
        when(fileRepository.saveAndFlush(any(File.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = fileService.uploadFile(fileName, filePath, size, contentType, folder);

        assertTrue(result);

        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        verify(bucketService).updateBucketSize(bucket, size);
        verify(fileRepository).saveAndFlush(fileCaptor.capture());

        File savedFile = fileCaptor.getValue();
        assertEquals(fileName, savedFile.getFileName());
        assertEquals(filePath, savedFile.getFilePath());
        assertEquals(size, savedFile.getFileSize());
        assertEquals(contentType, savedFile.getContentType());
        assertEquals(folder, savedFile.getFolder());
        assertNotNull(savedFile.getUploadedAt());
    }

    @Test
    void uploadFileBucketSizeExceededReturnsFalse() throws BucketSizeExceededException {
        Folder folder = new Folder();
        Bucket bucket = new Bucket();
        folder.setBucket(bucket);

        doThrow(new BucketSizeExceededException("Exceeded")).when(bucketService).updateBucketSize(bucket, 100L);

        boolean result = fileService.uploadFile("file", "/path", 100L, "type", folder);

        assertFalse(result);
        verify(bucketService).updateBucketSize(bucket, 100L);
        verify(fileRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteFileFileExistsDeletesFileAndUpdatesBucket() throws BucketSizeExceededException {
        Long fileId = 1L;
        Folder folder = new Folder();
        Bucket bucket = new Bucket();
        folder.setBucket(bucket);

        File file = File.builder()
                .fileSize(50L)
                .folder(folder)
                .build();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        doNothing().when(bucketService).updateBucketSize(bucket, -50L);
        doNothing().when(fileRepository).deleteById(fileId);

        fileService.deleteFile(fileId);

        verify(bucketService).updateBucketSize(bucket, -50L);
        verify(fileRepository).deleteById(fileId);
    }

    @Test
    void deleteFileBucketSizeExceededThrowsRuntimeException() throws BucketSizeExceededException {
        Long fileId = 1L;
        Folder folder = new Folder();
        Bucket bucket = new Bucket();
        folder.setBucket(bucket);

        File file = File.builder()
                .fileSize(50L)
                .folder(folder)
                .build();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        doThrow(new BucketSizeExceededException("Exceeded")).when(bucketService).updateBucketSize(bucket, -50L);

        assertThrows(RuntimeException.class, () -> fileService.deleteFile(fileId));

        verify(bucketService).updateBucketSize(bucket, -50L);
        verify(fileRepository, never()).deleteById(fileId);
    }

    @Test
    void saveFileCallsRepositoryAndReturnsSavedFile() {
        File file = new File();

        when(fileRepository.saveAndFlush(file)).thenReturn(file);

        File saved = fileService.saveFile(file);

        assertEquals(file, saved);
        verify(fileRepository).saveAndFlush(file);
    }

    @Test
    void findFilesByUserIdAndNameCallsRepositoryAndReturnsFiles() {
        Long userId = 1L;
        String search = "%file%";

        List<File> files = List.of(new File(), new File());

        when(fileRepository.findByUserIdAndFileNameLike(userId, search)).thenReturn(files);

        List<File> result = fileService.findFilesByUserIdAndName(userId, search);

        assertEquals(files, result);
        verify(fileRepository).findByUserIdAndFileNameLike(userId, search);
    }
}
