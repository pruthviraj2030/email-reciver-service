package com.refycap.email_receiver.config;

import com.refycap.email_receiver.service.EmailListener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.mail.Session;
import java.util.Properties;

@Configuration
@PropertySource(value = {"classpath:application.properties"})
public class EmailConfig {

    @Value("${email.host}")
    private String emailHost;

    @Value("${email.port}")
    private String emailPort;
    @Value("${email.username}")
    private String emailUsername;
    @Value("${email.password}")
    private String emailPassword;

    @Bean
    public Session session() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", emailHost);
        props.setProperty("mail.imaps.port", emailPort);


        // Create a new session with the properties
        Session session = Session.getInstance(props);
        session.setDebug(true); // Enable debug mode for troubleshooting

        return session;
    }

    @Bean
    public EmailListener emailListener() {
        return new EmailListener(session(), emailUsername, emailPassword);
    }
}
