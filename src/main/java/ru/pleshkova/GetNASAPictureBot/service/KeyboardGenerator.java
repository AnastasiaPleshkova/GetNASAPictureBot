package ru.pleshkova.GetNASAPictureBot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.pleshkova.GetNASAPictureBot.service.TelegramBot.TRANSLATE_BUTTON;

@Component
public class KeyboardGenerator {
    private ReplyKeyboardMarkup keyboardMarkup;
    private List<KeyboardRow> keyboardRows;
    private KeyboardRow row;
    private InlineKeyboardMarkup inlineKeyboardMarkup;

    ReplyKeyboardMarkup getGeneralKeyboard() {
        keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardRows = new ArrayList<>();
        row = new KeyboardRow();
        row.add("/picture");
        row.add("/description");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/info");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    ReplyKeyboardMarkup getDayKeyboard() {
        keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardRows = new ArrayList<>();
        row = new KeyboardRow();
        row.add("/today");
        row.add("/yesterday");
        row.add("/another day");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/back");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    ReplyKeyboardMarkup getDescriptionKeyboard() {
        keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardRows = new ArrayList<>();
        row = new KeyboardRow();
        row.add("/description");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/back");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    ReplyKeyboardMarkup getDescriptionOnMemoryDayKeyboard(String day) {
        keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardRows = new ArrayList<>();
        row = new KeyboardRow();
        row.add("/desc"+day);
        row.add("/description");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/back");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    ReplyKeyboardMarkup getDescDayKeyboard() {
        keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardRows = new ArrayList<>();
        row = new KeyboardRow();
        row.add("/desc today");
        row.add("/desc yesterday");
        row.add("/desc another day");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("/back");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    InlineKeyboardMarkup getInlineTranslateButton() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowinline = new ArrayList<>();
        InlineKeyboardButton desc_button = new InlineKeyboardButton();
        desc_button.setText("Перевести");
        desc_button.setCallbackData(TRANSLATE_BUTTON);
        rowinline.add(desc_button);
        rowsInline.add(rowinline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }
}
