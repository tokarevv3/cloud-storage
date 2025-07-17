package ru.tokarev.cloudstorage.unit.service;

import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.Result;
import io.minio.GetObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.service.S3Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class S3ServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBucketSuccess() throws Exception {
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));
        assertTrue(s3Service.createBucket("test-bucket"));
    }

    @Test
    void createBucketFailure() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).makeBucket(any(MakeBucketArgs.class));
        assertFalse(s3Service.createBucket("test-bucket"));
    }

    @Test
    void getBucketsListSuccess() throws Exception {
        List<Bucket> buckets = List.of(mock(Bucket.class));
        when(minioClient.listBuckets()).thenReturn(buckets);
        assertEquals(buckets, s3Service.getBucketsList());
    }

    @Test
    void getBucketsListFailure() throws Exception {
        when(minioClient.listBuckets()).thenThrow(new RuntimeException());
        assertNull(s3Service.getBucketsList());
    }

    @Test
    void uploadFileSuccess() throws Exception {
        ObjectWriteResponse response = mock(ObjectWriteResponse.class);
        doReturn(response).when(minioClient).putObject(any(PutObjectArgs.class));
        InputStream stream = new ByteArrayInputStream("file".getBytes());
        assertTrue(s3Service.uploadFile(1L, "file.txt", stream, 4));
    }

    @Test
    void uploadFileFailure() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).putObject(any(PutObjectArgs.class));
        InputStream stream = new ByteArrayInputStream("file".getBytes());
        assertFalse(s3Service.uploadFile(1L, "file.txt", stream, 4));
    }

    @Test
    void createFolderSuccess() throws Exception {
        ObjectWriteResponse response = mock(ObjectWriteResponse.class);
        doReturn(response).when(minioClient).putObject(any(PutObjectArgs.class));
        assertTrue(s3Service.createFolder("bucket", "folder", "path"));
    }

    @Test
    void createFolderFailure() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).putObject(any(PutObjectArgs.class));
        assertFalse(s3Service.createFolder("bucket", "folder", "path"));
    }

    @Test
    void createRootFolderSuccess() throws Exception {
        ObjectWriteResponse response = mock(ObjectWriteResponse.class);
        doReturn(response).when(minioClient).putObject(any(PutObjectArgs.class));
        assertTrue(s3Service.createRootFolder("bucket"));
    }

    @Test
    void createRootFolderFailure() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).putObject(any(PutObjectArgs.class));
        assertFalse(s3Service.createRootFolder("bucket"));
    }

    @Test
    void downloadFileSuccess() throws Exception {
        byte[] expected = "data".getBytes();
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.readAllBytes()).thenReturn(expected);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);
        byte[] result = s3Service.downloadFile("bucket", "file.txt");
        assertArrayEquals(expected, result);
    }

    @Test
    void downloadFileFailure() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException());
        assertNull(s3Service.downloadFile("bucket", "file.txt"));
    }

    @Test
    void deleteFileSuccess() throws Exception {
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));
        assertTrue(s3Service.deleteFile("bucket", "path/file.txt"));
    }

    @Test
    void deleteFileFailure() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).removeObject(any(RemoveObjectArgs.class));
        assertFalse(s3Service.deleteFile("bucket", "path/file.txt"));
    }

    @Test
    void updateFileSuccess() throws Exception {
        ObjectWriteResponse response = mock(ObjectWriteResponse.class);
        doReturn(response).when(minioClient).copyObject(any(CopyObjectArgs.class));
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));
        assertTrue(s3Service.updateFile("bucket", "old.txt", "new.txt"));
    }

    @Test
    void updateFileFailure() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).copyObject(any(CopyObjectArgs.class));
        assertFalse(s3Service.updateFile("bucket", "old.txt", "new.txt"));
    }

    @Test
    void deleteFolderSuccess() {
        Folder folder = new Folder();
        ru.tokarev.cloudstorage.database.entity.Bucket bucket = new ru.tokarev.cloudstorage.database.entity.Bucket();
        bucket.setName("bucket");
        folder.setBucket(bucket);
        folder.setPath("/path/");
        folder.setName("folder");

        Iterable<Result<Item>> iterable = Stream.<Result<Item>>of()::iterator;
        when(minioClient.listObjects(any(ListObjectsArgs.class))).thenReturn(iterable);

        assertTrue(s3Service.deleteFolder(folder));
    }

    @Test
    void deleteFolderFailure() {
        Folder folder = new Folder();
        ru.tokarev.cloudstorage.database.entity.Bucket bucket = new ru.tokarev.cloudstorage.database.entity.Bucket();
        bucket.setName("bucket");
        folder.setBucket(bucket);
        folder.setPath("/path/");
        folder.setName("folder");



        when(minioClient.listObjects(any(ListObjectsArgs.class)))
                .thenThrow(new RuntimeException());

        assertFalse(s3Service.deleteFolder(folder));
    }

    @Test
    void updateFolderPathSuccess() throws Exception {
        Result<Item> result = mock(Result.class);
        Item item = mock(Item.class);
        when(item.objectName()).thenReturn("old/path/file.txt");
        when(result.get()).thenReturn(item);
        Iterable<Result<Item>> iterable = List.of(result);
        when(minioClient.listObjects(any(ListObjectsArgs.class))).thenReturn(iterable);

        ObjectWriteResponse response = mock(ObjectWriteResponse.class);

        doReturn(response).when(minioClient).copyObject(any(CopyObjectArgs.class));
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertTrue(s3Service.updateFolderPath("bucket", "old/path", "new/path"));
    }

    @Test
    void updateFolderPathFailure() {
        when(minioClient.listObjects(any(ListObjectsArgs.class)))
                .thenThrow(new RuntimeException("Error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                s3Service.updateFolderPath("bucket", "old", "new")
        );
        assertTrue(ex.getMessage().contains("Failed to update folder path"));
    }
}
