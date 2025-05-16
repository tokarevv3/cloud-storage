package ru.tokarev.cloudstorage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Value;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Role;

import java.math.BigDecimal;

@Value
public class UserCreateEditDto {

    String firstName; //editable
    String lastName; //editable
    @Email
    String email; //editable
    @Size(min = 6, max = 18)
    String rawPassword; //editable
}
