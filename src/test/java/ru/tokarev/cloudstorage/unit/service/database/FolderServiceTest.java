package ru.tokarev.cloudstorage.unit.service.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.repositorty.FolderRepository;
import ru.tokarev.cloudstorage.dto.FolderCreateEditDto;
import ru.tokarev.cloudstorage.dto.FolderReadDto;
import ru.tokarev.cloudstorage.dto.FolderTreeNode;
import ru.tokarev.cloudstorage.mapper.FolderCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.FolderReadMapper;
import ru.tokarev.cloudstorage.service.S3Service;
import ru.tokarev.cloudstorage.service.database.BucketService;
import ru.tokarev.cloudstorage.service.database.FileService;
import ru.tokarev.cloudstorage.service.database.FolderService;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    FolderRepository folderRepository;

    @Mock
    BucketService bucketService;

    @Mock
    FolderCreateEditMapper folderCreateEditMapper;

    @Mock
    FolderReadMapper folderReadMapper;

    @Mock
    S3Service s3Service;

    @Mock
    FileService fileService;

    @InjectMocks
    FolderService folderService;

    @Test
    void createFolderSuccessfulCreationReturnsFolderReadDto() throws Exception {
        Folder parentFolder = new Folder();
        parentFolder.setId(1L);
        parentFolder.setName("parent");
        parentFolder.setPath("/parentPath/");
        Bucket bucket = new Bucket();
        bucket.setName("bucketName");
        parentFolder.setBucket(bucket);

        String newFolderName = "newFolder";

        FolderCreateEditDto createDto = new FolderCreateEditDto(
                newFolderName,
                "/parentPath/parent/",
                LocalDateTime.now(),
                parentFolder,
                bucket
        );

        Folder mappedFolder = new Folder();
        Folder savedFolder = new Folder();
        FolderReadDto folderReadDto = new FolderReadDto(1L, "dummy", "/path/", LocalDateTime.now());

        when(folderRepository.getFolderById(1L)).thenReturn(Optional.of(parentFolder));
        when(s3Service.createFolder(bucket.getName(), newFolderName, createDto.getPath())).thenReturn(true);
        when(folderCreateEditMapper.map(any(FolderCreateEditDto.class))).thenReturn(mappedFolder);
        when(folderRepository.saveAndFlush(mappedFolder)).thenReturn(savedFolder);
        when(folderReadMapper.map(savedFolder)).thenReturn(folderReadDto);

        Optional<FolderReadDto> result = folderService.createFolder(newFolderName, 1L);

        assertTrue(result.isPresent());
        assertEquals(folderReadDto, result.get());

        verify(folderRepository).getFolderById(1L);
        verify(s3Service).createFolder(bucket.getName(), newFolderName, createDto.getPath());
        verify(folderCreateEditMapper).map(any(FolderCreateEditDto.class));
        verify(folderRepository).saveAndFlush(mappedFolder);
        verify(folderReadMapper).map(savedFolder);
    }

    @Test
    void createFolderParentFolderNotFoundThrowsFileNotFoundException() {
        when(folderRepository.getFolderById(42L)).thenReturn(Optional.empty());

        assertThrows(FileNotFoundException.class, () -> folderService.createFolder("name", 42L));
    }

    @Test
    void createFolderS3CreateFolderFailsReturnsEmptyOptional() throws Exception {
        Folder parentFolder = new Folder();
        parentFolder.setId(1L);
        parentFolder.setName("parent");
        parentFolder.setPath("/parentPath/");
        Bucket bucket = new Bucket();
        bucket.setName("bucketName");
        parentFolder.setBucket(bucket);

        when(folderRepository.getFolderById(1L)).thenReturn(Optional.of(parentFolder));
        when(s3Service.createFolder(anyString(), anyString(), anyString())).thenReturn(false);

        Optional<FolderReadDto> result = folderService.createFolder("newFolder", 1L);

        assertTrue(result.isEmpty());
        verify(folderCreateEditMapper, never()).map(any());
        verify(folderRepository, never()).saveAndFlush(any());
        verify(folderReadMapper, never()).map(any());
    }

    @Test
    void createRootFolderSuccessfulCreationReturnsFolder() {
        Bucket bucket = new Bucket();
        bucket.setId(1L);
        bucket.setName("bucketName");

        when(folderRepository.saveAndFlush(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bucketService.saveBucket(bucket)).thenReturn(Optional.of(bucket));
        when(s3Service.createRootFolder(bucket.getName())).thenReturn(true);

        Optional<Folder> result = folderService.createRootFolder(bucket);

        assertTrue(result.isPresent());
        assertEquals("root-folder", result.get().getName());
        assertEquals("/", result.get().getPath());
        assertEquals(bucket, result.get().getBucket());
        assertEquals(result.get(), bucket.getRootFolder());

        verify(folderRepository).saveAndFlush(any(Folder.class));
        verify(bucketService).saveBucket(bucket);
        verify(s3Service).createRootFolder(bucket.getName());
    }

    @Test
    void createRootFolderS3CreateRootFolderFailsReturnsEmptyOptional() {
        Bucket bucket = new Bucket();
        bucket.setName("bucketName");

        when(folderRepository.saveAndFlush(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bucketService.saveBucket(bucket)).thenReturn(Optional.of(bucket));
        when(s3Service.createRootFolder(bucket.getName())).thenReturn(false);

        Optional<Folder> result = folderService.createRootFolder(bucket);

        assertTrue(result.isEmpty());
    }

    @Test
    void getFolderByNameAndBucketCallsRepository() {
        Bucket bucket = new Bucket();
        bucket.setId(1L);
        Folder folder = new Folder();

        when(folderRepository.getFolderByNameAndBucketId("folderName", bucket.getId())).thenReturn(folder);

        Folder result = folderService.getFolderByNameAndBucket("folderName", bucket);

        assertEquals(folder, result);
    }

    @Test
    void getFolderByNameAndPathAndBucketCallsRepository() {
        Bucket bucket = new Bucket();
        bucket.setId(1L);
        Folder folder = new Folder();

        when(folderRepository.getFolderByNameAndPathAndBucketId("folderName", "/path/", bucket.getId())).thenReturn(folder);

        Folder result = folderService.getFolderByNameAndPathAndBucket("folderName", "/path/", bucket);

        assertEquals(folder, result);
    }

    @Test
    void getFoldersInFolderCallsRepository() {
        Folder folder = new Folder();
        List<Folder> folders = List.of(new Folder(), new Folder());

        when(folderRepository.getAllFoldersByParentId(folder)).thenReturn(folders);

        List<Folder> result = folderService.getFoldersInFolder(folder);

        assertEquals(folders, result);
    }

    @Test
    void getFolderByIdCallsRepository() {
        Folder folder = new Folder();
        when(folderRepository.getFolderById(1L)).thenReturn(Optional.of(folder));

        Optional<Folder> result = folderService.getFolderById(1L);

        assertTrue(result.isPresent());
        assertEquals(folder, result.get());
    }

    @Test
    void deleteFolderByIdFolderExistsDeletesFolder() {
        Folder folder = new Folder();
        folder.setId(1L);

        when(folderRepository.findById(1L)).thenReturn(Optional.of(folder));
        when(bucketService.existsByFolder(folder)).thenReturn(false);
        when(fileService.getFilesInFolder(folder)).thenReturn(List.of());
        when(folderRepository.getAllFoldersByParentId(folder)).thenReturn(List.of());
        when(s3Service.deleteFolder(folder)).thenReturn(true);

        boolean result = folderService.deleteFolderById(1L);

        assertTrue(result);

        verify(folderRepository).deleteById(folder.getId());
        verify(folderRepository).flush();
        verify(s3Service).deleteFolder(folder);
    }

    @Test
    void deleteFolderRecursiveFolderLinkedToBucketSkipsDeletion() {
        Folder folder = new Folder();
        folder.setId(1L);

        when(bucketService.existsByFolder(folder)).thenReturn(true);

        // call private method via reflection or test public method that calls it
        // Here test deleteFolderById indirectly tests deleteFolderRecursive skipping logic

        when(folderRepository.findById(1L)).thenReturn(Optional.of(folder));
        when(s3Service.deleteFolder(folder)).thenReturn(true);

        boolean result = folderService.deleteFolderById(1L);

        assertTrue(result);

        verify(folderRepository, never()).deleteById(any());
        verify(folderRepository, never()).flush();
    }

    @Test
    void getUserFolderTreeReturnsTreeStructure() {
        Bucket bucket = new Bucket();
        bucket.setId(1L);

        Folder rootFolder = new Folder();
        rootFolder.setId(1L);
        rootFolder.setName("root");
        rootFolder.setParent(null);

        Folder childFolder = new Folder();
        childFolder.setId(2L);
        childFolder.setName("child");
        childFolder.setParent(rootFolder);

        List<Folder> folders = List.of(rootFolder, childFolder);

        when(folderRepository.findByBucketId(bucket.getId())).thenReturn(folders);

        List<FolderTreeNode> tree = folderService.getUserFolderTree(bucket);

        assertEquals(1, tree.size());
        FolderTreeNode rootNode = tree.get(0);
        assertEquals(rootFolder.getId(), rootNode.getId());
        assertEquals(rootFolder.getName(), rootNode.getName());
        assertEquals(1, rootNode.getChildren().size());
        assertEquals(childFolder.getId(), rootNode.getChildren().get(0).getId());
    }

    @Test
    void buildTreeReturnsEmptyListWhenNoFolders() {
        Map<Long, List<Folder>> emptyMap = Collections.emptyMap();

        List<FolderTreeNode> tree = folderService.buildTree(0L, emptyMap);

        assertTrue(tree.isEmpty());
    }

    @Test
    void saveCallsRepository() {
        Folder folder = new Folder();

        when(folderRepository.saveAndFlush(folder)).thenReturn(folder);

        Folder result = folderService.save(folder);

        assertEquals(folder, result);
        verify(folderRepository).saveAndFlush(folder);
    }

    @Test
    void updateFolderRecursiveUpdatesFilesAndFolders() {
        Folder folder = new Folder();
        folder.setName("folderName");
        folder.setPath("/path/");

        File file1 = File.builder().fileSize(10L).fileName("file1").build();
        File file2 = File.builder().fileSize(20L).fileName("file2").build();

        List<File> filesInFolder = List.of(file1, file2);

        Folder subfolder = new Folder();
        subfolder.setName("subfolder");
        subfolder.setPath("/path/folderName/");
        List<Folder> foldersInFolder = List.of(subfolder);

        when(fileService.getFilesInFolder(folder)).thenReturn(filesInFolder);
        when(folderRepository.getAllFoldersByParentId(folder)).thenReturn(foldersInFolder);

        folderService.updateFolderRecursive(folder);

        // files' filePath updated and saved
        assertEquals("/path/folderName/", file1.getFilePath());
        assertEquals("/path/folderName/", file2.getFilePath());
        verify(fileService).saveFile(file1);
        verify(fileService).saveFile(file2);

        // subfolder path updated and updateFolderRecursive called recursively
        assertEquals("/path/folderName/", subfolder.getPath());
    }
}