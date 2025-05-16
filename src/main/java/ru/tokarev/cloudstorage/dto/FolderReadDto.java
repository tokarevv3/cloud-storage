package ru.tokarev.cloudstorage.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class FolderReadDto {

    Long id;
    String folderName;
    String folderPath;
    LocalDateTime uploadedAt;
}
