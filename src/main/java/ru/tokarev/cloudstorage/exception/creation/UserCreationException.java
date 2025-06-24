package ru.tokarev.cloudstorage.exception.creation;

public class UserCreationException extends CreationException {
    public UserCreationException(String message) {
        super(message);
    }

    public UserCreationException() {
        super();
    }
}
