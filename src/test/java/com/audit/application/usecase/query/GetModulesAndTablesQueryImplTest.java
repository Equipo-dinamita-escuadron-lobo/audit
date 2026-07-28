package com.audit.application.usecase.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.GetModulesTablesRequest;
import com.audit.application.dto.response.ModuleTableResponse;
import com.audit.application.port.output.AuditOperationQueryPort;
import com.audit.application.usecases.queries.GetModulesAndTablesQueryImpl;
import com.audit.domain.model.ModuleTable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetModulesAndTablesQueryImplTest {

    @Mock
    private AuditOperationQueryPort queryPort;

    @InjectMocks
    private GetModulesAndTablesQueryImpl useCase;

    @Test
    @DisplayName("execute - debe retornar módulos y tablas distintas")
    void execute_validRequest_shouldReturnModulesAndTables() {
        when(queryPort.findDistinctModulesAndTables("ENT-001"))
                .thenReturn(List.of(
                        new ModuleTable("CONFIGURATION", "cost_centers"),
                        new ModuleTable("DOCUMENTS", "invoices")));

        GetModulesTablesRequest request = new GetModulesTablesRequest("ENT-001");

        List<ModuleTableResponse> response = useCase.execute(request);

        assertAll(
                () -> assertEquals(2, response.size()),
                () -> assertEquals("CONFIGURATION", response.get(0).moduleName()),
                () -> assertEquals("cost_centers", response.get(0).affectedTable()),
                () -> assertEquals("DOCUMENTS", response.get(1).moduleName()),
                () -> assertEquals("invoices", response.get(1).affectedTable()));

        verify(queryPort).findDistinctModulesAndTables("ENT-001");
    }
}