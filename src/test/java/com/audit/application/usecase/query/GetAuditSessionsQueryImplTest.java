package com.audit.application.usecase.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.application.dto.request.GetSessionsRequest;
import com.audit.application.dto.response.PageResponse;
import com.audit.application.dto.response.SessionAuditResponse;
import com.audit.application.internal.CombinedSession;
import com.audit.application.internal.query.PageResult;
import com.audit.application.port.output.AuditSessionQueryPort;
import com.audit.application.usecases.queries.GetAuditSessionsQueryImpl;
import com.audit.domain.enums.UserAction;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAuditSessionsQueryImplTest {

        @Mock
        private AuditSessionQueryPort queryPort;

        @InjectMocks
        private GetAuditSessionsQueryImpl useCase;

        @Test
        @DisplayName("execute - debe consultar sesiones combinadas y retornar respuesta paginada")
        void execute_validRequest_shouldReturnPageResponse() {
                Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
                Instant to = Instant.now();
                Instant login = Instant.now().minus(1, ChronoUnit.HOURS);
                Instant logout = Instant.now();

                CombinedSession session = CombinedSession.of(
                                "SESSION-001",
                                "Freider",
                                List.of("ADMIN"),
                                login,
                                logout);

                when(queryPort.findCombinedSessions(any(), any()))
                                .thenReturn(new PageResult<>(List.of(session), 1));

                GetSessionsRequest request = GetSessionsRequest.builder()
                                .dateFrom(from)
                                .dateTo(to)
                                .userName("Freider")
                                .userRole("ADMIN")
                                .action(UserAction.LOGIN)
                                .page(0)
                                .size(20)
                                .sortField("actionAt")
                                .sortDirection("DESC")
                                .build();

                PageResponse<SessionAuditResponse> response = useCase.execute(request);

                assertAll(
                                () -> assertEquals(1, response.getData().size()),
                                () -> assertEquals(1L, response.getTotalElements()),
                                () -> assertEquals(1, response.getTotalPages()),
                                () -> assertEquals(0, response.getCurrentPage()),
                                () -> assertEquals(20, response.getPageSize()),
                                () -> assertFalse(response.isHasNext()),
                                () -> assertFalse(response.isHasPrevious()),
                                () -> assertEquals("SESSION-001", response.getData().get(0).getSessionId()),
                                () -> assertEquals("Freider", response.getData().get(0).getUserName()),
                                () -> assertEquals(List.of("ADMIN"), response.getData().get(0).getUserRole()),
                                () -> assertEquals(login, response.getData().get(0).getLoginTime()),
                                () -> assertEquals(logout, response.getData().get(0).getLogoutTime()));

                verify(queryPort).findCombinedSessions(any(), any());
        }

        @Test
        @DisplayName("execute - página vacía debe retornar totalPages en 0")
        void execute_emptyResult_shouldReturnZeroPages() {
                when(queryPort.findCombinedSessions(any(), any()))
                                .thenReturn(new PageResult<>(List.of(), 0));

                GetSessionsRequest request = GetSessionsRequest.builder()
                                .dateFrom(Instant.now().minus(1, ChronoUnit.DAYS))
                                .dateTo(Instant.now())
                                .page(0)
                                .size(20)
                                .sortField("actionAt")
                                .sortDirection("DESC")
                                .build();

                PageResponse<SessionAuditResponse> response = useCase.execute(request);

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

                Instant login = Instant.now().minus(1, ChronoUnit.HOURS);
                Instant logout = Instant.now();

                CombinedSession session = CombinedSession.of(
                                "SESSION-001",
                                "Freider",
                                List.of("ADMIN"),
                                login,
                                logout);

                when(queryPort.findCombinedSessions(any(), any()))
                                .thenReturn(new PageResult<>(List.of(session), 50));

                GetSessionsRequest request = GetSessionsRequest.builder()
                                .dateFrom(Instant.now().minus(7, ChronoUnit.DAYS))
                                .dateTo(Instant.now())
                                .page(1)
                                .size(20)
                                .sortField("actionAt")
                                .sortDirection("DESC")
                                .build();

                PageResponse<SessionAuditResponse> response = useCase.execute(request);

                assertAll(
                                () -> assertEquals(3, response.getTotalPages()),
                                () -> assertTrue(response.isHasNext()),
                                () -> assertTrue(response.isHasPrevious()));
        }
}
