package com.audit.domain.enums;

public enum UserRole {
    ADMINISTRADOR("Administrador"),
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
