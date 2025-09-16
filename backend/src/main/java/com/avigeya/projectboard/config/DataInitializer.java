package com.avigeya.projectboard.config;

import com.avigeya.projectboard.domain.*;
import com.avigeya.projectboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    // Карты для сопоставления старых ID из CSV с новыми объектами из БД
    private final Map<Long, User> userMap = new HashMap<>();
    private final Map<Long, Project> projectMap = new HashMap<>();
    private final Map<Long, Status> statusMap = new HashMap<>();
    private final Map<Long, Stage> stageMap = new HashMap<>();
    private final Map<Long, Task> taskMap = new HashMap<>();

    // Все необходимые репозитории
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private final StageRepository stageRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskMemberRepository taskMemberRepository;

    @Autowired
    public DataInitializer(UserRepository u, ProjectRepository p, TaskRepository t, StatusRepository s, StageRepository st, ProjectMemberRepository pm, TaskMemberRepository tm) {
        this.userRepository = u;
        this.projectRepository = p;
        this.taskRepository = t;
        this.statusRepository = s;
        this.stageRepository = st;
        this.projectMemberRepository = pm;
        this.taskMemberRepository = tm;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            System.out.println("База данных уже содержит данные. Инициализация пропущена.");
            return;
        }

        System.out.println("Начата загрузка реальных данных из CSV...");

        // Правильный порядок загрузки для соблюдения зависимостей
        loadUsers();
        loadProjects();
        loadStatuses();
        loadStages();
        loadTasks(); // Сначала задачи
        loadProjectMembers(); // Потом участники проектов
        loadTaskMembers(); // В самом конце - участники задач

        System.out.println("Загрузка всех данных успешно завершена!");
    }

    private void loadUsers() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("AvigeyaProjectDataBase - Users.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine(); // Пропускаем заголовок
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 4) { // Убедимся, что есть все основные колонки
                    User user = new User();
                    user.setName(columns[1]);
                    user.setRole(columns[4]);

                    // --- ИСПРАВЛЕНИЕ ЗДЕСЬ ---
                    // Читаем tg_user_id из колонки 3
                    if (columns.length > 3 && !columns[3].isEmpty()) {
                        try {
                            user.setTgUserId(Long.parseLong(columns[3]));
                        } catch (NumberFormatException e) {
                            System.out.println("ПРЕДУПРЕЖДЕНИЕ: Не удалось прочитать tg_user_id '" + columns[3] + "' для пользователя '" + columns[1] + "'.");
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
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("AvigeyaProjectDataBase - Projects.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 1) {
                    Project project = new Project();
                    project.setName(columns[1]);
                    if (columns.length > 2) {
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
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("AvigeyaProjectDataBase - Statuses.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 1) {
                    Status status = new Status();
                    status.setName(columns[1]);

                    Status savedStatus = statusRepository.save(status);
                    long oldId = Long.parseLong(columns[0]);
                    statusMap.put(oldId, savedStatus);
                }
            }
        }
    }

    private void loadStages() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("AvigeyaProjectDataBase - Stages.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 1) {
                    Stage stage = new Stage();
                    stage.setName(columns[1]);

                    Stage savedStage = stageRepository.save(stage);
                    long oldId = Long.parseLong(columns[0]);
                    stageMap.put(oldId, savedStage);
                }
            }
        }
    }

    private void loadTasks() throws Exception {
        int lineNumber = 1;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("AvigeyaProjectDataBase - Tasks.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] columns = line.split(",");
                if (columns.length < 8) continue;

                try {
                    Task task = new Task();
                    task.setName(columns[1]);

                    if (!columns[2].isEmpty()) task.setStage(stageMap.get(Long.parseLong(columns[2])));
                    if (!columns[3].isEmpty()) task.setProject(projectMap.get(Long.parseLong(columns[3])));
                    if (!columns[4].isEmpty()) task.setCurator(userMap.get(Long.parseLong(columns[4])));
                    if (!columns[5].isEmpty()) task.setStatus(statusMap.get(Long.parseLong(columns[5])));
                    if (!columns[7].isEmpty()) task.setAuthor(userMap.get(Long.parseLong(columns[7])));
                    if (!columns[6].isEmpty()) task.setPriority(Integer.parseInt(columns[6]));

                    if (task.getProject() != null && task.getCurator() != null && task.getAuthor() != null) {
                        Task savedTask = taskRepository.save(task);
                        long oldId = Long.parseLong(columns[0]);
                        taskMap.put(oldId, savedTask);
                    } else {
                        System.out.println("ПРЕДУПРЕЖДЕНИЕ: Пропущена задача '" + task.getName() + "' (строка " + lineNumber + ") из-за ненайденной связи.");
                    }

                } catch (NumberFormatException e) {
                    System.out.println("ОШИБКА: Не удалось прочитать число в строке " + lineNumber + " в Tasks.csv. Строка пропущена.");
                }
            }
        }
    }

    private void loadProjectMembers() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("AvigeyaProjectDataBase - ProjectMembers.csv");
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

                        if (member.getProject() != null && member.getUser() != null) {
                            projectMemberRepository.save(member);
                        }
                    } catch (NumberFormatException e) { /* пропускаем */ }
                }
            }
        }
    }

    private void loadTaskMembers() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("AvigeyaProjectDataBase - TaskMembers.csv");
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
                    } catch (NumberFormatException e) { /* пропускаем */ }
                }
            }
        }
    }
}