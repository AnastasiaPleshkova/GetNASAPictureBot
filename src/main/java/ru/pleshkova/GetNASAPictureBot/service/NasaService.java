package ru.pleshkova.GetNASAPictureBot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.pleshkova.GetNASAPictureBot.config.NasaConfig;
import ru.pleshkova.GetNASAPictureBot.model.NasaAnswer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
public class NasaService {
    private final NasaConfig nasaConfig;
    private final DateTimeFormatter formatterToNasa = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private final String FAILED_ANSWER = "Не получилось связаться с NASA";
    @Autowired
    public NasaService(NasaConfig nasaConfig) {
        this.nasaConfig = nasaConfig;
    }

    private Optional<NasaAnswer> getAnswer(String day) {
        CloseableHttpClient client = HttpClients.createDefault();
        ObjectMapper objectMapper = new ObjectMapper();
        CloseableHttpResponse response = null;
        try {
            response = client.execute(new HttpGet(nasaConfig.getNasaUrl()+nasaConfig.getNasaKey()+"&date="+day));
            return Optional.ofNullable(objectMapper.readValue(response.getEntity().getContent(), NasaAnswer.class));
        } catch (IOException e) {
            log.info("Error during receiving answer from Nasa: " + e.getMessage());
        }
        return Optional.empty();
    }

    public String getNasaDayUrl (LocalDateTime day) {
        Optional<NasaAnswer> answer = getAnswer(day.format(formatterToNasa));
        if (answer.isPresent()) {
            return answer.get().getUrl();
        } else {
            return FAILED_ANSWER;
        }
    }

    public String getNasaDayDesc (LocalDateTime day) {
        Optional<NasaAnswer> answer = getAnswer(day.format(formatterToNasa));
        if (answer.isPresent()) {
            return answer.get().getExplanation();
        } else {
            return FAILED_ANSWER;
        }
    }

}
