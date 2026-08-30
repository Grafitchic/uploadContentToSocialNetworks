package it.project.uploadContentToSocialNetworks.feignclient;

import it.project.uploadContentToSocialNetworks.model.TikTokTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tiktok-feign-client", url = "tiktok.url")
public interface TikTokFeignClient {
    @PostMapping(value = "/token/", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TikTokTokenResponse exchangeToken(
            @RequestParam("client_key") String clientKey,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code,
            @RequestParam("grant_type") String grantType,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier
    );

    @PostMapping(value = "/token/", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TikTokTokenResponse refreshAccessToken(
            @RequestParam("client_key") String clientKey,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("refresh_token") String refreshToken,
            @RequestParam("grant_type") String grantType
    );
}
