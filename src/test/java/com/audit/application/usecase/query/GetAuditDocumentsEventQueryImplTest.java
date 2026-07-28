package com.audit.application.usecase.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.GetDocumentsEventsRequest;
import com.audit.application.dto.response.DocumentEventAuditResponse;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.internal.DocumentSummaryProjection;
import com.audit.application.internal.query.PageResult;
import com.audit.application.port.output.AuditDocumentEventQueryPort;
import com.audit.application.usecases.queries.GetAuditDocumentsEventQueryImpl;
import com.audit.domain.enums.AuditDateType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAuditDocumentsEventQueryImplTest {

        @Mock
        private AuditDocumentEventQueryPort queryPort;

        @InjectMocks
        private GetAuditDocumentsEventQueryImpl useCase;

        @Test
        @DisplayName("execute - debe consultar documentos y retornar respuesta paginada")
        void execute_validRequest_shouldReturnPageResponse() {
                Instant modifiedAt = Instant.now();

                DocumentSummaryProjection projection = DocumentSummaryProjection.of(
                                "FAC-001",
                                "FACTURA",
                                "Cliente prueba",
                                LocalDate.now(),
                                "Freider",
                                "Admin",
                                modifiedAt);

                when(queryPort.findDocumentSummaries(any(), any()))
                                .thenReturn(new PageResult<>(List.of(projection), 1));

                GetDocumentsEventsRequest request = GetDocumentsEventsRequest.builder()
                                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                                .dateTo(Instant.now())
                                .dateType(AuditDateType.DOCUMENT_DATE)
                                .documentType("FACTURA")
                                .documentCode("FAC-001")
                                .enterpriseId("ENT-001")
                                .thirdPartyName("Cliente")
                                .createdBy("Freider")
                                .page(0)
                                .size(20)
                                .sortField("documentDate")
                                .sortDirection("DESC")
                                .build();

                PageResponse<DocumentEventAuditResponse> response = useCase.execute(request);

                assertAll(
                                () -> assertEquals(1, response.getData().size()),
                                () -> assertEquals(1L, response.getTotalElements()),
                                () -> assertEquals(1, response.getTotalPages()),
                                () -> assertEquals("FAC-001", response.getData().get(0).getDocumentCode()),
                                () -> assertEquals("FACTURA", response.getData().get(0).getDocumentType()),
                                () -> assertEquals("Cliente prueba", response.getData().get(0).getThirdPartyName()),
                                () -> assertEquals("Freider", response.getData().get(0).getCreatedBy()),
                                () -> assertEquals("Admin", response.getData().get(0).getLastModifiedBy()),
                                () -> assertEquals(modifiedAt, response.getData().get(0).getLastModifiedAt()));

                verify(queryPort).findDocumentSummaries(any(), any());
        }

        @Test
        @DisplayName("execute - página vacía debe retornar totalPages en 0")
        void execute_emptyResult_shouldReturnZeroPages() {
                when(queryPort.findDocumentSummaries(any(), any()))
                                .thenReturn(new PageResult<>(List.of(), 0));

                GetDocumentsEventsRequest request = GetDocumentsEventsRequest.builder()
                                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                                .dateTo(Instant.now())
                                .page(0)
                                .size(20)
                                .build();

                PageResponse<DocumentEventAuditResponse> response = useCase.execute(request);

                assertAll(
                                () -> assertTrue(response.getData().isEmpty()),
                                () -> assertEquals(0L, response.getTotalElements()),
                                () -> assertEquals(0, response.getTotalPages()),
                                () -> assertFalse(response.isHasNext()),
                                () -> assertFalse(response.isHasPrevious()));
        }

        @Test
        @DisplayName("execute - debe marcar hasNext y hasPrevious cuando existen más páginas")
        void execute_multiplePages_shouldSetHasNextAndHasPrevious() {

                when(queryPort.findDocumentSummaries(any(), any()))
                                .thenReturn(new PageResult<>(List.of(), 50));

                GetDocumentsEventsRequest request = GetDocumentsEventsRequest.builder()
                                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                                .dateTo(Instant.now())
                                .page(1)
                                .size(20)
                                .build();

                PageResponse<DocumentEventAuditResponse> response = useCase.execute(request);

                assertAll(
                                () -> assertEquals(3, response.getTotalPages()),
                                () -> assertTrue(response.isHasNext()),
                                () -> assertTrue(response.isHasPrevious()));
        }
}
