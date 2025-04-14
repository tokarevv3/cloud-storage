package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tokarev.cloudstorage.database.entity.Folder;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PreviewService {

//    private final MinioClient minioClient;
//    private final FolderRepository folderRepository;
//    private final FileRepository fileRepository;

    private final FolderService folderService;

//    public void getFilesInFolder(String folderPath) {
//        Folder folderByPath = folderRepository.getFolderByPath(folderPath);
//        List<>
//        List<Object> allByFolder = fileRepository.getAllByFolder(folderByPath);


    public Map<Long, String> getListOfFilesAndFoldersInFolder(String path) {

        System.out.println(path);

        Folder folderByPath = folderService.getFolderByPath(path);
        System.out.println(folderByPath.getName());

        return folderService.getListInCurrentFolder(folderByPath);

    }



}
