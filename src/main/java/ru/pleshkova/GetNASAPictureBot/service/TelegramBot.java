package ru.pleshkova.GetNASAPictureBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.pleshkova.GetNASAPictureBot.config.BotConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final NasaService nasaService;
    private final DateTimeFormatter formatterFromClient = DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ENGLISH);
    private final DateTimeFormatter formatterToClient = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH);
    @Autowired
    public TelegramBot(BotConfig botConfig, NasaService nasaService) {
        this.botConfig = botConfig;
        this.nasaService = nasaService;
    }
    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }
    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            } else if (messageText.equals("/pic")) {
                sendDayPicture(chatId, LocalDateTime.ofEpochSecond(update.getMessage().getDate(),0,ZoneOffset.UTC));
            } else if (messageText.startsWith("/pic")) {
                sendDayPicture(chatId, messageText.replaceFirst("/pic",""));
            } else {
                sendMessage(chatId, "Прости, я такого не умею");
            }
        }
    }
    private void startCommandReceived(long chatId, String name){
        String answer = "Привет, " + name +"! Чтобы получить сегодняшнюю картинку дня пришли /pic \n" +
                "Если хочешь получить картинку другого дня пришли сообщение в формате, например, /pic17062023";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend)  {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendDayPicture(long chatId, String day)  {
        try {
        LocalDateTime timeFromClient = LocalDate.parse(day, formatterFromClient).atStartOfDay();
        if (timeFromClient.isBefore(LocalDateTime.now())) {
            sendDayPicture(chatId, timeFromClient);
        } else {
            sendMessage(chatId, "Нельзя получить картинку из будущего");
        }
        } catch (DateTimeParseException exception) {
            sendMessage(chatId, "Некорректно введено число");
        }
    }

    private void sendDayPicture(long chatId, LocalDateTime day)  {
        String textToSend = "Картинка дня " + day.format(formatterToClient) + " : \n " + nasaService.getNasaUrl(day);
        sendMessage(chatId, textToSend);
    }

}
