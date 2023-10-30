package ru.pleshkova.GetNASAPictureBot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.pleshkova.GetNASAPictureBot.config.BotConfig;
import ru.pleshkova.GetNASAPictureBot.model.User;
import ru.pleshkova.GetNASAPictureBot.model.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private UserRepository userRepository;
    private final BotConfig botConfig;
    private final NasaService nasaService;
    private final DateTimeFormatter formatterFromClient = DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ENGLISH);
    private final DateTimeFormatter formatterToClient = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH);
    @Autowired
    public TelegramBot(BotConfig botConfig, NasaService nasaService, UserRepository userRepository) {
        this.botConfig = botConfig;
        this.nasaService = nasaService;
        this.userRepository = userRepository;
        List<BotCommand> listCommands = new ArrayList<>();
        listCommands.add(new BotCommand("/start", "Начать работу"));
        listCommands.add(new BotCommand("/pic", "Показать картинку дня"));
        listCommands.add(new BotCommand("/description", "Показать описание картинки дня"));
        listCommands.add(new BotCommand("/info", "Помощь в работе с ботом"));
        try {
            this.execute(new SetMyCommands(listCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error during adding bot commands: " + e.getMessage());
        }
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

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().toLowerCase();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getFirstName();
            log.info("Receive from user "+ chatId + ", name " + userName + " : " + messageText);
            LocalDateTime today = LocalDateTime.ofEpochSecond(update.getMessage().getDate(), 0, ZoneOffset.UTC);

            switch (messageText) {
                case "/start" -> {
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, userName);}
                case "/pic" -> sendDayPicture(chatId, today);
                case "/description" -> sendDescriptionOfDayPicture(chatId, today);
                case "/info" -> sendMessage(chatId, "Чтобы получить сегодняшнюю картинку дня пришли /pic \n" +
                        " Чтобы получить описание к картинке дня пришли /description \n" +
                        "Если хочешь получить картинку другого дня пришли сообщение в формате, например, /pic17062023");
                default -> {
                    if (messageText.startsWith("/pic")) {
                        sendDayPicture(chatId, messageText.replaceFirst("/pic", ""));
                    } else {
                        sendMessage(chatId, "Прости, я такого не умею");
                    }
                }
            }
        }
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()){
            Long chatId = message.getChatId();
            Chat chat = message.getChat();
            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved " + user);
        }
    }

    private void startCommandReceived(long chatId, String name){
        String answer = "Привет, " + name +"! Чтобы получить сегодняшнюю картинку дня пришли /pic \n" +
                "Больше информации в /info";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend)  {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
            log.info("Reply to user: " + textToSend);
        } catch (TelegramApiException e) {
            log.error("Error occurred during sending message: " + e.getMessage());
        }
    }

    private void sendDayPicture(long chatId, String day)  {
        try { LocalDateTime timeFromClient = LocalDate.parse(day, formatterFromClient).atStartOfDay();
            if (timeFromClient.isBefore(LocalDateTime.now())) {
                sendDayPicture(chatId, timeFromClient);
            } else {
                sendMessage(chatId, "Нельзя получить картинку из будущего");
                log.info("User " + chatId + " enter future data: " + day + ". Today is " + LocalDateTime.now());
            }
        } catch (DateTimeParseException exception) {
            sendMessage(chatId, "Некорректно введено число");
            log.info("User enter wrong data: " + exception.getMessage());
        } catch (Exception generalException) {
            log.info("Unknown mistake during parsing data: " + generalException.getMessage());
            sendMessage(chatId, "Неизвестная ошибка");
        }
    }

    private void sendDayPicture(long chatId, LocalDateTime day)  {
        String textToSend = "Картинка дня " + day.format(formatterToClient) + " : \n " + nasaService.getNasaDayUrl(day);
        sendMessage(chatId, textToSend);
    }

    private void sendDescriptionOfDayPicture(long chatId, LocalDateTime day)  {
        String textToSend = "Описание к картинке дня " + day.format(formatterToClient) + " : \n " + nasaService.getNasaDayDesc(day);
        sendMessage(chatId, textToSend);
    }

}
