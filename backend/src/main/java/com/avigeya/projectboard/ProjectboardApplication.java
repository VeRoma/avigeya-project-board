package com.avigeya.projectboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.avigeya.projectboard")
public class ProjectboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectboardApplication.class, args);
	}

}
