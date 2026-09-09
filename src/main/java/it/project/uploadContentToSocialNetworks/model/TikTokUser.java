package it.project.uploadContentToSocialNetworks.model;

import it.project.uploadContentToSocialNetworks.listener.TikTokUserEventListener;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tik_tok_user")
@Getter
@Setter
public class TikTokUser {
    @Id
    private Long telegramId;

    @NotNull
    private String codeVerifier;

    @NotNull
    private UUID state;

    private String accessToken;

    private String refreshToken;

    private Integer refreshExpiresIn;

    private Integer expiresIn;

    private String openId;

    private String scope;
}
