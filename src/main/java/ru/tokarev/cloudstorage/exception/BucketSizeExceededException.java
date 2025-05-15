package ru.tokarev.cloudstorage.exception;

public class BucketSizeExceededException extends Exception {

    public BucketSizeExceededException(String message) {
        super(message);
    }

    public BucketSizeExceededException() {
        super();
    }
}
