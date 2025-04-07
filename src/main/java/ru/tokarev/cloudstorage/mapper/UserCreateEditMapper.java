package ru.tokarev.cloudstorage.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.tokarev.cloudstorage.database.entity.User;
import ru.tokarev.cloudstorage.dto.UserCreateEditDto;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserCreateEditMapper implements Mapper<UserCreateEditDto, User> {

    private final PasswordEncoder passwordEncoder;

    @Override
    public  User map(UserCreateEditDto fromObj,User toObj) {
        copy(fromObj, toObj);
        return toObj;
    }

    @Override
    public  User map(UserCreateEditDto fromObj) {
        User user = new User();
        copy(fromObj, user);

        return user;

    }

    private void copy(UserCreateEditDto fromObj, User toObj) {
        toObj.setUsername(fromObj.getUsername());
        toObj.setLogin(fromObj.getLogin());
        toObj.setBucket(fromObj.getBucket());
        toObj.setRole(fromObj.getRole());

        Optional.ofNullable(fromObj.getRawPassword())
                .filter(StringUtils::hasText)
                .map(passwordEncoder::encode)
                .ifPresent(toObj::setPassword);
    }
}
