package it.project.uploadContentToSocialNetworks.config.property;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Slf4j
@ConfigurationProperties(prefix = "tiktok")
public record TikTokProperties(
        String url,
        String clientKey,
        String clientSecret,
        String redirectUri,
        List<String> scopes
) {
    public TikTokProperties {
        log.info("""
                tiktok.url={}
                tiktok.client-key={}
                tiktok.client-secret={}
                tiktok.redirect-uri={}
                tiktok.scopes={}
                """, url, clientKey, clientSecret, redirectUri, String.join(",", scopes));
    }
}
