package ru.tokarev.cloudstorage.mapper;

import org.springframework.stereotype.Component;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.dto.BucketCreateEditDto;

@Component
public class BucketCreateEditMapper implements Mapper<BucketCreateEditDto, Bucket> {


    @Override
    public Bucket map(BucketCreateEditDto fromObj) {
        Bucket toObj = new Bucket();
        copy(fromObj, toObj);
        return toObj;
    }

    @Override
    public Bucket map(BucketCreateEditDto fromObj, Bucket toObj) {
        copy(fromObj, toObj);
        return toObj;
    }

    private void copy(BucketCreateEditDto fromObj, Bucket toObj) {
        toObj.setName(fromObj.getName());
        toObj.setSize(fromObj.getSize());
        toObj.setUser(fromObj.getUser());
        toObj.setRootFolder(fromObj.getRootFolder());
    }
}
