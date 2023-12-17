package com.epam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class BlogApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogApiApplication.class, args);
	}

}
