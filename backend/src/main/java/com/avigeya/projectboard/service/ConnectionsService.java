package com.avigeya.projectboard.service;

import com.avigeya.projectboard.dto.ConnectionsDto;
import com.avigeya.projectboard.model.ProjectMember;
import com.avigeya.projectboard.model.ProjectStage;
import com.avigeya.projectboard.repository.ProjectMemberRepository;
import com.avigeya.projectboard.repository.ProjectStageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionsService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectStageRepository projectStageRepository;

    public ConnectionsService(ProjectMemberRepository projectMemberRepository, ProjectStageRepository projectStageRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectStageRepository = projectStageRepository;
    }

    public ConnectionsDto getAllConnections() {
        List<ProjectMember> projectMembers = projectMemberRepository.findAll();
        List<ProjectStage> projectStages = projectStageRepository.findAll();

        // В будущем здесь можно добавить и другие связи
        return new ConnectionsDto(projectMembers, projectStages);
    }
}
