package com.worldcup.service;

import com.worldcup.domain.Prediction;
import com.worldcup.domain.Team;
import com.worldcup.domain.User;
import com.worldcup.exception.DuplicateTeamNameException;
import com.worldcup.exception.TeamJoinException;
import com.worldcup.exception.TeamNotFoundException;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.repository.TeamRepository;
import com.worldcup.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TeamService {

    private static final int INVITE_CODE_LENGTH = 8;
    private static final int INVITE_CODE_MAX_RETRIES = 5;

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PredictionRepository predictionRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository,
                       PredictionRepository predictionRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.predictionRepository = predictionRepository;
    }

    public String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "")
            .substring(0, INVITE_CODE_LENGTH).toUpperCase();
    }

    @Transactional
    public Team createTeam(String name, String username) {
        if (teamRepository.findByName(name).isPresent()) {
            throw new DuplicateTeamNameException("Team name '" + name + "' is already taken.");
        }
        User owner = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Team team = new Team();
        team.setName(name);
        team.setOwner(owner);
        team.getMembers().add(owner);
        saveWithFreshInviteCode(team);
        log.info("Team '{}' created by {}", name, username);
        return team;
    }

    @Transactional
    public Team joinTeam(String inviteCode, String username) {
        Team team = teamRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new TeamNotFoundException("No team found with invite code: " + inviteCode));
        if (!team.isInviteEnabled()) {
            throw new TeamJoinException("team.error.invites.disabled");
        }
        if (team.getMaxMembers() > 0 && team.getMembers().size() >= team.getMaxMembers()) {
            throw new TeamJoinException("team.error.full");
        }
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (team.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()))) {
            throw new TeamJoinException("team.error.already.member");
        }
        team.getMembers().add(user);
        teamRepository.save(team);
        log.info("User {} joined team {}", username, team.getName());
        return team;
    }

    public Team findById(Long id) {
        return teamRepository.findById(id)
            .orElseThrow(() -> new TeamNotFoundException("Team not found: " + id));
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> computeMemberScores(Team team) {
        return team.getMembers().stream()
            .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
            .collect(Collectors.toMap(
                User::getId,
                m -> predictionRepository.getTotalScoreForUser(m),
                (a, b) -> a,
                LinkedHashMap::new));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<Prediction>> computeMemberPredictions(Team team) {
        return team.getMembers().stream()
            .collect(Collectors.toMap(
                User::getId,
                m -> predictionRepository.findByUser(m).stream()
                    .sorted(Comparator.comparing(p -> p.getMatch().getMatchDate()))
                    .collect(Collectors.toList()),
                (a, b) -> a,
                LinkedHashMap::new));
    }

    @Transactional
    public void removeMember(Long teamId, Long memberId, User requester) {
        Team team = findById(teamId);
        if (!team.getOwner().getUsername().equals(requester.getUsername())) {
            throw new AccessDeniedException("Not the team owner.");
        }
        User memberToRemove = team.getMembers().stream()
            .filter(m -> m.getId().equals(memberId))
            .findFirst()
            .orElseThrow(() -> new TeamNotFoundException("Member not found: " + memberId));
        if (memberToRemove.getId().equals(team.getOwner().getId())) {
            throw new AccessDeniedException("Cannot remove the team owner.");
        }
        team.getMembers().remove(memberToRemove);
        teamRepository.save(team);
        log.info("User {} removed from team {} by {}", memberId, teamId, requester.getUsername());
    }

    @Transactional
    public void regenerateInviteCode(Long teamId, User requester) {
        Team team = findById(teamId);
        if (!team.getOwner().getUsername().equals(requester.getUsername())) {
            throw new AccessDeniedException("Not the team owner.");
        }
        saveWithFreshInviteCode(team);
        log.info("Invite code regenerated for team {}", teamId);
    }

    @Transactional
    public void updateSettings(Long teamId, boolean inviteEnabled, int maxMembers, User requester) {
        Team team = findById(teamId);
        if (!team.getOwner().getUsername().equals(requester.getUsername())) {
            throw new AccessDeniedException("Not the team owner.");
        }
        team.setInviteEnabled(inviteEnabled);
        team.setMaxMembers(Math.max(0, maxMembers));
        teamRepository.save(team);
        log.debug("Settings updated for team {} by {}: inviteEnabled={} maxMembers={}", teamId, requester.getUsername(), inviteEnabled, maxMembers);
    }

    private void saveWithFreshInviteCode(Team team) {
        for (int attempt = 1; attempt <= INVITE_CODE_MAX_RETRIES; attempt++) {
            String candidate = generateInviteCode();
            if (!teamRepository.existsByInviteCode(candidate)) {
                team.setInviteCode(candidate);
                teamRepository.save(team);
                return;
            }
            log.warn("Invite code collision on attempt {}, retrying", attempt);
        }
        throw new DataIntegrityViolationException("Could not generate unique invite code after "
            + INVITE_CODE_MAX_RETRIES + " attempts");
    }
}
