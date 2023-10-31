package ru.pleshkova.GetNASAPictureBot.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.pleshkova.GetNASAPictureBot.config.TranslatorConfig;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Optional;

@Slf4j
@Component
public class TranslatorService {

    private final TranslatorConfig translatorConfig;
    @Autowired
    public TranslatorService(TranslatorConfig translatorConfig) {
        this.translatorConfig = translatorConfig;
    }

    public Optional<String> translate(String langFrom, String langTo, String text) {
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String urlStr;
        try {
            urlStr = translatorConfig.getTranslatorUrl() +
                    "?q=" + URLEncoder.encode(text, "UTF-8") +
                    "&target=" + langTo +
                    "&source=" + langFrom;

            response = client.execute(new HttpGet(urlStr));
            return EntityUtils.toString(response.getEntity(), "UTF-8").describeConstable();
        } catch (IOException e) {
            log.info("Error during receiving answer from Google: " + e.getMessage());
        }
        return Optional.empty();
    }
}
