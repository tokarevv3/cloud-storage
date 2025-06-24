package ru.tokarev.cloudstorage.exception.creation;

public class BucketCreationException extends CreationException {
    public BucketCreationException(String message) {
        super(message);
    }

    public BucketCreationException() {
        super();
    }
}
