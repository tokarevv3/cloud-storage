package ru.tokarev.cloudstorage.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class FileReadDto {

    Long id;

    String fileName;

    String filePath;

    LocalDateTime uploadedAt;

    String fileSize;

}
