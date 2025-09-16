package com.avigeya.projectboard.controller;

import com.avigeya.projectboard.service.ConnectionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/connections")
public class ConnectionsController {

    private final ConnectionsService connectionsService;

    public ConnectionsController(ConnectionsService connectionsService) {
        this.connectionsService = connectionsService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Object>> getAllConnections() {
        // Логика теперь находится в сервисном слое
        return ResponseEntity.ok(connectionsService.getAllConnections());
    }
}
