package com.worldcup.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String inviteCode;

    @Column(nullable = false)
    private boolean inviteEnabled = true;

    @Column(nullable = false)
    private int maxMembers = 0;

    @ManyToOne
    private User owner;

    @ManyToMany
    @JoinTable(name = "team_members",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public boolean isInviteEnabled() { return inviteEnabled; }
    public void setInviteEnabled(boolean inviteEnabled) { this.inviteEnabled = inviteEnabled; }
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }
    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }
}
