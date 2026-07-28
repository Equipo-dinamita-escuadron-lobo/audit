package com.audit.infrastructure.adpaters.output.jpa.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.audit.application.internal.DocumentSummaryProjection;
import com.audit.application.internal.query.AuditDocumentCriteria;
import com.audit.application.internal.query.AuditDocumentExportCriteria;
import com.audit.application.internal.query.PageResult;
import com.audit.application.internal.query.QueryOptions;
import com.audit.domain.enums.AuditDateType;
import com.audit.domain.enums.DocumentOperationType;
import com.audit.domain.model.AuditDocumentEvent;
import com.audit.domain.model.DocumentData;
import com.audit.infrastructure.adapters.output.jpa.adapter.AuditDocumentEventRepositoryAdapter;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditDocumentEventEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditDocumentEventJpaMapper;
import com.audit.infrastructure.adapters.output.jpa.repository.IAuditDocumentEventRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuditDocumentEventRepositoryAdapterTest {

    private final IAuditDocumentEventRepository repository = mock(IAuditDocumentEventRepository.class);
    private final AuditDocumentEventJpaMapper mapper = new AuditDocumentEventJpaMapper();
    private final EntityManager entityManager = mock(EntityManager.class);

    private final AuditDocumentEventRepositoryAdapter adapter = new AuditDocumentEventRepositoryAdapter(repository,
            mapper, entityManager);

    private AuditDocumentEvent domainEvent() {
        Instant now = Instant.now();

        return AuditDocumentEvent.reconstruct(
                1L,
                "ENT-001",
                "FAC-001",
                "FACTURA",
                "DOC-001",
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                DocumentOperationType.CREATE,
                "TP-001",
                "Cliente prueba",
                "DOCUMENTS",
                now,
                LocalDate.now(),
                DocumentData.of(
                        Map.of("documentCode", "FAC-001"),
                        List.of(Map.of("account", "1105")),
                        Map.of("total", 1000),
                        Map.of("source", "sales")),
                now);
    }

    private AuditDocumentEventEntity entityEvent() {
        Instant now = Instant.now();

        return AuditDocumentEventEntity.builder()
                .id(1L)
                .enterpriseId("ENT-001")
                .documentCode("FAC-001")
                .documentType("FACTURA")
                .documentId("DOC-001")
                .userId("USER-001")
                .userName("Freider")
                .userRoles(List.of("ADMIN"))
                .operationType(DocumentOperationType.CREATE)
                .thirdPartyId("TP-001")
                .thirdPartyName("Cliente prueba")
                .moduleName("DOCUMENTS")
                .operationAt(now)
                .documentDate(LocalDate.now())
                .documentData(Map.of(
                        "header", Map.of("documentCode", "FAC-001"),
                        "details", List.of(Map.of("account", "1105")),
                        "totals", Map.of("total", 1000),
                        "metadata", Map.of("source", "sales")))
                .createdAt(now)
                .build();
    }

    @Test
    @DisplayName("save - debe guardar evento documental y retornar dominio")
    void save_validEvent_shouldPersistAndReturnDomain() {
        when(repository.save(any(AuditDocumentEventEntity.class)))
                .thenReturn(entityEvent());

        AuditDocumentEvent result = adapter.save(domainEvent());

        assertAll(
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals("ENT-001", result.getEnterpriseId()),
                () -> assertEquals("FAC-001", result.getDocumentCode()),
                () -> assertEquals("FACTURA", result.getDocumentType()));

        verify(repository).save(any(AuditDocumentEventEntity.class));
    }

    @Test
    @DisplayName("findById - si existe debe retornar dominio")
    void findById_existingEvent_shouldReturnDomain() {
        when(repository.findById(1L)).thenReturn(Optional.of(entityEvent()));

        Optional<AuditDocumentEvent> result = adapter.findById(1L);

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals("FAC-001", result.get().getDocumentCode()));
    }

    @Test
    @DisplayName("findById - si no existe debe retornar vacío")
    void findById_notFound_shouldReturnEmpty() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<AuditDocumentEvent> result = adapter.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findEventsByDocumentCode - debe consultar eventos y mapearlos a dominio")
    void findEventsByDocumentCode_validData_shouldReturnEvents() {
        when(repository.findByEnterpriseIdAndDocumentCodeOrderByOperationAtAsc("ENT-001", "FAC-001"))
                .thenReturn(List.of(entityEvent()));

        List<AuditDocumentEvent> result = adapter.findEventsByDocumentCode("ENT-001", "FAC-001");

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("ENT-001", result.get(0).getEnterpriseId()),
                () -> assertEquals("FAC-001", result.get(0).getDocumentCode()));
    }

    @Test
    @DisplayName("findDocumentSummaries - debe aplicar filtros, contar, consultar y mapear proyecciones")
    void findDocumentSummaries_validCriteria_shouldReturnPageResult() {
        Query countQuery = mock(Query.class);
        Query dataQuery = mock(Query.class);

        when(entityManager.createNativeQuery(anyString()))
                .thenReturn(countQuery)
                .thenReturn(dataQuery);

        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(dataQuery.setParameter(anyString(), any())).thenReturn(dataQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        List<Object[]> rows = List.of(new Object[][] {
                { "FAC-001", "FACTURA", "Cliente prueba",
                        Date.valueOf(LocalDate.of(2026, 6, 1)), "Freider", "Admin",
                        Timestamp.from(Instant.parse("2026-06-01T10:00:00Z")) }
        });

        when(dataQuery.getResultList()).thenReturn(rows);

        AuditDocumentCriteria criteria = AuditDocumentCriteria.create(
                Instant.now().minus(7, ChronoUnit.DAYS),
                Instant.now(),
                AuditDateType.DOCUMENT_DATE,
                "FACTURA",
                "FAC",
                "ENT-001",
                "Cliente",
                "Freider");

        QueryOptions options = QueryOptions.builder()
                .page(0)
                .size(20)
                .sortField("documentDate")
                .sortDirection("DESC")
                .build();

        PageResult<DocumentSummaryProjection> result = adapter.findDocumentSummaries(criteria, options);

        assertAll(
                () -> assertEquals(1L, result.getTotalElements()),
                () -> assertEquals(1, result.getContent().size()),
                () -> assertEquals("FAC-001", result.getContent().get(0).getDocumentCode()));

        verify(countQuery, atLeastOnce()).setParameter(eq("enterpriseId"), eq("ENT-001"));
        verify(dataQuery, atLeastOnce()).setParameter(eq("size"), eq(20));
        verify(dataQuery, atLeastOnce()).setParameter(eq("offset"), eq(0));
    }

    @Test
    @DisplayName("findAllDocumentSummariesForExport - debe consultar y mapear resumen documental")
    void findAllDocumentSummariesForExport_validCriteria_shouldReturnSummaries() {
        Query query = mock(Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> rows = List.of(new Object[][] {
                { "FAC-001", "FACTURA", "Cliente prueba",
                        LocalDate.of(2026, 6, 1), "Freider", "Admin",
                        Instant.parse("2026-06-01T10:00:00Z") }
        });

        when(query.getResultList()).thenReturn(rows);

        AuditDocumentCriteria criteria = AuditDocumentCriteria.create(
                null, null, null, null, null, "ENT-001", null, null);

        List<DocumentSummaryProjection> result = adapter.findAllDocumentSummariesForExport(criteria);

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("FAC-001", result.get(0).getDocumentCode()),
                () -> assertEquals("FACTURA", result.get(0).getDocumentType()));
    }

    @Test
    @DisplayName("findAllEventsForExport - debe construir consulta con filtros y mapear entidades")
    void findAllEventsForExport_validCriteria_shouldReturnEvents() {
        Query query = mock(Query.class);

        when(entityManager.createNativeQuery(anyString(), eq(AuditDocumentEventEntity.class)))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entityEvent()));

        AuditDocumentExportCriteria criteria = AuditDocumentExportCriteria.create(
                "ENT-001",
                Instant.now().minus(7, ChronoUnit.DAYS),
                Instant.now(),
                AuditDateType.OPERATION_DATE,
                "FAC",
                "FACTURA",
                "Cliente",
                DocumentOperationType.CREATE,
                "Freider");

        List<AuditDocumentEvent> result = adapter.findAllEventsForExport(criteria);

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("FAC-001", result.get(0).getDocumentCode()),
                () -> assertEquals(DocumentOperationType.CREATE, result.get(0).getOperationType()));

        verify(query).setParameter(eq("enterpriseId"), eq("ENT-001"));
        verify(query).setParameter(eq("documentType"), eq("FACTURA"));
        verify(query).setParameter(eq("operationType"), eq("CREATE"));
    }

    @Test
    @DisplayName("countDistinctDocuments - debe retornar conteo de documentos distintos")
    void countDistinctDocuments_validCriteria_shouldReturnCount() {
        Query query = mock(Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(3L);

        AuditDocumentCriteria criteria = AuditDocumentCriteria.create(
                Instant.now().minus(7, ChronoUnit.DAYS),
                Instant.now(),
                AuditDateType.OPERATION_DATE,
                "FACTURA",
                "FAC",
                "ENT-001",
                "Cliente",
                "Freider");

        long result = adapter.countDistinctDocuments(criteria);

        assertEquals(3L, result);

        verify(query).setParameter(eq("enterpriseId"), eq("ENT-001"));
    }

    @Test
    @DisplayName("findDocumentSummaries - tipo de fecha DOCUMENT_DATE debe convertir Instant a LocalDate")
    void findDocumentSummaries_documentDateFilter_shouldConvertInstantToLocalDate() {
        Query countQuery = mock(Query.class);
        Query dataQuery = mock(Query.class);

        when(entityManager.createNativeQuery(anyString()))
                .thenReturn(countQuery)
                .thenReturn(dataQuery);

        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(dataQuery.setParameter(anyString(), any())).thenReturn(dataQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);
        when(dataQuery.getResultList()).thenReturn(List.of());

        Instant from = Instant.parse("2026-06-01T05:00:00Z");
        Instant to = Instant.parse("2026-06-02T05:00:00Z");

        AuditDocumentCriteria criteria = AuditDocumentCriteria.create(
                from, to, AuditDateType.DOCUMENT_DATE, null, null, "ENT-001", null, null);

        QueryOptions options = QueryOptions.builder()
                .page(0).size(10).sortField("lastModifiedAt").sortDirection("ASC").build();

        adapter.findDocumentSummaries(criteria, options);

        verify(dataQuery, atLeastOnce()).setParameter(eq("dateFrom"), eq(LocalDate.of(2026, 6, 1)));
        verify(dataQuery, atLeastOnce()).setParameter(eq("dateTo"), eq(LocalDate.of(2026, 6, 2)));
    }

    @Test
    @DisplayName("findAllDocumentSummariesForExport - tipo de fecha no soportado debe lanzar IllegalArgumentException")
    void findAllDocumentSummariesForExport_unsupportedDateType_shouldThrowException() {
        Query query = mock(Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> rows = List.of(new Object[][] {
                { "FAC-001", "FACTURA", "Cliente prueba",
                        12345,
                        "Freider", "Admin", Instant.now() }
        });

        when(query.getResultList()).thenReturn(rows);

        AuditDocumentCriteria criteria = AuditDocumentCriteria.create(
                null, null, null, null, null, "ENT-001", null, null);

        assertThrows(IllegalArgumentException.class,
                () -> adapter.findAllDocumentSummariesForExport(criteria));
    }
}
