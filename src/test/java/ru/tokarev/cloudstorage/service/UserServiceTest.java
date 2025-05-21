package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.tokarev.cloudstorage.TestCloudStorageApplication;
import ru.tokarev.cloudstorage.database.entity.Role;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.dto.UserReadDto;
import ru.tokarev.cloudstorage.mapper.UserReadMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
@SpringBootTest(classes = TestCloudStorageApplication.class)
@Transactional
public class UserServiceTest {

    private final UserService userService;

    @Test
    public void userExistsById() {
        Long userId = 21L;
        Optional<User> userById = userService.findById(userId);
        assertTrue(userById.isPresent());
    }

    @Test
    public void userDontExistsById() {
        Long userId = 1L;
        Optional<User> userById = userService.findById(userId);
        assertFalse(userById.isPresent());
    }

    @Test
    public void userExistsByLogin() {
        String login = "sset@mail.ru";
        Optional<User> userByLogin = userService.findByUsername(login);
        assertTrue(userByLogin.isPresent());
    }

    @Test
    public void userDontExistsByLogin() {
        String login = "set@mail.ru";
        Optional<User> userByLogin = userService.findByUsername(login);
        assertFalse(userByLogin.isPresent());
    }

    @Test
    public void makeUserAdmin() {
        User user = User.builder()
                .role(Role.USER)
                .email("test@mail.ru")
                .password("{noop}123")
                .bucket(null)
                .firstName("test")
                .lastName("test")
                .build();

        userService.save(user);

        userService.makeUserAdmin(user.getId());

        var adminUser = userService.findById(user.getId()).get();

        assertEquals(Role.ADMIN, adminUser.getRole());

    }

    @Test
    public void deleteUser() {
        long userId = 21L;

        userService.delete(userId);

        Optional<User> userById = userService.findById(userId);

        assertFalse(userById.isPresent());
    }

    @Test
    public void createdUserFieldsAreNotNullExceptBucket() {

        UserCreateEditDto creatingUser = new UserCreateEditDto(
                "testFirstName",
                "testLastName",
                "test@email.ru",
                "testpassword"
        );

        var createdUser = userService.create(creatingUser).get();

        assertAll(
                () -> assertNotNull(createdUser.getId()),
                () -> assertNotNull(createdUser.getFirstName()),
                () -> assertNotNull(createdUser.getFirstName()),
                () -> assertNotNull(createdUser.getEmail()),
                () -> assertNotNull(createdUser.getPassword()),
                () -> assertNotNull(createdUser.getRole()),
                () -> assertNull(createdUser.getBucket())
        );
    }
}
