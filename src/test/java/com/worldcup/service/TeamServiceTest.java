package com.worldcup.service;

import com.worldcup.domain.Team;
import com.worldcup.domain.User;
import com.worldcup.exception.DuplicateTeamNameException;
import com.worldcup.exception.TeamJoinException;
import com.worldcup.exception.TeamNotFoundException;
import com.worldcup.repository.TeamRepository;
import com.worldcup.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock TeamRepository teamRepository;
    @Mock UserRepository userRepository;
    @InjectMocks TeamService teamService;

    private User owner;
    private User member;
    private Team team;

    @BeforeEach
    void setUp() {
        owner = new User();
        ReflectionTestUtils.setField(owner, "id", 1L);
        owner.setUsername("alice");

        member = new User();
        ReflectionTestUtils.setField(member, "id", 2L);
        member.setUsername("bob");

        team = new Team();
        team.setId(10L);
        team.setName("Test Team");
        team.setInviteCode("INVITE01");
        team.setInviteEnabled(true);
        team.setMaxMembers(0);
        team.setOwner(owner);
        team.setMembers(new HashSet<>());
        team.getMembers().add(owner);
    }

    @Test
    void createTeam_whenNoDuplicate_shouldSaveAndReturn() {
        when(teamRepository.findByName("New Team")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(owner));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team result = teamService.createTeam("New Team", "alice");

        assertThat(result.getName()).isEqualTo("New Team");
        assertThat(result.getOwner()).isEqualTo(owner);
        assertThat(result.getMembers()).contains(owner);
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void createTeam_whenDuplicateName_shouldThrow() {
        when(teamRepository.findByName("Test Team")).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.createTeam("Test Team", "alice"))
            .isInstanceOf(DuplicateTeamNameException.class);
        verify(teamRepository, never()).save(any());
    }

    @Test
    void createTeam_whenUserNotFound_shouldThrow() {
        when(teamRepository.findByName("New Team")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.createTeam("New Team", "ghost"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void joinTeam_whenValid_shouldAddMemberAndSave() {
        when(teamRepository.findByInviteCode("INVITE01")).thenReturn(Optional.of(team));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(member));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team result = teamService.joinTeam("INVITE01", "bob");

        assertThat(result.getMembers()).contains(member);
        verify(teamRepository).save(team);
    }

    @Test
    void joinTeam_whenInvalidCode_shouldThrow() {
        when(teamRepository.findByInviteCode("BADCODE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.joinTeam("BADCODE", "bob"))
            .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void joinTeam_whenInvitesDisabled_shouldThrow() {
        team.setInviteEnabled(false);
        when(teamRepository.findByInviteCode("INVITE01")).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.joinTeam("INVITE01", "bob"))
            .isInstanceOf(TeamJoinException.class)
            .hasMessageContaining("invites.disabled");
    }

    @Test
    void joinTeam_whenTeamFull_shouldThrow() {
        team.setMaxMembers(1);
        when(teamRepository.findByInviteCode("INVITE01")).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.joinTeam("INVITE01", "bob"))
            .isInstanceOf(TeamJoinException.class)
            .hasMessageContaining("full");
    }

    @Test
    void joinTeam_whenAlreadyMember_shouldThrow() {
        when(teamRepository.findByInviteCode("INVITE01")).thenReturn(Optional.of(team));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> teamService.joinTeam("INVITE01", "alice"))
            .isInstanceOf(TeamJoinException.class)
            .hasMessageContaining("already.member");
    }

    @Test
    void findById_whenExists_shouldReturn() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        Team result = teamService.findById(10L);
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.findById(99L))
            .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void removeMember_whenOwnerRemovesOther_shouldRemoveAndSave() {
        team.getMembers().add(member);
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        teamService.removeMember(10L, 2L, owner);

        assertThat(team.getMembers()).doesNotContain(member);
        verify(teamRepository).save(team);
    }

    @Test
    void removeMember_whenNonOwnerRequests_shouldThrow() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.removeMember(10L, 1L, member))
            .isInstanceOf(AccessDeniedException.class);
        verify(teamRepository, never()).save(any());
    }

    @Test
    void removeMember_whenOwnerRemovesSelf_shouldThrow() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.removeMember(10L, 1L, owner))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Cannot remove the team owner");
    }

    @Test
    void regenerateInviteCode_whenOwnerRequests_shouldUpdateAndSave() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        teamService.regenerateInviteCode(10L, owner);

        assertThat(team.getInviteCode()).isNotEqualTo("INVITE01");
        verify(teamRepository).save(team);
    }

    @Test
    void regenerateInviteCode_whenNonOwner_shouldThrow() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.regenerateInviteCode(10L, member))
            .isInstanceOf(AccessDeniedException.class);
        verify(teamRepository, never()).save(any());
    }

    @Test
    void updateSettings_whenOwner_shouldPersist() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        teamService.updateSettings(10L, false, 5, owner);

        assertThat(team.isInviteEnabled()).isFalse();
        assertThat(team.getMaxMembers()).isEqualTo(5);
        verify(teamRepository).save(team);
    }

    @Test
    void updateSettings_whenNonOwner_shouldThrow() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.updateSettings(10L, false, 5, member))
            .isInstanceOf(AccessDeniedException.class);
    }
}
