package ru.tokarev.cloudstorage.mapper;

import org.springframework.stereotype.Component;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.dto.FolderReadDto;

@Component
public class FolderReadMapper implements Mapper<Folder, FolderReadDto> {
    @Override
    public FolderReadDto map(Folder obj) {
        return new FolderReadDto(
                obj.getId(),
                obj.getName(),
                obj.getPath(),
                obj.getUploadedAt(),
                obj.getParent(),
                obj.getBucketId()
        );
    }
}
