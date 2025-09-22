/*

    ================================================================================
    ========================== КРИТИЧЕСКАЯ ОШИБКА СТРУКТУРЫ ==========================
    ================================================================================

    ЭТОТ ФАЙЛ НАХОДИТСЯ В НЕПРАВИЛЬНОМ МЕСТЕ.

    Тестовые классы должны располагаться в директории 'src/test/java', а не 'src/main/java'.

    Пока этот файл находится здесь, он НИКОГДА не скомпилируется, потому что
    тестовые библиотеки (JUnit, Mockito, Spring Test) здесь недоступны.

    ================================== ЧТО НУЖНО СДЕЛАТЬ ==================================

    1. ПЕРЕМЕСТИТЕ ЭТОТ ФАЙЛ:
       - ИЗ: src/main/java/com/avigeya/projectboard/service
       - В:  src/test/java/com/avigeya/projectboard/controller

    2. ПОСЛЕ ПЕРЕМЕЩЕНИЯ, ЗАМЕНИТЕ ВСЕ СОДЕРЖИМОЕ ЭТОГО ФАЙЛА НА КОД НИЖЕ:

       (Этот код полностью исправен и будет работать в правильном месте)

*/

/*
package com.avigeya.projectboard.controller;

import com.avigeya.projectboard.dto.TaskBatchUpdateRequest;
import com.avigeya.projectboard.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void whenBatchUpdateTasks_thenReturnsSuccess() throws Exception {
        // 1. Подготовка данных
        TaskBatchUpdateRequest updateRequest = new TaskBatchUpdateRequest();
        updateRequest.setTaskId(1L);
        updateRequest.setStatusId(2L);
        updateRequest.setPriority(1);

        List<TaskBatchUpdateRequest> requestList = Collections.singletonList(updateRequest);

        // 2. Настройка мока (заглушки) для сервиса
        doNothing().when(taskService).batchUpdateTasks(any());

        // 3. Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/v1/tasks/batch-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andExpect(status().isOk());
    }
}
*/
