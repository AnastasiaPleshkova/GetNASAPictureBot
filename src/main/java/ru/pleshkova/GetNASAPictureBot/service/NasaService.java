package ru.pleshkova.GetNASAPictureBot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.pleshkova.GetNASAPictureBot.config.NasaConfig;
import ru.pleshkova.GetNASAPictureBot.model.NasaAnswer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;
@Component
public class NasaService {
    private final NasaConfig nasaConfig;
    private final DateTimeFormatter formatterToNasa = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    @Autowired
    public NasaService(NasaConfig nasaConfig) {
        this.nasaConfig = nasaConfig;
    }

    public String getNasaUrl(LocalDateTime day) {
        String mainAnswer;
        CloseableHttpClient client = HttpClients.createDefault();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CloseableHttpResponse response = client.execute(new HttpGet(nasaConfig.getNasaUrl()+nasaConfig.getNasaKey()+"&date="+day.format(formatterToNasa)));
            NasaAnswer answer = objectMapper.readValue(response.getEntity().getContent(), NasaAnswer.class);
//            System.out.println(answer.toString());
//            CloseableHttpResponse imageResponse = client.execute(new HttpGet(answer.getUrl()));
//            String imageName = answer.getTitle().chars().filter(Character::isLetterOrDigit)
//                    .limit(30).mapToObj(i -> String.valueOf((char) i))
//                    .collect(Collectors.joining());
//            if (answer.getMedia_type().equals("image")) {
//                FileOutputStream fos = new FileOutputStream("src/main/resources/"+imageName+".jpg");
//                imageResponse.getEntity().writeTo(fos); }
//            else {
//                System.out.println(answer.getUrl());
//            }
            mainAnswer = answer.getUrl();
        } catch (IOException e) {
            e.printStackTrace();
            mainAnswer = "не получилось(";
        }

        return mainAnswer;
    }


}
