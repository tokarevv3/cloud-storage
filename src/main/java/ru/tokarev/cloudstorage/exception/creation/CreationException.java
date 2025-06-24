package ru.tokarev.cloudstorage.exception.creation;

public abstract class CreationException extends Exception {
    public CreationException(String message) {
        super(message);
    }

    public CreationException() {
        super();
    }
}
