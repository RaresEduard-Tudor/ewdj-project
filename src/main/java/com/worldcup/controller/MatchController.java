package com.worldcup.controller;

import com.worldcup.domain.Match;
import com.worldcup.domain.Prediction;
import com.worldcup.domain.User;
import com.worldcup.dto.PredictionDto;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.service.MatchService;
import com.worldcup.service.PredictionService;
import com.worldcup.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/matches")
@Slf4j
public class MatchController {

    private final MatchService matchService;
    private final PredictionService predictionService;
    private final UserService userService;
    private final PredictionRepository predictionRepository;

    public MatchController(MatchService matchService, PredictionService predictionService,
                           UserService userService, PredictionRepository predictionRepository) {
        this.matchService = matchService;
        this.predictionService = predictionService;
        this.userService = userService;
        this.predictionRepository = predictionRepository;
    }

    @GetMapping
    public String list(Model model, Principal principal) {
        model.addAttribute("matches", matchService.findAll());
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            Map<Long, Prediction> predictions = predictionRepository.findByUser(user).stream()
                .collect(Collectors.toMap(p -> p.getMatch().getId(), p -> p));
            model.addAttribute("userPredictions", predictions);
        }
        return "match/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Match match = matchService.findById(id);
        model.addAttribute("match", match);
        model.addAttribute("predictionDto", new PredictionDto());
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            predictionRepository.findByUserAndMatch(user, match)
                .ifPresent(p -> model.addAttribute("existingPrediction", p));
        }
        return "match/detail";
    }

    @PostMapping("/{id}/predict")
    public String predict(@PathVariable Long id,
                          @Valid @ModelAttribute PredictionDto predictionDto,
                          BindingResult bindingResult,
                          @RequestParam(defaultValue = "/matches") String redirectTo,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("predictionError", "prediction.error.invalid");
            return "redirect:" + redirectTo;
        }
        predictionDto.setMatchId(id);
        User user = userService.findByUsername(principal.getName());
        predictionService.savePrediction(predictionDto, user);
        redirectAttributes.addFlashAttribute("predictionSuccess", "prediction.saved");
        return "redirect:" + redirectTo;
    }
}
