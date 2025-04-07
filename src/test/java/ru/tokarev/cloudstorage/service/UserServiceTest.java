package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import ru.tokarev.cloudstorage.TestCloudStorateApplication;
import ru.tokarev.cloudstorage.database.entity.Role;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.dto.UserReadDto;
import ru.tokarev.cloudstorage.mapper.UserReadMapper;

import java.util.Optional;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
@SpringBootTest(classes = TestCloudStorateApplication.class)
public class UserServiceTest {

    private final UserService userService;
    @Autowired
    private UserReadMapper userReadMapper;


    @Test
    void createUser() {

        // id auto-increment
        // bucket - null-able.
        UserCreateEditDto preparedUser = new UserCreateEditDto(
                 "my-username",
                "my-login@mail.ru",
                "{noop}123",
                Role.USER,
                null
        );

        UserReadDto createdUser = userService.create(preparedUser);
        Optional<UserReadDto> DbUser = userService.findById(createdUser.getId()).map(userReadMapper::map);
        Assertions.assertEquals(createdUser.getUsername(), DbUser.get().getUsername());


    }
}
