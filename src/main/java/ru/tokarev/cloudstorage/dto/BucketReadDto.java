package ru.tokarev.cloudstorage.dto;


import lombok.Value;


@Value
public class BucketReadDto {

    Long id;
    String name;
    Long size;
    Long userId;
}
