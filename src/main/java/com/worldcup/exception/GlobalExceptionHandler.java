package com.worldcup.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleTeamNotFound(TeamNotFoundException ex, Model model) {
        log.warn("Team not found: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(DuplicateTeamNameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateTeam(DuplicateTeamNameException ex, Model model) {
        log.warn("Duplicate team name: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(TeamJoinException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTeamJoin(TeamJoinException ex, Model model) {
        log.warn("Team join failed: {}", ex.getMessage());
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

    @ExceptionHandler(DuplicateMatchException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateMatch(DuplicateMatchException ex, Model model) {
        log.warn("Duplicate match: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(ResultBeforeKickoffException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleResultBeforeKickoff(ResultBeforeKickoffException ex, Model model) {
        log.warn("Result before kickoff: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateUsername(DuplicateUsernameException ex, Model model) {
        log.warn("Duplicate username: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateEmail(DuplicateEmailException ex, Model model) {
        log.warn("Duplicate email: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, Model model) {
        log.warn("404: {}", ex.getMessage());
        return "error/404";
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
