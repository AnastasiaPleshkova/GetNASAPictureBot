package ru.pleshkova.GetNASAPictureBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SpringBootApplication
public class GetNasaPictureBotApplication {

	public static void main(String[] args) {

		SpringApplication.run(GetNasaPictureBotApplication.class, args);

	}

}
