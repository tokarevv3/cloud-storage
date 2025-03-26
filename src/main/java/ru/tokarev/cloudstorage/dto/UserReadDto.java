package ru.tokarev.cloudstorage.dto;

import lombok.Value;
import ru.tokarev.cloudstorage.database.entity.Role;

import java.math.BigDecimal;

@Value
public class UserReadDto {

    Long id;
    String username;
    String login;
    Role role;
    BigDecimal memoryUsage;
}
