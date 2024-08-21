package com.refycap.email_receiver;

import com.refycap.email_receiver.config.EmailConfig;
import com.refycap.email_receiver.service.EmailListener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


@SpringBootApplication
public class EmailReceiverApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(EmailReceiverApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ApplicationContext context = new AnnotationConfigApplicationContext(EmailConfig.class);
		EmailListener emailListener = context.getBean(EmailListener.class);
		emailListener.startListening();
	}
}
