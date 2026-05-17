package com.OnlineToyStore.Sllit;

import com.OnlineToyStore.Sllit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SllitApplication {

	public static void main(String[] args) {
		ApplicationContext ctx =
				SpringApplication.run(SllitApplication.class, args);

		// Auto-create default admin on first run
		UserService userService = ctx.getBean(UserService.class);
		userService.createDefaultAdminIfNotExists();
	}
}