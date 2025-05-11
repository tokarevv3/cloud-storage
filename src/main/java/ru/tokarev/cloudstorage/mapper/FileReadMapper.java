package ru.tokarev.cloudstorage.mapper;

import org.springframework.stereotype.Component;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.dto.FileReadDto;

@Component
public class FileReadMapper implements Mapper<File, FileReadDto>{
    @Override
    public FileReadDto map(File obj) {
        return new FileReadDto(
                obj.getId(),
                obj.getFileName(),
                obj.getFilePath(),
                obj.getUploadedAt(),
                obj.getFileSize()
        );
    }
}
