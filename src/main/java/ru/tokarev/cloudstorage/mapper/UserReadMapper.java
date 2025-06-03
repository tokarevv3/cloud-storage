package ru.tokarev.cloudstorage.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.BucketReadDto;
import ru.tokarev.cloudstorage.dto.UserReadDto;
import ru.tokarev.cloudstorage.service.BucketService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserReadMapper implements Mapper<User, UserReadDto> {

    private final BucketReadMapper bucketReadMapper;
    private final BucketService bucketService;

    @Override
    public UserReadDto map(User obj) {
        BucketReadDto bucket = Optional.ofNullable(obj.getBucket())
                .map(bucketReadMapper::map)
                .orElse(null);
        return new UserReadDto(
                obj.getId(),
                obj.getFirstName(),
                obj.getLastName(),
                obj.getEmail(),
                bucket,
                obj.getRole(),
                bucketService.getMaxCapacity());
    }

}
