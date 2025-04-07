package ru.tokarev.cloudstorage.service;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.repositorty.FileRepository;
import ru.tokarev.cloudstorage.database.repositorty.FolderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileFolderService {

//    private final MinioClient minioClient;
//    private final FolderRepository folderRepository;
//    private final FileRepository fileRepository;

//    public void getFilesInFolder(String folderPath) {
//        Folder folderByPath = folderRepository.getFolderByPath(folderPath);
//        List<>
//        List<Object> allByFolder = fileRepository.getAllByFolder(folderByPath);



}
