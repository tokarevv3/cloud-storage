package ru.tokarev.cloudstorage.dto;

import lombok.Value;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.database.entity.User;

@Value
public class BucketCreateEditDto {

    String name;
    Long size;
    User user;
    Folder rootFolderId;
}
