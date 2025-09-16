package com.avigeya.projectboard.service;

import com.avigeya.projectboard.domain.User;
import com.avigeya.projectboard.dto.AppDataDto;
import com.avigeya.projectboard.exception.ResourceNotFoundException;
import com.avigeya.projectboard.repository.ProjectRepository;
import com.avigeya.projectboard.repository.StageRepository;
import com.avigeya.projectboard.repository.StatusRepository;
import com.avigeya.projectboard.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AppDataServiceImpl implements AppDataService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final StatusRepository statusRepository;
    private final StageRepository stageRepository;

    public AppDataServiceImpl(UserRepository userRepository, ProjectRepository projectRepository,
            StatusRepository statusRepository, StageRepository stageRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.statusRepository = statusRepository;
        this.stageRepository = stageRepository;
    }

    @Override
    public AppDataDto getAppData(Long userId) {
        // Находим пользователя или выбрасываем исключение
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // TODO: Реализовать логику получения и фильтрации проектов/задач
        // TODO: Реализовать маппинг из Entity в DTO

        // Пока возвращаем "заглушку" с данными о пользователе и пустыми списками
        return AppDataDto.builder()
                .currentUserId(currentUser.getId())
                .userName(currentUser.getName())
                .userRole(currentUser.getRole())
                .projects(Collections.emptyList())
                .allProjects(Collections.emptyList())
                .allUsers(Collections.emptyList())
                .allStatuses(Collections.emptyList())
                .allStages(Collections.emptyList())
                .build();
    }
}