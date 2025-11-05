package com.audit.domain.model;

import java.util.List;

public class PageResult<T> {
    private final List<T> content;
    private final long totalElements;

    public PageResult(List<T> content, long totalElements) {
        this.content = content;
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
    }

    public long getTotalElements() {
        return totalElements;
    }
}
