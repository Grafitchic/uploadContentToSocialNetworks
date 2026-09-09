package it.project.uploadContentToSocialNetworks.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TikTokTokenResponse(
        String accessToken,
        String refreshToken,
        Integer refreshExpiresIn,
        Integer expiresIn,
        String openId,
        String scope,
        String tokenType,
        String error,
//        @JsonProperty("error_description")
        String errorDescription,
//        @JsonProperty("log_id")
        String logId
) {
}
