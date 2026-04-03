package com.worldcup.controller.admin;

import com.worldcup.domain.Match;
import com.worldcup.dto.MatchDto;
import com.worldcup.exception.DuplicateMatchException;
import com.worldcup.service.MatchService;
import com.worldcup.service.ScoringService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Locale;

@Controller
@RequestMapping("/admin/matches")
@Slf4j
public class AdminMatchController {

    private final MatchService matchService;
    private final ScoringService scoringService;
    private final MessageSource messageSource;

    public AdminMatchController(MatchService matchService, ScoringService scoringService,
                                MessageSource messageSource) {
        this.matchService = matchService;
        this.scoringService = scoringService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("matches", matchService.findAll());
        return "admin/matches";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("matchDto", new MatchDto());
        return "admin/match-form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute MatchDto matchDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       Locale locale) {
        if (bindingResult.hasErrors()) {
            return "admin/match-form";
        }
        try {
            matchService.save(matchDto);
            redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("match.add.success", null, locale));
            return "redirect:/admin/matches";
        } catch (DuplicateMatchException e) {
            bindingResult.reject("match.duplicate", e.getMessage());
            return "admin/match-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Match match = matchService.findById(id);
        MatchDto dto = new MatchDto();
        dto.setId(match.getId());
        dto.setCountryA(match.getCountryA());
        dto.setCountryB(match.getCountryB());
        dto.setMatchDate(match.getMatchDate());
        dto.setCity(match.getCity());
        dto.setStadium(match.getStadium());
        dto.setStadiumCode(match.getStadiumCode());
        dto.setChecksum(match.getChecksum());
        model.addAttribute("matchDto", dto);
        return "admin/match-form";
    }

    @PostMapping("/{id}/result")
    public String saveResult(@PathVariable Long id,
                             @RequestParam Integer goalsA,
                             @RequestParam Integer goalsB,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        matchService.saveResult(id, goalsA, goalsB);
        Match match = matchService.findById(id);
        scoringService.calculateScoresForMatch(match);
        redirectAttributes.addFlashAttribute("successMessage",
            messageSource.getMessage("match.result.success", null, locale));
        return "redirect:/admin/matches";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes, Locale locale) {
        matchService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
            messageSource.getMessage("match.delete.success", null, locale));
        return "redirect:/admin/matches";
    }
}
