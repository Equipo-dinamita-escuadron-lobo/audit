package com.audit.domain.enums;

public enum DocumentOperationType {
    CREATE("Creación"),
    UPDATE("Actualización"),
    DELETE("Eliminación"),
    APPROVE("Aprobación"),
    CANCEL("Anulación");

    private final String description;

    DocumentOperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
