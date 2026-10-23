package com.example.studentregistration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		scanBasePackages = "com.example"
)
public class StudentRegistrationApplication {

	public static void main(String[] args) {

		SpringApplication.run(
				StudentRegistrationApplication.class,
				args
		);
	}
}