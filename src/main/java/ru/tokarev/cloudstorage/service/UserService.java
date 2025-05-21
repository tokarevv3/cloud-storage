package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tokarev.cloudstorage.database.entity.Role;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.database.repositorty.UserRepository;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;
import ru.tokarev.cloudstorage.dto.UserReadDto;
import ru.tokarev.cloudstorage.mapper.UserCreateEditMapper;
import ru.tokarev.cloudstorage.mapper.UserReadMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    private final UserCreateEditMapper userCreateEditMapper;
    private final UserReadMapper userReadMapper;

    private final Role USER_ADMIN_ROLE = Role.ADMIN;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findById(Long id) {
        return userRepository
                .findById(id);
    }

    public List<UserReadDto> findAll() {
        return userRepository.findAll().stream()
                .map(userReadMapper::map)
                .toList();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public boolean delete(Long id) {
        return userRepository.findById(id)
                .map(entity -> {
                    userRepository.delete(entity);
                    userRepository.flush();
                    return true;
                })
                .orElse(false);
    }

    public Optional<UserReadDto> update(Long id, UserCreateEditDto userCreateEditDto) {
        return userRepository.findById(id)
                .map(entity -> userCreateEditMapper.map(userCreateEditDto, entity))
                .map(userRepository::saveAndFlush)
                .map(userReadMapper::map);
    }

    public Optional<User> create(UserCreateEditDto userCreateEditDto) {
        return Optional.of(userCreateEditDto)
                .map(userCreateEditMapper::map)
                .map(userRepository::saveAndFlush);
//                .map(userReadMapper::map)
    }

    @Override
    public UserDetails loadUserByUsername(String username)  {
        return userRepository.findByEmail(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        Collections.singleton(user.getRole())
                )).orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user: " + username));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByEmail(username);
    }

    public boolean makeUserAdmin(Long userId) {
        User userRepositoryById = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        userRepositoryById.setRole(USER_ADMIN_ROLE);

        userRepository.saveAndFlush(userRepositoryById);

        return true;
    }

    public boolean updatePassword(Long userId, String password) {
        User updateUser = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String encodedPassword = passwordEncoder.encode(password);


        updateUser.setPassword(encodedPassword);

        userRepository.save(updateUser);

        return true;
    }
}
