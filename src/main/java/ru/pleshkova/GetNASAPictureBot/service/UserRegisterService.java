package ru.pleshkova.GetNASAPictureBot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.pleshkova.GetNASAPictureBot.model.User;
import ru.pleshkova.GetNASAPictureBot.model.UserRepository;

import java.sql.Timestamp;

@Slf4j
@Component
public class UserRegisterService {
    private final UserRepository userRepository;
    @Autowired
    public UserRegisterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()){
            Chat chat = message.getChat();
            User user = new User(message.getChatId(), chat.getFirstName(), chat.getLastName(), chat.getUserName(), new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("user saved " + user);
        }
    }
}
