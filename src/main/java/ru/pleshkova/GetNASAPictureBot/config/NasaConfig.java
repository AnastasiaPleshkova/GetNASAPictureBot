package ru.pleshkova.GetNASAPictureBot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class NasaConfig {
    @Value("${nasa.key}")
    String nasaKey;

    @Value("${nasa.url}")
    String nasaUrl;

}
