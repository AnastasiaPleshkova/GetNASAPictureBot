package ru.pleshkova.GetNASAPictureBot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
public class DataTimeConverter {

    private final DateTimeFormatter formatterFromClient = DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ENGLISH);
    private final DateTimeFormatter formatterToClient = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH);

    public Optional<LocalDateTime> convertStringToLDT(String day) {
        LocalDateTime timeFromClient = null;
        try {
            timeFromClient = LocalDate.parse(day, formatterFromClient).atStartOfDay();
        } catch (DateTimeParseException exception) {
            log.info("User enter wrong data: " + exception.getMessage());
        } catch (Exception generalException) {
            log.info("Unknown mistake during parsing data: " + generalException.getMessage());
        }
        return Optional.of(timeFromClient);
    }

    public String convertToClientTime(LocalDateTime day) {
        return day.format(formatterToClient);
    }

    public String convertFromClientTime(LocalDateTime day) {
        return day.format(formatterFromClient);
    }

}
