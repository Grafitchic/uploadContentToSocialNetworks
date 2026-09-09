package it.project.uploadContentToSocialNetworks.feignclient;

import feign.Headers;
import feign.form.FormProperty;
import it.project.uploadContentToSocialNetworks.config.TikTokFeignConfig;
import it.project.uploadContentToSocialNetworks.model.TikTokTokenResponse;
import it.project.uploadContentToSocialNetworks.model.TokenExchangeRequest;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "tiktok-feign-client", url = "https://open.tiktokapis.com/v2/oauth/token/", configuration = TikTokFeignConfig.class)
public interface TikTokFeignClient {
    @PostMapping(value = "/", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    TikTokTokenResponse exchangeToken(
            @RequestParam Map<String, ?> params
//            @RequestBody TokenExchangeRequest request
//            @RequestParam("client_key")
//            String clientKey,
//            @RequestParam("client_secret")
//            String clientSecret,
//            @RequestParam("code")
//            String code,
//            @RequestParam("grant_type")
//            String grantType,
//            @RequestParam("redirect_uri")
//            String redirectUri,
//            @RequestParam("code_verifier")
//            String codeVerifier
    );

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TikTokTokenResponse refreshAccessToken(
            @RequestParam("client_key") String clientKey,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("refresh_token") String refreshToken,
            @RequestParam("grant_type") String grantType
    );
}
