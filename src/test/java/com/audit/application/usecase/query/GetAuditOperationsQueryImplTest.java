package com.audit.application.usecase.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.GetOperationsRequest;
import com.audit.application.dto.response.OperationAuditResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.internal.query.PageResult;
import com.audit.application.port.output.AuditOperationQueryPort;
import com.audit.application.usecases.queries.GetAuditOperationsQueryImpl;
import com.audit.domain.enums.OperationType;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.OperationData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAuditOperationsQueryImplTest {

        @Mock
        private AuditOperationQueryPort queryPort;

        @InjectMocks
        private GetAuditOperationsQueryImpl useCase;

        @Test
        @DisplayName("execute - debe consultar operaciones y retornar respuesta paginada")
        void execute_validRequest_shouldReturnPageResponse() {
                Instant operationAt = Instant.now().minus(1, ChronoUnit.HOURS);
                OperationData data = OperationData.forCreate(Map.of("id", 1L));

                AuditOperation operation = AuditOperation.reconstruct(
                                1L,
                                "USER-001",
                                "Freider",
                                List.of("ADMIN"),
                                OperationType.CREATE,
                                operationAt,
                                "CONFIGURATION",
                                "cost_centers",
                                "1",
                                "ENT-001",
                                data,
                                Instant.now());

                when(queryPort.findPageByCriteria(any(), any()))
                                .thenReturn(new PageResult<>(List.of(operation), 1));

                GetOperationsRequest request = GetOperationsRequest.builder()
                                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                                .dateTo(Instant.now())
                                .moduleName("CONFIGURATION")
                                .affectedTable("cost_centers")
                                .userName("Freider")
                                .userRole("ADMIN")
                                .operationType(OperationType.CREATE)
                                .registerId("1")
                                .enterpriseId("ENT-001")
                                .page(0)
                                .size(20)
                                .sortField("operationAt")
                                .sortDirection("DESC")
                                .build();

                PageResponse<OperationAuditResponse> response = useCase.execute(request);

                assertAll(
                                () -> assertEquals(1, response.getData().size()),
                                () -> assertEquals(1L, response.getTotalElements()),
                                () -> assertEquals(1, response.getTotalPages()),
                                () -> assertEquals("Freider", response.getData().get(0).getUserName()),
                                () -> assertEquals(List.of("ADMIN"), response.getData().get(0).getUserRole()),
                                () -> assertEquals("CREATE", response.getData().get(0).getOperationType()),
                                () -> assertEquals("CONFIGURATION", response.getData().get(0).getModuleName()),
                                () -> assertEquals("cost_centers", response.getData().get(0).getAffectedTable()),
                                () -> assertEquals("1", response.getData().get(0).getRegisterId()),
                                () -> assertEquals(data, response.getData().get(0).getDataObject()));

                verify(queryPort).findPageByCriteria(any(), any());
        }

        @Test
        @DisplayName("execute - página vacía debe retornar totalPages en 0")
        void execute_emptyResult_shouldReturnZeroPages() {

                when(queryPort.findPageByCriteria(any(), any()))
                                .thenReturn(new PageResult<>(List.of(), 0));

                GetOperationsRequest request = GetOperationsRequest.builder()
                                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                                .dateTo(Instant.now())
                                .page(0)
                                .size(20)
                                .sortField("operationAt")
                                .sortDirection("DESC")
                                .build();

                PageResponse<OperationAuditResponse> response = useCase.execute(request);

                assertAll(
                                () -> assertTrue(response.getData().isEmpty()),
                                () -> assertEquals(0L, response.getTotalElements()),
                                () -> assertEquals(0, response.getTotalPages()),
                                () -> assertFalse(response.isHasNext()),
                                () -> assertFalse(response.isHasPrevious()));
        }

        @Test
        @DisplayName("execute - prueba hasNext y hasPrevious con varias páginas")
        void execute_multiplePages_shouldSetHasNextAndHasPrevious() {

                when(queryPort.findPageByCriteria(any(), any()))
                                .thenReturn(new PageResult<>(List.of(), 50));

                GetOperationsRequest request = GetOperationsRequest.builder()
                                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                                .dateTo(Instant.now())
                                .page(1)
                                .size(20)
                                .sortField("operationAt")
                                .sortDirection("DESC")
                                .build();

                PageResponse<OperationAuditResponse> response = useCase.execute(request);

                assertAll(
                                () -> assertEquals(3, response.getTotalPages()),
                                () -> assertTrue(response.isHasNext()),
                                () -> assertTrue(response.isHasPrevious()));
        }
}
