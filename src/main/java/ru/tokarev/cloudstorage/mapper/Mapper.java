package ru.tokarev.cloudstorage.mapper;

public interface Mapper<F, T> {

    T map(F obj);

    default T map(F fromObj, T toObj) {
        return toObj;
    }
}
