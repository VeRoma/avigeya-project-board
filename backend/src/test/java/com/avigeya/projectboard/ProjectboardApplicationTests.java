package com.avigeya.projectboard;

import com.avigeya.projectboard.config.DataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ProjectboardApplicationTests {

	// Эта аннотация заменяет настоящий DataInitializer на "пустышку" (мок-объект)
	// во время выполнения этого теста. Это предотвращает попытку загрузки CSV-файлов.
	@MockBean
	private DataInitializer dataInitializer;

	@Test
	void contextLoads() {
		// Теперь этот тест должен успешно пройти, так как проблемный инициализатор данных
		// был заменен на мок.
	}

}
