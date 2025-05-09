package ru.tokarev.cloudstorage.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.*;
import ru.tokarev.cloudstorage.database.repositorty.FileRepository;
import ru.tokarev.cloudstorage.dto.FileReadDto;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;


    public List<File> getFilesInFolder(Folder folder) {
        return fileRepository.getAllFilesByParentId(folder);
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
                .fileType(Type.UNKNOWN)
                .folder(parentFolder)
                .build();

        Bucket bucket = parentFolder.getBucketId();
        bucket.updateSize(size);
//        bucketService.saveBucket(bucket);

        fileRepository.saveAndFlush(file);

        return true;
    }



    public static String isSlashEnded(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path = path + '/';
        }
        return path;
    }

    public void deleteFile(Long id) {
        fileRepository.deleteById(id);
    }

    public File saveFile(File file) {
        return fileRepository.saveAndFlush(file);
    }
}
