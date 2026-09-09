package it.project.uploadContentToSocialNetworks.service;

import it.project.uploadContentToSocialNetworks.bot.TelegramBot;
import it.project.uploadContentToSocialNetworks.config.property.TikTokProperties;
import it.project.uploadContentToSocialNetworks.feignclient.TikTokFeignClient;
import it.project.uploadContentToSocialNetworks.listener.TikTokUserTokenUpdatedEvent;
import it.project.uploadContentToSocialNetworks.model.TikTokTokenResponse;
import it.project.uploadContentToSocialNetworks.model.TikTokUser;
import it.project.uploadContentToSocialNetworks.model.TokenExchangeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TikTokOAuthService {

    private static final String RESPONSE_TYPE = "code";
    private static final String PKCE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
    private static final String SHA256_CRYPTO_ALGORITHM = "S256";


    private final TikTokProperties properties;
    private final TikTokFeignClient tikTokFeignClient;
    private final TikTokUserService tikTokUserService;
    private final ApplicationEventPublisher eventPublisher;

    private final SecureRandom secureRandom = new SecureRandom();

    public String initiateTikTokLogin(Long telegramId) {
        UUID stateUuid = UUID.randomUUID();
        String stateString = stateUuid.toString();
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        fillTikTokUser(telegramId, codeVerifier, stateUuid);
        String authorizationUrl = buildAuthorizationUrl(stateString, codeChallenge);
        log.info("Redirecting to TikTok OAuth with PKCE");
        return authorizationUrl;
    }

    @Transactional
    public void handleTikTokCallback(String code, String state, String error, String errorDescription) {
        if (error != null) {
            log.error("Ошибка получения кода авторизации от ТикТок: {} - {}", error, errorDescription);
        }
        TikTokUser userByState = tikTokUserService.getUserByState(UUID.fromString(state)).orElse(null);
        if (userByState == null) {
            log.error("После получения кода авторизации не совпал state в ответе с значением в БД у пользователя: {}", state);
            return;
        }
        String codeVerifier = userByState.getCodeVerifier();
        log.info("Обмен кода авторизации на токен");
        TikTokTokenResponse tokenResponse = exchangeCodeForToken(code, codeVerifier);
        if (tokenResponse != null && tokenResponse.accessToken() != null) {
            userByState.setAccessToken(tokenResponse.accessToken());
            userByState.setRefreshToken(tokenResponse.refreshToken());
            userByState.setExpiresIn(tokenResponse.expiresIn());
            userByState.setRefreshExpiresIn(tokenResponse.refreshExpiresIn());
            userByState.setOpenId(tokenResponse.openId());
            userByState.setScope(tokenResponse.scope());
            TikTokUser savedUser = tikTokUserService.save(userByState);
            log.info("Успешно залогинился в тикток!");
            eventPublisher.publishEvent(new TikTokUserTokenUpdatedEvent(savedUser));
        }
        log.error("Ошибка при получении токена: {} - {}", tokenResponse.error(), tokenResponse.errorDescription());
        eventPublisher.publishEvent(new TikTokUserTokenUpdatedEvent(userByState));
    }

    private void fillTikTokUser(Long telegramId, String codeVerifier, UUID state) {
        TikTokUser tikTokUser;
        Optional<TikTokUser> tikTokUserOpt = tikTokUserService.getUserByTelegramId(telegramId);
        if (tikTokUserOpt.isPresent()) {
            tikTokUser = tikTokUserOpt.get();
        } else {
            TikTokUser newUser = new TikTokUser();
            newUser.setTelegramId(telegramId);
            tikTokUser = newUser;
        }
        tikTokUser.setCodeVerifier(codeVerifier);
        tikTokUser.setState(state);
        tikTokUserService.save(tikTokUser);
    }

    /**
     * Формирует URL для перенаправления пользователя на авторизацию TikTok
     */
    public String buildAuthorizationUrl(String state, String codeChallenge) {
        return UriComponentsBuilder
                .fromUriString("https://www.tiktok.com/v2/auth/authorize/")
                .queryParam("client_key", properties.clientKey())
                .queryParam("redirect_uri", URLEncoder.encode(properties.redirectUri(), StandardCharsets.UTF_8))
                .queryParam("response_type", RESPONSE_TYPE)
                .queryParam("scope", String.join(",", properties.scopes()))
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", SHA256_CRYPTO_ALGORITHM)
                .build()
                .toUriString();
    }

    /**
     * Генерирует code_verifier для PKCE
     * Длина: 43-128 символов
     */
    private String generateCodeVerifier() {
        StringBuilder codeVerifier = new StringBuilder();
        for (int i = 0; i < 64; i++) { // Оптимальная длина - 64 символа
            codeVerifier.append(PKCE_CHARS.charAt(secureRandom.nextInt(PKCE_CHARS.length())));
        }
        return codeVerifier.toString();
    }

    /**
     * Генерирует code_challenge из code_verifier используя SHA-256
     * Возвращает hex-encoded хеш
     */
    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Конвертирует байты в hex строку
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }


    /**
     * Обменивает authorization code на access token
     */
    private TikTokTokenResponse exchangeCodeForToken(String code, String codeVerifier) {
        Map<String, String> mapData = new HashMap<>();
        mapData.put("client_key", properties.clientKey());
        mapData.put("client_secret", properties.clientSecret());
        mapData.put("code", code);
        mapData.put("grant_type", "authorization_code");
        mapData.put("redirect_uri", properties.redirectUri());
        mapData.put("code_verifier", codeVerifier);
        return tikTokFeignClient.exchangeToken(mapData);
//        TokenExchangeRequest request = TokenExchangeRequest.builder()
//                .clientKey(properties.clientKey())
//                .clientSecret(properties.clientSecret())
//                .code(code)
//                .grantType("authorization_code")
//                .redirectUri(properties.redirectUri())
//                .codeVerifier(codeVerifier)
//                .build();
//        return tikTokFeignClient.exchangeToken(request);
//        return tikTokFeignClient.exchangeToken(properties.clientKey(),
//                properties.clientSecret(),
//                code,
//                "authorization_code",
//                properties.redirectUri(),
//                codeVerifier);
    }

    /**
     * Обновляет access token с помощью refresh token
     */
    public TikTokTokenResponse refreshAccessToken(String refreshToken) {
        return tikTokFeignClient.refreshAccessToken(
                properties.clientKey(),
                properties.clientSecret(),
                refreshToken,
                "refresh_token"
        );
    }

}