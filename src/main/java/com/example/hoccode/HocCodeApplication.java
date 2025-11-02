package com.example.hoccode;

import com.example.hoccode.dto.request.create.CreateNotificationRequest;
import com.example.hoccode.service.INotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class HocCodeApplication {


	public static void main(String[] args) {
		SpringApplication.run(HocCodeApplication.class, args);
	}
	@PostConstruct
	public void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

	}
}
