package org.eclipse.slm.self_description_service.common.parent.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.self_description_service.common.model.SystemVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/variables")
public abstract class VariablesRestController {

    private final SystemVariableHandler systemVariableHandler;

    public VariablesRestController(SystemVariableHandler systemVariableHandler) {
        this.systemVariableHandler = systemVariableHandler;
    }

    @RequestMapping(value = "/system", method = RequestMethod.GET)
    @Operation(summary = "Get system variables")
    public ResponseEntity<List<SystemVariable>> getSystemVariables() {
        var systemVariables = this.systemVariableHandler.getSystemVariablesWithValue();

        return ResponseEntity.ok(systemVariables);
    }

}
