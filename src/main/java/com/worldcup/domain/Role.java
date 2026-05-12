package com.worldcup.domain;

public enum Role {
    USER, ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}