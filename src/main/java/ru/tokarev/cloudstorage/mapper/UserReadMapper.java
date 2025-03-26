package ru.tokarev.cloudstorage.mapper;

import org.springframework.stereotype.Component;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.UserReadDto;

@Component
public class UserReadMapper implements Mapper<User, UserReadDto> {

    @Override
    public UserReadDto map(User obj) {
        return new UserReadDto(
                obj.getId(),
                obj.getUsername(),
                obj.getLogin(),
                obj.getRole(),
                obj.getMemoryUsage()
        );
    }

}
