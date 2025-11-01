package com.audit.domain.enums;

public enum UserRole {
    ADMIN("Administrador"),
    PROFESOR("Profesor"),
    ESTUDIANTE("Estudiante");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
