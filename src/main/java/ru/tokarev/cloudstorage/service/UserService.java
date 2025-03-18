package ru.tokarev.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.database.repositorty.UserRepository;
import ru.tokarev.cloudstorage.dto.UserDto;
import ru.tokarev.cloudstorage.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public Optional<User> findById(Long id) {
        return userRepository
                .findById(id);
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapper)
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

    //TODO make logic
    public Optional<UserDto> update(Long id, UserDto userDto) {
        return Optional.empty();
    }
}
