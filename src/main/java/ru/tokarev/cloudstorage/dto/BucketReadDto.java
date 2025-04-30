package ru.tokarev.cloudstorage.dto;


import lombok.Value;


@Value
public class BucketReadDto {

    String name;
    Long size;
}
