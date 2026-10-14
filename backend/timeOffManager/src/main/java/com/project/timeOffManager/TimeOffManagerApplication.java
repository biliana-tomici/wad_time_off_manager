package com.project.timeOffManager;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;


@SpringBootApplication
public class TimeOffManagerApplication {

	private static final Logger log =  LoggerFactory.getLogger(TimeOffManagerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TimeOffManagerApplication.class, args);
	}
}