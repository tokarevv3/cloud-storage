package ru.tokarev.cloudstorage.mapper;

import org.springframework.stereotype.Component;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.dto.BucketReadDto;

@Component
public class BucketReadMapper implements Mapper<Bucket, BucketReadDto> {
    @Override
    public BucketReadDto map(Bucket obj) {
        return new BucketReadDto(
                obj.getId(),
                obj.getName(),
                obj.getSize(),
                obj.getUser().getId()
        );
    }
}
