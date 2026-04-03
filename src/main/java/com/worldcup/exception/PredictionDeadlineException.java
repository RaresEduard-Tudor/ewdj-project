package com.worldcup.exception;

import java.time.LocalDateTime;

public class PredictionDeadlineException extends RuntimeException {

    private final LocalDateTime deadline;

    public PredictionDeadlineException(String matchName, LocalDateTime deadline) {
        super("Prediction deadline passed for: " + matchName);
        this.deadline = deadline;
    }

    public LocalDateTime getDeadline() { return deadline; }
}
