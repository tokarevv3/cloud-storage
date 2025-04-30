package ru.tokarev.cloudstorage.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.BucketReadDto;
import ru.tokarev.cloudstorage.dto.UserReadDto;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserReadMapper implements Mapper<User, UserReadDto> {

    private final BucketReadMapper bucketReadMapper;

    @Override
    public UserReadDto map(User obj) {
        BucketReadDto bucket = Optional.ofNullable(obj.getBucket())
                .map(bucketReadMapper::map)
                .orElse(null);
        return new UserReadDto(
                obj.getId(),
                obj.getUsername(),
                obj.getLogin(),
                obj.getRole(),
                bucket);
    }

}
