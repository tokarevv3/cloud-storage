package ru.tokarev.cloudstorage.dto;

import lombok.Value;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;

import java.time.LocalDateTime;

@Value
public class FolderReadDto {

    Long id;
    String name;
    String path;
    LocalDateTime uploadedAt;
    Folder parentId;
    Bucket bucketId;
}
