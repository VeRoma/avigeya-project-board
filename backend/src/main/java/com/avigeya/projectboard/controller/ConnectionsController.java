package com.avigeya.projectboard.controller;

import com.avigeya.projectboard.dto.ConnectionsDto;
import com.avigeya.projectboard.service.ConnectionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/connections")
@CrossOrigin(origins = "*") // Разрешаем CORS, как и в прошлый раз
public class ConnectionsController {

    private static final Logger log = LoggerFactory.getLogger(ConnectionsController.class);
    private final ConnectionsService connectionsService;

    public ConnectionsController(ConnectionsService connectionsService) {
        this.connectionsService = connectionsService;
    }

    @GetMapping("/all")
    public ResponseEntity<ConnectionsDto> getAllConnections() {
        log.info("Received request to fetch all connections.");
        ConnectionsDto connections = connectionsService.getAllConnections();
        log.info("Successfully fetched all connections.");
        return ResponseEntity.ok(connections);
    }
}
