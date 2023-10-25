package ru.pleshkova.GetNASAPictureBot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {

    @Value("${telegramBot.name}")
    String botName;

    @Value("${telegramBot.token}")
    String token;



}
