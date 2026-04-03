package com.worldcup.service;

import com.worldcup.domain.Team;
import com.worldcup.domain.User;
import com.worldcup.exception.DuplicateTeamNameException;
import com.worldcup.exception.TeamJoinException;
import com.worldcup.exception.TeamNotFoundException;
import com.worldcup.repository.TeamRepository;
import com.worldcup.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private static final int INVITE_CODE_LENGTH = 8;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
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
        team.setInviteCode(generateInviteCode());
        team.setOwner(owner);
        team.getMembers().add(owner);
        teamRepository.save(team);
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
        team.setInviteCode(generateInviteCode());
        teamRepository.save(team);
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
        log.info("Settings updated for team {} by {}: inviteEnabled={} maxMembers={}", teamId, requester.getUsername(), inviteEnabled, maxMembers);
    }
}
