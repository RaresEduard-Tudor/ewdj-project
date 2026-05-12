package com.worldcup.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        return role == null ? null : role.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        String trimmed = dbValue.startsWith("ROLE_") ? dbValue.substring(5) : dbValue;
        return Role.valueOf(trimmed);
    }
}
