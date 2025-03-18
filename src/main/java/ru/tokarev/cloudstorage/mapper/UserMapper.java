package ru.tokarev.cloudstorage.mapper;

import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.UserDto;

public class UserMapper {

    public static UserDto mapper(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getLogin(),
                user.getRole(),
                user.getMemoryUsage()
        );
    }
}
