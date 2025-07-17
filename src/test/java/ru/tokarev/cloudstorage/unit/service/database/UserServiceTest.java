package ru.tokarev.cloudstorage.unit.service.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.tokarev.cloudstorage.database.entity.Role;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.database.repositorty.UserRepository;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.dto.UserReadDto;
import ru.tokarev.cloudstorage.mapper.UserCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.UserReadMapper;
import ru.tokarev.cloudstorage.service.database.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCreateEditMapper userCreateEditMapper;

    @Mock
    private UserReadMapper userReadMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void findByIdUserExistsReturnsUser() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findByIdUserNotFoundReturnsEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllReturnsMappedList() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        UserReadDto dto1 = new UserReadDto(1L, "Dummy", "Dummy", "dummy@dummy.dummy", null, null, null);
        UserReadDto dto2 = new UserReadDto(2L, "Dummy", "Dummy", "dummy@dummy.dummy", null, null, null);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userReadMapper.map(user1)).thenReturn(dto1);
        when(userReadMapper.map(user2)).thenReturn(dto2);

        List<UserReadDto> result = userService.findAll();

        assertEquals(2, result.size());
        System.out.println(result);
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

    }

    @Test
    void saveCallsRepositoryAndReturnsUser() {
        User user = new User();

        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.save(user);

        assertEquals(user, saved);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUserExistsDeletesAndReturnsTrue() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.delete(1L);

        assertTrue(result);
        verify(userRepository).delete(user);
        verify(userRepository).flush();
    }

    @Test
    void deleteUserNotFoundReturnsFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.delete(1L);

        assertFalse(result);
        verify(userRepository, never()).delete(any());
        verify(userRepository, never()).flush();
    }

    @Test
    void updateUserExistsReturnsUpdatedDto() {
        User user = new User();
        UserCreateEditDto dto = new UserCreateEditDto("Dummy", "Dummy", "dummy@dummy.dummy", "dummy");
        User mappedUser = new User();
        User savedUser = new User();
        UserReadDto readDto = new UserReadDto(1L, "Dummy", "Dummy", "dummy@dummy.dummy", null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userCreateEditMapper.map(dto, user)).thenReturn(mappedUser);
        when(userRepository.saveAndFlush(mappedUser)).thenReturn(savedUser);
        when(userReadMapper.map(savedUser)).thenReturn(readDto);

        Optional<UserReadDto> result = userService.update(1L, dto);

        assertTrue(result.isPresent());
        assertEquals(readDto, result.get());
    }

    @Test
    void updateUserNotFoundReturnsEmpty() {
        UserCreateEditDto dto = new UserCreateEditDto("Dummy", "Dummy", "dummy@dummy.dummy", "dummy");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserReadDto> result = userService.update(1L, dto);

        assertTrue(result.isEmpty());
    }

    @Test
    void createMapsAndSavesUserReturnsUser() {
        UserCreateEditDto dto = new UserCreateEditDto("Dummy", "Dummy", "dummy@dummy.dummy", "dummy");
        User mappedUser = new User();
        User savedUser = new User();

        when(userCreateEditMapper.map(dto)).thenReturn(mappedUser);
        when(userRepository.saveAndFlush(mappedUser)).thenReturn(savedUser);

        Optional<User> result = userService.create(dto);

        assertTrue(result.isPresent());
        assertEquals(savedUser, result.get());
    }

    @Test
    void loadUserByUsernameUserExistsReturnsUserDetails() {
        User user = new User();
        user.setEmail("dummy@dummy.dummy");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("dummy@dummy.dummy")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("dummy@dummy.dummy");

        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals(Role.USER.name())));
    }

    @Test
    void loadUserByUsernameUserNotFoundThrowsException() {
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknown"));
    }

    @Test
    void findByUsernameUserExistsReturnsUser() {
        User user = new User();
        when(userRepository.findByEmail("email")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("email");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findByUsernameUserNotFoundReturnsEmpty() {
        when(userRepository.findByEmail("email")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("email");

        assertTrue(result.isEmpty());
    }

    @Test
    void makeUserAdminUserExistsSetsRoleAndSaves() {
        User user = new User();
        user.setRole(Role.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        boolean result = userService.makeUserAdmin(1L);

        assertTrue(result);
        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void makeUserAdminUserNotFoundThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.makeUserAdmin(1L));
    }

    @Test
    void updatePasswordUserExistsEncodesAndSaves() {
        User user = new User();
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(user)).thenReturn(user);

        boolean result = userService.updatePassword(1L, rawPassword);

        assertTrue(result);
        assertEquals(encodedPassword, user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updatePasswordUserNotFoundThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.updatePassword(1L, "password"));
    }
}
