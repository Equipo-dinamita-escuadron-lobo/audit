package com.audit.domain.model;

import java.util.Map;
import java.util.Collections;

import lombok.Getter;

@Getter
public class OperationData {

    private final Map<String, Object> entity;
    private final Map<String, Object> context;
    private final Map<String, FieldChange> changes;

    private OperationData(Map<String, Object> entity, Map<String, Object> context, Map<String, FieldChange> changes) {
        this.entity = entity != null ? Map.copyOf(entity) : Collections.emptyMap();
        this.context = context != null ? Map.copyOf(context) : Collections.emptyMap();
        this.changes = changes != null ? Map.copyOf(changes) : Collections.emptyMap();
    }

    public static OperationData forCreate(Map<String, Object> entity) {
        if (entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("Entity data cannot be null or empty for CREATE operation");
        }
        return new OperationData(entity, null, null);
    }

    public static OperationData forUpdate(Map<String, FieldChange> changes) {
        if (changes == null || changes.isEmpty()) {
            throw new IllegalArgumentException("Changes cannot be null or empty for UPDATE operation");
        }
        return new OperationData(null, null, changes);
    }

    public static OperationData forDelete(Map<String, Object> entity) {
        if (entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("Entity data cannot be null or empty for DELETE operation");
        }
        return new OperationData(entity, null, null);
    }

    public static OperationData forUpdate(Map<String, Object> context, Map<String, FieldChange> changes) {
        if (changes == null || changes.isEmpty())
            throw new IllegalArgumentException("Changes cannot be null or empty for UPDATE operation");
        return new OperationData(null, context, changes);
    }

    @Getter
    public static class FieldChange {
        private final Object before;
        private final Object after;

        private FieldChange(Object before, Object after) {
            this.before = before;
            this.after = after;
        }

        public static FieldChange of(Object before, Object after) {
            return new FieldChange(before, after);
        }
    }
}
