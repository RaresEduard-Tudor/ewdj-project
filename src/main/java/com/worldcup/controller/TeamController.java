package com.worldcup.controller;

import com.worldcup.domain.Team;
import com.worldcup.domain.User;
import com.worldcup.exception.DuplicateTeamNameException;
import com.worldcup.exception.TeamJoinException;
import com.worldcup.exception.TeamNotFoundException;
import com.worldcup.service.TeamService;
import com.worldcup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.hibernate.Hibernate;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/team")
@Slf4j
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

    public TeamController(TeamService teamService, UserService userService) {
        this.teamService = teamService;
        this.userService = userService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String myTeams(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<Team> teams = user.getTeams().stream()
            .sorted(java.util.Comparator.comparing(Team::getName))
            .collect(Collectors.toList());
        teams.forEach(t -> {
            Hibernate.initialize(t.getMembers());
            Hibernate.initialize(t.getOwner());
        });
        model.addAttribute("myTeams", teams);
        model.addAttribute("currentUser", principal.getName());
        return "team/list";
    }

    @GetMapping("/create")
    public String createForm() {
        return "team/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String name, Principal principal,
                         RedirectAttributes redirectAttributes) {
        try {
            Team team = teamService.createTeam(name, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "team.create.success");
            return "redirect:/team/" + team.getId();
        } catch (DuplicateTeamNameException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "team.error.duplicate");
            return "redirect:/team";
        }
    }

    @GetMapping("/join")
    public String joinForm() {
        return "team/join";
    }

    @PostMapping("/join")
    public String join(@RequestParam String inviteCode, Principal principal,
                       RedirectAttributes redirectAttributes) {
        try {
            Team team = teamService.joinTeam(inviteCode, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "team.join.success");
            return "redirect:/team/" + team.getId();
        } catch (TeamNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "team.error.invite");
            return "redirect:/team";
        } catch (TeamJoinException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/team";
        }
    }

    @PostMapping("/{id}/settings")
    public String updateSettings(@PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean inviteEnabled,
                                 @RequestParam(defaultValue = "0") int maxMembers,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        teamService.updateSettings(id, inviteEnabled, maxMembers, user);
        redirectAttributes.addFlashAttribute("successMessage", "team.settings.saved");
        return "redirect:/team/" + id;
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Team team = teamService.findById(id);
        User user = userService.findByUsername(principal.getName());
        if (team.getMembers().stream().noneMatch(m -> m.getId().equals(user.getId()))) {
            throw new org.springframework.security.access.AccessDeniedException("You are not a member of this team.");
        }
        Hibernate.initialize(team.getMembers());
        Hibernate.initialize(team.getOwner());
        model.addAttribute("team", team);
        model.addAttribute("currentUser", principal.getName());

        Map<Long, Integer> memberScores = teamService.computeMemberScores(team);
        int totalScore = memberScores.values().stream().mapToInt(Integer::intValue).sum();
        model.addAttribute("memberScores", memberScores);
        model.addAttribute("totalScore", totalScore);
        model.addAttribute("memberPredictions", teamService.computeMemberPredictions(team));
        return "team/detail";
    }

    @PostMapping("/{id}/remove/{memberId}")
    public String removeMember(@PathVariable Long id, @PathVariable Long memberId,
                               Principal principal, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        teamService.removeMember(id, memberId, user);
        return "redirect:/team/" + id;
    }

    @GetMapping("/{id}/scoreboard")
    @Transactional(readOnly = true)
    public String scoreboard(@PathVariable Long id, Model model, Principal principal) {
        Team team = teamService.findById(id);
        User user = userService.findByUsername(principal.getName());

        if (!team.getMembers().stream().anyMatch(m -> m.getUsername().equals(user.getUsername()))) {
            throw new org.springframework.security.access.AccessDeniedException("You are not a member of this team.");
        }

        Hibernate.initialize(team.getMembers());

        Map<Long, Integer> memberScores = teamService.computeMemberScores(team);
        Map<User, Integer> ranked = team.getMembers().stream()
            .collect(Collectors.toMap(m -> m, m -> memberScores.getOrDefault(m.getId(), 0)))
            .entrySet().stream()
            .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        model.addAttribute("team", team);
        model.addAttribute("scores", ranked);
        return "scoreboard/team";
    }

    @PostMapping("/{id}/regenerate")
    public String regenerateCode(@PathVariable Long id, Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        teamService.regenerateInviteCode(id, user);
        return "redirect:/team/" + id;
    }
}
