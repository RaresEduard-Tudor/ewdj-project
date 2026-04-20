package com.worldcup.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PredictionDeadlineException.class)
    public String handleDeadline(PredictionDeadlineException ex, Model model) {
        log.warn("Prediction deadline exceeded: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("deadline", ex.getDeadline());
        return "error/error";
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public String handleTeamNotFound(TeamNotFoundException ex, Model model) {
        log.warn("Team not found: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(DuplicateTeamNameException.class)
    public String handleDuplicateTeam(DuplicateTeamNameException ex, Model model) {
        log.warn("Duplicate team name: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(MatchNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleMatchNotFound(MatchNotFoundException ex, Model model) {
        log.warn("Match not found: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public String handleDuplicateUsername(DuplicateUsernameException ex, Model model) {
        log.warn("Duplicate username: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        log.warn("Access denied: {}", ex.getMessage());
        model.addAttribute("error", "You do not have permission to access this page.");
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) {
        log.error("Unhandled exception", ex);
        model.addAttribute("error", "An unexpected error occurred.");
        return "error/error";
    }
}
