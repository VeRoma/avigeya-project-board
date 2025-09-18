package com.avigeya.projectboard.config;

import com.avigeya.projectboard.domain.*;
import com.avigeya.projectboard.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final Map<Long, User> userMap = new HashMap<>();
    private final Map<Long, Project> projectMap = new HashMap<>();
    private final Map<Long, Status> statusMap = new HashMap<>();
    private final Map<Long, Stage> stageMap = new HashMap<>();
    private final Map<Long, Task> taskMap = new HashMap<>();

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private final StageRepository stageRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskMemberRepository taskMemberRepository;
    private final ProjectStageRepository projectStageRepository;

    public DataInitializer(UserRepository u, ProjectRepository p, TaskRepository t, StatusRepository s,
            StageRepository st, ProjectMemberRepository pm, TaskMemberRepository tm, ProjectStageRepository psr) {
        this.userRepository = u;
        this.projectRepository = p;
        this.taskRepository = t;
        this.statusRepository = s;
        this.stageRepository = st;
        this.projectMemberRepository = pm;
        this.taskMemberRepository = tm;
        this.projectStageRepository = psr;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.warn("База данных уже содержит данные. Инициализация пропущена.");
            return;
        }

        log.info("Начата загрузка реальных данных из CSV...");

        loadUsers();
        loadProjects();
        loadStatuses();
        loadStages();
        loadProjectStages();
        loadTasks();
        loadProjectMembers();
        loadTaskMembers();

        log.info("Загрузка всех данных успешно завершена!");
    }

    private void loadUsers() throws Exception {
        String fileName = "AvigeyaProjectDataBase-Users.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            log.error("Файл {} не найден в ресурсах!", fileName);
            return;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 1 && !columns[0].isEmpty()) {
                    User user = new User();
                    user.setName(columns[1]);
                    if (columns.length > 4)
                        user.setRole(columns[4]);

                    if (columns.length > 3 && !columns[3].isEmpty()) {
                        try {
                            user.setTgUserId(Long.parseLong(columns[3]));
                        } catch (NumberFormatException e) {
                            log.warn("Не удалось прочитать tg_user_id '{}' для пользователя '{}'.", columns[3],
                                    columns[1]);
                        }
                    }

                    if (columns.length > 5 && !columns[5].isEmpty()) {
                        user.setDescription(columns[5]);
                    }

                    User savedUser = userRepository.save(user);
                    long oldId = Long.parseLong(columns[0]);
                    userMap.put(oldId, savedUser);
                }
            }
        }
    }

    private void loadProjects() throws Exception {
        String fileName = "AvigeyaProjectDataBase-Projects.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            log.error("Файл {} не найден в ресурсах!", fileName);
            return;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 1 && !columns[0].isEmpty()) {
                    Project project = new Project();
                    project.setName(columns[1]);
                    if (columns.length > 2 && !columns[2].isEmpty()) {
                        project.setDescription(columns[2]);
                    }
                    Project savedProject = projectRepository.save(project);
                    long oldId = Long.parseLong(columns[0]);
                    projectMap.put(oldId, savedProject);
                }
            }
        }
    }

    private void loadStatuses() throws Exception {
        String fileName = "AvigeyaProjectDataBase-Statuses.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            log.error("Файл {} не найден в ресурсах!", fileName);
            return;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 1 && !columns[0].isEmpty()) {
                    Status status = new Status();
                    status.setName(columns[1]);

                    if (columns.length > 2 && !columns[2].isEmpty()) {
                        status.setIcon(columns[2]);
                    }

                    if (columns.length > 3 && !columns[3].isEmpty()) {
                        try {
                            status.setOrder(Integer.parseInt(columns[3]));
                        } catch (NumberFormatException e) {
                            log.warn("Не удалось прочитать order '{}' для статуса '{}'.", columns[3], columns[1]);
                        }
                    }

                    Status savedStatus = statusRepository.save(status);
                    long oldId = Long.parseLong(columns[0]);
                    statusMap.put(oldId, savedStatus);
                }
            }
        }
    }

    private void loadStages() throws Exception {
        String fileName = "AvigeyaProjectDataBase-Stages.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            log.error("Файл {} не найден в ресурсах!", fileName);
            return;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 1 && !columns[0].isEmpty()) {
                    Stage stage = new Stage();
                    stage.setName(columns[1]);

                    if (columns.length > 2 && !columns[2].isEmpty()) {
                        stage.setDescription(columns[2]);
                    }

                    Stage savedStage = stageRepository.save(stage);
                    long oldId = Long.parseLong(columns[0]);
                    stageMap.put(oldId, savedStage);
                }
            }
        }
    }

    private void loadProjectStages() throws Exception {
        String fileName = "AvigeyaProjectDataBase-ProjectStages.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            log.error("Файл {} не найден в ресурсах!", fileName);
            return;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 3 && !columns[1].isEmpty() && !columns[2].isEmpty()) {
                    try {
                        long projectId = Long.parseLong(columns[1]);
                        long stageId = Long.parseLong(columns[2]);
                        boolean isActive = Boolean.parseBoolean(columns[3]);

                        Project project = projectMap.get(projectId);
                        Stage stage = stageMap.get(stageId);

                        if (project != null && stage != null) {
                            ProjectStage projectStage = new ProjectStage(project, stage, isActive);
                            projectStageRepository.save(projectStage);
                        }
                    } catch (NumberFormatException e) {
                        // Пропускаем
                    }
                }
            }
        }
    }

    private void loadTasks() throws Exception {
        int lineNumber = 1;
        String fileName = "AvigeyaProjectDataBase-Tasks.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            log.error("Файл {} не найден в ресурсах!", fileName);
            return;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] columns = line.split(",");
                if (columns.length < 8)
                    continue;

                try {
                    Task task = new Task();
                    task.setName(columns[1]);

                    if (!columns[2].isEmpty())
                        task.setStage(stageMap.get(Long.parseLong(columns[2])));
                    if (!columns[3].isEmpty())
                        task.setProject(projectMap.get(Long.parseLong(columns[3])));
                    if (!columns[4].isEmpty())
                        task.setCurator(userMap.get(Long.parseLong(columns[4])));
                    if (!columns[5].isEmpty())
                        task.setStatus(statusMap.get(Long.parseLong(columns[5])));
                    if (!columns[7].isEmpty())
                        task.setAuthor(userMap.get(Long.parseLong(columns[7])));

                    if (!columns[6].isEmpty())
                        task.setPriority(Integer.parseInt(columns[6]));

                    if (columns.length > 9 && !columns[9].isEmpty()) {
                        try {
                            task.setStartDate(LocalDate.parse(columns[9]));
                        } catch (DateTimeParseException e) {
                            log.warn("Не удалось прочитать startDate '{}' для задачи '{}'.", columns[9], columns[1]);
                        }
                    }

                    if (columns.length > 10 && !columns[10].isEmpty()) {
                        try {
                            task.setFinishDate(LocalDate.parse(columns[10]));
                        } catch (DateTimeParseException e) {
                            log.warn("Не удалось прочитать finishDate '{}' для задачи '{}'.", columns[10], columns[1]);
                        }
                    }

                    if (columns.length > 11 && !columns[11].isEmpty()) {
                        task.setDeleted(Boolean.parseBoolean(columns[11]));
                    }

                    if (task.getProject() != null && task.getCurator() != null && task.getAuthor() != null) {
                        Task savedTask = taskRepository.save(task);
                        long oldId = Long.parseLong(columns[0]);
                        taskMap.put(oldId, savedTask);
                    } else {
                        log.warn("Пропущена задача '{}' (строка {}) из-за ненайденной связи.", task.getName(),
                                lineNumber);
                    }

                } catch (NumberFormatException e) {
                    log.error("ОШИБКА: Не удалось прочитать число в строке {} в Tasks.csv. Строка пропущена.",
                            lineNumber);
                }
            }
        }
    }

    private void loadProjectMembers() throws Exception {
        String fileName = "AvigeyaProjectDataBase-ProjectMembers.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            log.error("Файл {} не найден в ресурсах!", fileName);
            return;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 2 && !columns[1].isEmpty() && !columns[2].isEmpty()) {
                    try {
                        ProjectMember member = new ProjectMember();
                        member.setProject(projectMap.get(Long.parseLong(columns[1])));
                        member.setUser(userMap.get(Long.parseLong(columns[2])));

                        if (columns.length > 4 && !columns[4].isEmpty()) {
                            member.setIsActive(Boolean.parseBoolean(columns[4])); // ИСПРАВЛЕНО
                        }

                        if (member.getProject() != null && member.getUser() != null) {
                            projectMemberRepository.save(member);
                        }
                    } catch (NumberFormatException e) {
                        /* пропускаем */ }
                }
            }
        }
    }

    private void loadTaskMembers() throws Exception {
        String fileName = "AvigeyaProjectDataBase-TaskMembers.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            log.error("Файл {} не найден в ресурсах!", fileName);
            return;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 2 && !columns[1].isEmpty() && !columns[2].isEmpty()) {
                    try {
                        long taskId = Long.parseLong(columns[1]);
                        long userId = Long.parseLong(columns[2]);

                        Task task = taskMap.get(taskId);
                        User user = userMap.get(userId);

                        if (task != null && user != null) {
                            TaskMember member = new TaskMember();
                            member.setTask(task);
                            member.setUser(user);
                            taskMemberRepository.save(member);
                        }
                    } catch (NumberFormatException e) {
                        /* пропускаем */ }
                }
            }
        }
    }
}
