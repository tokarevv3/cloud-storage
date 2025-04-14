package ru.tokarev.cloudstorage.mapper;

import org.springframework.stereotype.Component;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.dto.FolderCreateEditDto;

@Component
public class FolderCreateEditMapper implements Mapper<FolderCreateEditDto, Folder> {
    @Override
    public Folder map(FolderCreateEditDto obj) {
        Folder toObj = new Folder();
        copy(obj, toObj);
        return toObj;
    }

    @Override
    public Folder map(FolderCreateEditDto fromObj, Folder toObj) {
        copy(fromObj, toObj);
        return toObj;
    }

    public void copy(FolderCreateEditDto fromObj, Folder toObj) {
        toObj.setName(fromObj.getName());
        toObj.setPath(fromObj.getPath());
        toObj.setBucketId(fromObj.getBucketId());
        toObj.setUploadedAt(fromObj.getUploadedAt());
        toObj.setParent(fromObj.getParentId());
    }
}
