package com.avigeya.projectboard.service;

import com.avigeya.projectboard.repository.ProjectMemberRepository;
import com.avigeya.projectboard.repository.StageRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ConnectionsService {

    private final ProjectMemberRepository projectMemberRepository;
    private final StageRepository stageRepository;

    public ConnectionsService(ProjectMemberRepository projectMemberRepository, StageRepository stageRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.stageRepository = stageRepository;
    }

    public List<Object> getAllConnections() {
        // The original implementation was broken, referencing non-existent classes.
        // In the future, this can be implemented to return actual connection data.
        // For now, returning an empty list to match the controller's behavior and fix errors.
        return Collections.emptyList();
    }
}
