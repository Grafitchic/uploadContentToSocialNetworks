package it.project.uploadContentToSocialNetworks.bot;

import it.project.uploadContentToSocialNetworks.config.property.TelegramBotProperty;
import it.project.uploadContentToSocialNetworks.model.enums.CommandEnum;
import it.project.uploadContentToSocialNetworks.service.TikTokOAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final Long USER_ID = 439240603L;

    private final TelegramBotProperty telegramBotProperty;
    private final TikTokOAuthService tikTokOAuthService;

    public TelegramBot(TelegramBotProperty telegramBotProperty,
                       TikTokOAuthService tikTokOAuthService) {
        super(telegramBotProperty.getToken());
        this.telegramBotProperty = telegramBotProperty;
        this.tikTokOAuthService = tikTokOAuthService;
        setBotCommands();
    }

    @Override
    public String getBotUsername() {
        return telegramBotProperty.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            User user = update.getMessage().getFrom();
            Message message = update.getMessage();
            long chatId = update.getMessage().getChatId();

            if (user.getIsBot() || !user.getId().equals(USER_ID)) {
                log.info("Залезал какой-то чёрт");
                return;
            }

            if (message.isCommand()) {
                String command = message.getText();
                switch (CommandEnum.of(command)) {
                    case CommandEnum.START -> {
                        String redirectUrlToTikTok = tikTokOAuthService.initiateTikTokLogin(user.getId());
                        try {
                            String messageToUser = "Привет, бро. Хочешь постануть в тикток? <a href=\"" + redirectUrlToTikTok + "\">🔐 Авторизация</a>";
                            sendLinkToUser(chatId, messageToUser);
                        } catch (Exception e) {
                            log.error("Ошибка отправки пользователю ссылки на авторизацию в tiktok: {}", redirectUrlToTikTok, e);
                            sendMessageToUser(chatId, "Не удалось сгенерировать ссылку для авторизации в tiktok! Попробуйте повторить попытку позже.");
                        }
                    }
                    case null, default -> sendMessageToUser(chatId, "Неизвестная команда");
                }
            } else {
                if (update.getMessage().hasText()) {
                    log.info("Пользователь с id {} и именем {} отправил сообщение: {}", user.getId(), user.getUserName() == null ? "" : user.getUserName(), message.getText());
                    sendMessageToUser(chatId, "Извини, я всего лишь бот и не могу обрабатывать сообщения людей. Напиши @Khromov_Pavel, если у тебя возник вопрос.");
                }
            }
        }

        if (update.hasCallbackQuery()) {

        }

    }

    public void sendMessageToUser(Long chatId, String message) {
        SendMessage sm = SendMessage.builder()
                .chatId(chatId.toString())
                .text(message).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            log.info("Ошибка отправки сообщения пользователю id {}: {} \n {}", chatId, e.getMessage(), e.getStackTrace());
        }
    }

    public void sendLinkToUser(Long chatId, String message) {
        SendMessage sm = SendMessage.builder()
                .chatId(chatId.toString())
                .text(message)
                .parseMode(ParseMode.HTML)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            log.info("Ошибка отправки сообщения с ссылкой пользователю id {}: {}", chatId, e.getMessage(), e);
        }
    }

    private void setBotCommands() {
        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/start", "Запуск бота и регистрация пользователя"));

        SetMyCommands setMyCommands = new SetMyCommands(commandList, new BotCommandScopeDefault(), null);
        try {
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            log.info("Ошибка установки команд боту: {}", e.getMessage(), e);
        }
    }
}
