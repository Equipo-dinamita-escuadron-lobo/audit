package com.audit.domain.enums;

public enum DocumentStatus {
    APPROVED("Aprobado"),
    CANCELED("Anulado"),
    INPREPARATION("En elaboración");

    private final String description;

    DocumentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
