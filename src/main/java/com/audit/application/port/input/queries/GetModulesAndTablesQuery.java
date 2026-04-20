package com.audit.application.port.input.queries;

import java.util.List;

import com.audit.application.dto.request.GetModulesTablesRequest;
import com.audit.application.dto.response.ModuleTableResponse;

public interface GetModulesAndTablesQuery {
    List<ModuleTableResponse> execute(GetModulesTablesRequest request);
}
