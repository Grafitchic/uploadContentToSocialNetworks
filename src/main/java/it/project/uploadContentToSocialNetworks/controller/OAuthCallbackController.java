package it.project.uploadContentToSocialNetworks.controller;

import it.project.uploadContentToSocialNetworks.model.TikTokTokenResponse;
import it.project.uploadContentToSocialNetworks.service.TikTokOAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthCallbackController {

    private final TikTokOAuthService oauthService;
    private final HttpSession httpSession;

    /**
     * Точка входа для начала OAuth-процесса с PKCE
     */
    @GetMapping("/tiktok/login")
    public String initiateTikTokLogin() {
        // 1. Генерируем state для защиты от CSRF
        String state = UUID.randomUUID().toString();

        // 2. Генерируем code_verifier для PKCE
        String codeVerifier = oauthService.generateCodeVerifier();

        // 3. Генерируем code_challenge из code_verifier
        String codeChallenge = oauthService.generateCodeChallenge(codeVerifier);

        // 4. Сохраняем state и code_verifier в сессию
        // code_verifier понадобится позже при обмене code на токен
        httpSession.setAttribute("tiktok_oauth_state", state);
        httpSession.setAttribute("tiktok_code_verifier", codeVerifier);

        // 5. Формируем URL авторизации с code_challenge
        String authorizationUrl = oauthService.buildAuthorizationUrl(state, codeChallenge);

        log.info("Redirecting to TikTok OAuth with PKCE. State: {}, Code Challenge: {}",
                state, codeChallenge);

        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/callback/")
    public String handleTikTokCallback(@RequestParam(required = false) String code,
                                       @RequestParam(required = false) String state,
                                       @RequestParam(required = false) String error,
                                       @RequestParam(required = false) String error_description,
                                       @RequestParam(required = false) String scopes) {

        // 1. Проверяем наличие ошибок
        if (error != null) {
            log.error("OAuth error: {} - {}", error, error_description);
            return "redirect:/auth/error?message=Authorization+failed:+{}" +
                    URLEncoder.encode(error_description != null ? error_description : error, StandardCharsets.UTF_8);
        }

        // 2. Проверяем state (CSRF защита)
        String sessionState = (String) httpSession.getAttribute("tiktok_oauth_state");
        if (sessionState == null || !sessionState.equals(state)) {
            log.error("State mismatch! Session: {}, Request: {}", sessionState, state);
            return "redirect:/auth/error?message=Invalid+state+parameter";
        }

        // 3. Получаем code_verifier из сессии
        String codeVerifier = (String) httpSession.getAttribute("tiktok_code_verifier");
        if (codeVerifier == null) {
            log.error("Code verifier not found in session. PKCE flow broken.");
            return "redirect:/auth/error?message=Session+expired";
        }

        // 4. Очищаем сессию
        httpSession.removeAttribute("tiktok_oauth_state");
        httpSession.removeAttribute("tiktok_code_verifier");

        // 5. Обмениваем code на токен с использованием code_verifier (PKCE)
        log.info("Exchanging code for token with PKCE...");
        TikTokTokenResponse tokenResponse = oauthService.exchangeCodeForToken(code, codeVerifier);

        if (tokenResponse != null && tokenResponse.accessToken() != null) {
            log.info("✓ Successfully received tokens!");
            log.info("  Access Token: {}", maskToken(tokenResponse.accessToken()));
            log.info("  Refresh Token: {}", maskToken(tokenResponse.refreshToken()));
            log.info("  Open ID: {}", tokenResponse.openId());
            log.info("  Scope: {}", tokenResponse.scope());
            log.info("  Expires In: {} seconds", tokenResponse.expiresIn());

            // 6. Сохраняем токены
//            tokenStorage.saveTokens(tokenResponse);

            return "redirect:/auth/success?open_id=" + tokenResponse.openId();
        }

        log.error("✗ Failed to exchange code for token");
        return "redirect:/auth/error?message=Token+exchange+failed";
    }

    /**
     * Маскирует токен для логирования (показывает только первые и последние символы)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return token;
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
