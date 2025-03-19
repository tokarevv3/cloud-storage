package ru.tokarev.cloudstorage.dto;

import lombok.Value;
import ru.tokarev.cloudstorage.database.entity.Role;

import java.math.BigDecimal;

@Value
public class UserDto {

    Long id;
    String username;
    String login;
    Role role;
    BigDecimal memoryUsage;
}
