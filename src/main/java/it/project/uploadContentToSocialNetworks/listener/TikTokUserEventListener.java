package it.project.uploadContentToSocialNetworks.listener;


import it.project.uploadContentToSocialNetworks.bot.TelegramBot;
import it.project.uploadContentToSocialNetworks.model.TikTokUser;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@AllArgsConstructor
public class TikTokUserEventListener {
    private final TelegramBot telegramBot;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendMessageIfTokenUpdate(TikTokUserTokenUpdatedEvent event) {
        TikTokUser user = event.user();
        Long telegramId = user.getTelegramId();
        if (user.getAccessToken() == null) {
            telegramBot.sendMessageToUser(telegramId, "Приложению не удалось получить доступ к профилю. Свяжитесь с администратором для выяснения причины.");
        } else {
            telegramBot.sendMessageToUser(telegramId, "Успешно залогинился в тикток! Больше авторизация не потребуется.");
        }
    }
}
