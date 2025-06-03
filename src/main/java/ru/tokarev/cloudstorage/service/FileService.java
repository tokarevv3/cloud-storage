package ru.tokarev.cloudstorage.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.*;
import ru.tokarev.cloudstorage.database.repositorty.FileRepository;
import ru.tokarev.cloudstorage.exception.BucketSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final BucketService bucketService;


    public List<File> getFilesInFolder(Folder folder) {
        return fileRepository.findAllByParentFolder(folder);
    }

    public Optional<File> getFile(Long id) {
        return fileRepository.findById(id);
    }


    //TODO: pure SQL method, disctruct to s3service upload method
    public boolean uploadFile(String fileName, String filePath, long size, String contentType, Folder parentFolder) {

        File file = File.builder()
                .fileName(fileName)
                .filePath(filePath)
                .fileSize(String.valueOf(size)) // TODO:: to Long
                .contentType(contentType)
                .uploadedAt(LocalDateTime.now())
                .folder(parentFolder)
                .build();

        Bucket bucket = parentFolder.getBucketId();
        try {
            bucketService.updateBucketSize(bucket, size);
            fileRepository.saveAndFlush(file);
            return true;
        } catch (BucketSizeExceededException e) {
            log.error(e.getMessage());
            return false;
        }

    }

    public void deleteFile(Long id) {

        getFile(id).ifPresent(file -> {
            Bucket bucket = file.getFolder().getBucketId();
            String fileSize = file.getFileSize();
            bucket.updateSize(-Long.parseLong(fileSize));
        });

        fileRepository.deleteById(id);
    }

    public File saveFile(File file) {
        return fileRepository.saveAndFlush(file);
    }

    public List<File> findFilesByUserIdAndName(Long id, String search) {
        return fileRepository.findByUserIdAndFileNameLike(id, search);
    }
}
