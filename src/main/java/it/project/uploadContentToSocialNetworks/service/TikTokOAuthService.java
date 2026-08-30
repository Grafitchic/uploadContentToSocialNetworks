package it.project.uploadContentToSocialNetworks.service;

import it.project.uploadContentToSocialNetworks.config.property.TikTokProperties;
import it.project.uploadContentToSocialNetworks.feignclient.TikTokFeignClient;
import it.project.uploadContentToSocialNetworks.model.TikTokTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class TikTokOAuthService {

    private static final String RESPONSE_TYPE = "code";
    private static final String PKCE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
    private static final String SHA256_CRYPTO_ALGORITHM = "S256";


    private final TikTokProperties properties;
    private final TikTokFeignClient tikTokFeignClient;
    private final SecureRandom secureRandom = new SecureRandom();

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
    public String generateCodeVerifier() {
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
    public String generateCodeChallenge(String codeVerifier) {
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
    public TikTokTokenResponse exchangeCodeForToken(String code, String codeVerifier) {
        return tikTokFeignClient.exchangeToken(
                properties.clientKey(),
                properties.clientSecret(),
                code,
                "authorization_code",
                properties.redirectUri(),
                codeVerifier
        );
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