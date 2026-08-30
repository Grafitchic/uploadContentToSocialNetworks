package it.project.uploadContentToSocialNetworks.model;

public record TikTokTokenResponse(
        String accessToken,
        String refreshToken,
        Integer expiresIn,
        String openId,
        String scope,
        String tokenType
) {
}
