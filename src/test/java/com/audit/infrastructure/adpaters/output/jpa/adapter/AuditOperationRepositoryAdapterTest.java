package com.audit.infrastructure.adpaters.output.jpa.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.audit.application.internal.query.AuditOperationCriteria;
import com.audit.application.internal.query.PageResult;
import com.audit.application.internal.query.QueryOptions;
import com.audit.domain.enums.OperationType;
import com.audit.domain.model.AuditOperation;
import com.audit.domain.model.ModuleTable;
import com.audit.domain.model.OperationData;
import com.audit.infrastructure.adapters.output.jpa.adapter.AuditOperationRepositoryAdapter;
import com.audit.infrastructure.adapters.output.jpa.entity.AuditOperationEntity;
import com.audit.infrastructure.adapters.output.jpa.mapper.AuditOperationJpaMapper;
import com.audit.infrastructure.adapters.output.jpa.projection.ModuleTableProjection;
import com.audit.infrastructure.adapters.output.jpa.repository.IAuditOperationRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class AuditOperationRepositoryAdapterTest {

    private final IAuditOperationRepository repository = mock(IAuditOperationRepository.class);
    private final AuditOperationJpaMapper mapper = new AuditOperationJpaMapper();
    private final AuditOperationRepositoryAdapter adapter = new AuditOperationRepositoryAdapter(repository, mapper);

    private AuditOperation domainOperation() {
        Instant now = Instant.now();

        return AuditOperation.reconstruct(
                1L,
                "USER-001",
                "Freider",
                List.of("ADMIN"),
                OperationType.CREATE,
                now,
                "CONFIGURATION",
                "cost_centers",
                "1",
                "ENT-001",
                OperationData.forCreate(Map.of("id", 1L, "name", "Centro")),
                now);
    }

    private AuditOperationEntity entityOperation() {
        Instant now = Instant.now();

        return AuditOperationEntity.builder()
                .id(1L)
                .enterpriseId("ENT-001")
                .userId("USER-001")
                .userName("Freider")
                .userRole(List.of("ADMIN"))
                .operationType(OperationType.CREATE)
                .operationAt(now)
                .moduleName("CONFIGURATION")
                .affectedTable("cost_centers")
                .registerId("1")
                .dataObject(Map.of("entity", Map.of("id", 1L, "name", "Centro")))
                .createdAt(now)
                .build();
    }

    @Test
    @DisplayName("save - debe mapear dominio a entidad, guardar y retornar dominio")
    void save_validOperation_shouldPersistAndReturnDomain() {
        when(repository.save(any(AuditOperationEntity.class)))
                .thenReturn(entityOperation());

        AuditOperation result = adapter.save(domainOperation());

        assertAll(
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals("ENT-001", result.getEnterpriseId()),
                () -> assertEquals("Freider", result.getUserName()),
                () -> assertEquals(OperationType.CREATE, result.getOperationType()),
                () -> assertEquals("cost_centers", result.getAffectedTable()));

        verify(repository).save(any(AuditOperationEntity.class));
    }

    @Test
    @DisplayName("findById - si existe debe retornar dominio")
    void findById_existingOperation_shouldReturnDomain() {
        when(repository.findById(1L)).thenReturn(Optional.of(entityOperation()));

        Optional<AuditOperation> result = adapter.findById(1L);

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(1L, result.get().getId()),
                () -> assertEquals("Freider", result.get().getUserName()));
    }

    @Test
    @DisplayName("findById - si no existe debe retornar vacío")
    void findById_notFound_shouldReturnEmpty() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<AuditOperation> result = adapter.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByRegisterId - si existe debe retornar dominio")
    void findByRegisterId_existingOperation_shouldReturnDomain() {
        when(repository.findByRegisterIdAndAffectedTable("1", "cost_centers"))
                .thenReturn(Optional.of(entityOperation()));

        Optional<AuditOperation> result = adapter.findByRegisterId("1", "cost_centers");

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals("1", result.get().getRegisterId()),
                () -> assertEquals("cost_centers", result.get().getAffectedTable()));
    }

    @Test
    @DisplayName("findPageByCriteria - debe consultar con filtros, paginar y mapear resultados")
    void findPageByCriteria_validCriteria_shouldReturnPageResult() {
        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        when(repository.findByFilters(
                eq("ENT-001"),
                eq(from),
                eq(to),
                eq("CONFIGURATION"),
                eq("cost_centers"),
                eq("Freider"),
                eq("CREATE"),
                eq("ADMIN"),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entityOperation())));

        AuditOperationCriteria criteria = AuditOperationCriteria.create(
                from,
                to,
                "CONFIGURATION",
                "cost_centers",
                "Freider",
                "ADMIN",
                OperationType.CREATE,
                "1",
                "ENT-001");

        QueryOptions options = QueryOptions.builder()
                .page(0)
                .size(20)
                .sortField("operationAt")
                .sortDirection("DESC")
                .build();

        PageResult<AuditOperation> result = adapter.findPageByCriteria(criteria, options);

        assertAll(
                () -> assertEquals(1, result.getContent().size()),
                () -> assertEquals(1L, result.getTotalElements()),
                () -> assertEquals("Freider", result.getContent().get(0).getUserName()),
                () -> assertEquals(OperationType.CREATE, result.getContent().get(0).getOperationType()));
    }

    @Test
    @DisplayName("findPageByCriteria - sort inválido debe usar valores seguros por defecto")
    void findPageByCriteria_invalidSort_shouldUseDefaultSort() {
        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        when(repository.findByFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entityOperation())));

        AuditOperationCriteria criteria = AuditOperationCriteria.create(
                from, to, null, null, null, null, null, null, "ENT-001");

        QueryOptions options = QueryOptions.builder()
                .page(0)
                .size(20)
                .sortField("campo_invalido")
                .sortDirection("BAD")
                .build();

        PageResult<AuditOperation> result = adapter.findPageByCriteria(criteria, options);

        assertEquals(1, result.getContent().size());
        verify(repository).findByFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("countByCriteria - debe retornar totalElements desde findByFilters")
    void countByCriteria_validCriteria_shouldReturnTotalElements() {
        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        Page<AuditOperationEntity> page = mock(Page.class);
        when(page.getTotalElements()).thenReturn(5L);

        when(repository.findByFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        AuditOperationCriteria criteria = AuditOperationCriteria.create(
                from, to, null, null, null, null, null, null, "ENT-001");

        long result = adapter.countByCriteria(criteria);

        assertEquals(5L, result);
        verify(repository).findByFilters(any(), any(), any(), any(), any(), any(), any(), any(),
                argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 1));
    }

    @Test
    @DisplayName("findAllForExport - debe consultar lista y mapear entidades a dominio")
    void findAllForExport_validCriteria_shouldReturnDomainList() {
        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        when(repository.findAllForExport(
                eq("ENT-001"),
                eq(from),
                eq(to),
                eq("CONFIGURATION"),
                eq("cost_centers"),
                eq("Freider"),
                eq("CREATE"),
                eq("ADMIN"))).thenReturn(List.of(entityOperation()));

        AuditOperationCriteria criteria = AuditOperationCriteria.create(
                from,
                to,
                "CONFIGURATION",
                "cost_centers",
                "Freider",
                "ADMIN",
                OperationType.CREATE,
                "1",
                "ENT-001");

        List<AuditOperation> result = adapter.findAllForExport(criteria);

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Freider", result.get(0).getUserName()),
                () -> assertEquals(OperationType.CREATE, result.get(0).getOperationType()));
    }

    @Test
    @DisplayName("findDistinctModulesAndTables - debe mapear proyecciones a ModuleTable")
    void findDistinctModulesAndTables_validEnterprise_shouldReturnModuleTables() {
        ModuleTableProjection projection = mock(ModuleTableProjection.class);
        when(projection.getModuleName()).thenReturn("CONFIGURATION");
        when(projection.getAffectedTable()).thenReturn("cost_centers");

        when(repository.findDistinctModulesAndTables("ENT-001"))
                .thenReturn(List.of(projection));

        List<ModuleTable> result = adapter.findDistinctModulesAndTables("ENT-001");

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("CONFIGURATION", result.get(0).moduleName()),
                () -> assertEquals("cost_centers", result.get(0).affectedTable()));
    }

    @Test
    @DisplayName("findPageByCriteria - userName nulo debe pasar null al repositorio")
    void findPageByCriteria_nullUserName_shouldPassNull() {
        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        when(repository.findByFilters(
                eq("ENT-001"), eq(from), eq(to), eq(null), eq(null),
                isNull(), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        AuditOperationCriteria criteria = AuditOperationCriteria.create(
                from, to, null, null, null, null, null, null, "ENT-001");

        QueryOptions options = QueryOptions.builder().page(0).size(10).build();

        PageResult<AuditOperation> result = adapter.findPageByCriteria(criteria, options);

        assertNotNull(result);
        verify(repository).findByFilters(
                eq("ENT-001"), eq(from), eq(to), eq(null), eq(null),
                isNull(), eq(null), eq(null), any(Pageable.class));
    }

}
