package com.worldcup.exception;

public class DuplicateTeamNameException extends RuntimeException {
    public DuplicateTeamNameException(String message) {
        super(message);
    }
}
