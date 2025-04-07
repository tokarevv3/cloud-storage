package ru.tokarev.cloudstorage.dto;

import lombok.Value;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Role;

import java.math.BigDecimal;

@Value
public class UserCreateEditDto {

    String username;
    String login;
    String rawPassword;
    Role role;
    Bucket bucket;
}
