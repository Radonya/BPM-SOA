package com.project.bpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BpmApplication {

	public static void main(String[] args) {
		SpringApplication.run(BpmApplication.class, args);
	}

}
