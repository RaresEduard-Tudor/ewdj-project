package com.worldcup.controller;

import com.worldcup.domain.Prediction;
import com.worldcup.domain.User;
import com.worldcup.service.PredictionService;
import com.worldcup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/predictions")
@Slf4j
public class PredictionController {

    private final PredictionService predictionService;
    private final UserService userService;

    public PredictionController(PredictionService predictionService, UserService userService) {
        this.predictionService = predictionService;
        this.userService = userService;
    }

    @GetMapping
    public String myPredictions(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<Prediction> predictions = predictionService.findByUser(user);
        LocalDateTime cutoff = LocalDateTime.now().plusDays(3);
        Set<Long> editableIds = predictions.stream()
            .filter(p -> p.getMatch().getMatchDate().isAfter(cutoff))
            .map(Prediction::getId)
            .collect(Collectors.toSet());
        model.addAttribute("predictions", predictions);
        model.addAttribute("editableIds", editableIds);
        return "prediction/list";
    }
}
