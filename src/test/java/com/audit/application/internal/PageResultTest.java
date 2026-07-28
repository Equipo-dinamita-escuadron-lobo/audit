package com.audit.application.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.audit.application.internal.query.PageResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    @DisplayName("constructor - debe crear resultado paginado inmutable")
    void constructor_shouldCreateImmutablePageResult() {
        List<String> content = new ArrayList<>();
        content.add("A");
        content.add("B");

        PageResult<String> result = new PageResult<>(content, 2);

        content.add("C");

        assertAll(
                () -> assertEquals(2, result.getContent().size()),
                () -> assertEquals(2, result.getTotalElements()),
                () -> assertThrows(UnsupportedOperationException.class, () -> result.getContent().add("D")));
    }
}
