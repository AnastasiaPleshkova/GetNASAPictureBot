package ru.pleshkova.GetNASAPictureBot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.pleshkova.GetNASAPictureBot.config.BotConfig;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final NasaService nasaService;
    private final UserRegisterService userRegisterService;
    private final KeyboardGenerator keyboardGenerator;
    private final DataTimeConverter dataTimeConverter;
    private final TranslatorService translatorService;
    protected final static String TRANSLATE_BUTTON = "TRANSLATE_BUTTON";
    protected final static String REPLY_TO_USER = "Reply to user: ";

    @Autowired
    public TelegramBot(BotConfig botConfig, NasaService nasaService, UserRegisterService userRegisterService,
                       KeyboardGenerator keyboardGenerator, DataTimeConverter dataTimeConverter, TranslatorService translatorService) {
        this.botConfig = botConfig;
        this.nasaService = nasaService;
        this.userRegisterService = userRegisterService;
        this.keyboardGenerator = keyboardGenerator;
        this.dataTimeConverter = dataTimeConverter;
        this.translatorService = translatorService;
        getListCommandsMenu();
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
                    userRegisterService.registerUser(update.getMessage());
                    startCommandReceived(chatId, userName);}
                case "/picture" -> sendMessage(chatId, "Картинку какого дня ты хочешь получить?", keyboardGenerator.getDayKeyboard());
                case "/today" -> sendDayPicture(chatId, today);
                case "/yesterday" -> sendDayPicture(chatId, today.minusDays(1));
                case "/another day" -> sendMessage(chatId, "Введи дату в формате /picddMMyyyy. Например, /pic28102023");
                case "/description" -> sendMessage(chatId, "Описание за какой день ты хочешь получить?", keyboardGenerator.getDescDayKeyboard());
                case "/desc today" -> sendDescriptionOfDayPicture(chatId, today);
                case "/desc yesterday" -> sendDescriptionOfDayPicture(chatId, today.minusDays(1));
                case "/desc another day" -> sendMessage(chatId, "Введи дату в формате /descddMMyyyy. Например, /desc28102023");
                case "/info" -> helpCommandReceived(chatId);
                case "/back" -> sendMessage(chatId, "Возвращаемся назад...", keyboardGenerator.getGeneralKeyboard());
                default -> {
                    if (messageText.startsWith("/pic")) {
                        sendAnotherDayPicture(chatId, messageText.replaceFirst("/pic", ""));
                    } else if (messageText.startsWith("/desc")) {
                        sendDescriptionOfDayPicture(chatId, messageText.replaceFirst("/desc", ""));
                    } else {
                        sendMessage(chatId, "Прости, я такого не умею", keyboardGenerator.getGeneralKeyboard());
                    }
                }
            }

        } else if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals(TRANSLATE_BUTTON)) {
                String oldText = update.getCallbackQuery().getMessage().getText();
                Optional<String> newText = translatorService.translate("en", "ru", oldText);
                if (newText.isPresent()) {
                    executeEditMessageText(newText.get(), update.getCallbackQuery().getMessage().getChatId(),
                            update.getCallbackQuery().getMessage().getMessageId());
                } else {
                    sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Прости, произошла ошибки при переводе",
                            keyboardGenerator.getGeneralKeyboard());
                }

            }
        }
    }

    private void startCommandReceived(long chatId, String name){
        sendMessage(chatId, EmojiParser.parseToUnicode("Привет, " + name +"! :blush:"),
                keyboardGenerator.getGeneralKeyboard());
    }

    private void helpCommandReceived(long chatId) {
        sendMessage(chatId, """
                Чтобы получить картинку дня, пришли /picture и выбери дату\s
                Чтобы получить описание к картинке дня, пришли /description и выбери дату\s
                Если хочешь получить картинку конктретного дня, пришли сообщение в формате, например, /pic20102023\s
                После вывода картинки дня будет предложено вывести к ней описание в формате /desc20102023
                Либо ты можешь смостоятельно ввести подобную команду в любой момент времени /desc19202023""",
                keyboardGenerator.getGeneralKeyboard());
    }

    private SendMessage prepareMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        return message;
    }

    private void sendMessage(long chatId, String textToSend, ReplyKeyboard markup)  {
        SendMessage message = prepareMessage(chatId, textToSend);
        message.setReplyMarkup(markup);
        try {
            execute(message);
            log.info(REPLY_TO_USER + textToSend);
        } catch (TelegramApiException e) {
            log.error("Error occurred during sending message: " + e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend)  {
        SendMessage message = prepareMessage(chatId, textToSend);
        try {
            execute(message);
            log.info(REPLY_TO_USER + textToSend);
        } catch (TelegramApiException e) {
            log.error("Error occurred during sending message: " + e.getMessage());
        }
    }

    private void sendAnotherDayPicture(long chatId, String day)  {
        Optional<LocalDateTime> timeFromClient = dataTimeConverter.convertStringToLDT(day);
        if (timeFromClient.isPresent()){
            if (timeFromClient.get().isBefore(LocalDateTime.now())){
                sendDayPicture(chatId, timeFromClient.get());
            } else {
                sendMessage(chatId, "Нельзя получить данные из будущего", keyboardGenerator.getDayKeyboard());
                log.info("User " + chatId + " enter future data: " + day + ". Today is " + LocalDateTime.now());
            }
        } else {
            sendMessage(chatId, "Ошибка чтения числа", keyboardGenerator.getDayKeyboard());
        }
    }
    private void sendDayPicture(long chatId, LocalDateTime day)  {
        String textToSend = "Картинка дня " + dataTimeConverter.convertToClientTime(day) + " : \n " + nasaService.getNasaDayUrl(day);
        sendMessage(chatId, textToSend, keyboardGenerator.getDescriptionOnMemoryDayKeyboard(dataTimeConverter.convertFromClientTime(day)));
    }
    private void sendDescriptionOfDayPicture(long chatId, LocalDateTime day)  {
        String textToSend = "Описание к картинке дня " + dataTimeConverter.convertToClientTime(day) + " : \n " + nasaService.getNasaDayDesc(day);
        sendMessage(chatId, textToSend, keyboardGenerator.getInlineTranslateButton());
    }
    private void sendDescriptionOfDayPicture(long chatId, String day)  {
        Optional<LocalDateTime> timeFromClient = dataTimeConverter.convertStringToLDT(day);
        if (timeFromClient.isPresent()){
            if (timeFromClient.get().isBefore(LocalDateTime.now())){
                sendDescriptionOfDayPicture(chatId, timeFromClient.get());
            } else {
                sendMessage(chatId, "Нельзя получить данные из будущего", keyboardGenerator.getDayKeyboard());
                log.info("User " + chatId + " enter future data: " + day + ". Today is " + LocalDateTime.now());
            }
        } else {
            sendMessage(chatId, "Ошибка чтения числа", keyboardGenerator.getDayKeyboard());
        }
    }
    private void executeEditMessageText(String text, long chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId((int) messageId);
        try {
            execute(message);
            log.info("Reply to user: " + text);
        } catch (TelegramApiException e) {
            log.error("Error occurred during sending message: " + e.getMessage());
        }
    }
    private void getListCommandsMenu() {
        List<BotCommand> listCommands = new ArrayList<>();
        listCommands.add(new BotCommand("/start", "Начать работу"));
        listCommands.add(new BotCommand("/picture", "Показать картинку дня"));
        listCommands.add(new BotCommand("/description", "Показать описание картинки дня"));
        listCommands.add(new BotCommand("/info", "Помощь в работе с ботом"));
        try {
            this.execute(new SetMyCommands(listCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error during adding bot commands: " + e.getMessage());
        }
    }

}
