package it.project.uploadContentToSocialNetworks.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "bot")
public class TelegramBotProperty {
    private String name;
    private String token;
}
