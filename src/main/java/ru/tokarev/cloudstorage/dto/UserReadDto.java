package ru.tokarev.cloudstorage.dto;

import lombok.Value;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Role;


@Value
public class UserReadDto {

    Long id;
    String firstName;
    String lastName;
    String email;
    BucketReadDto bucket;
    Role role;
    Long maxCapacityInGb;

}
