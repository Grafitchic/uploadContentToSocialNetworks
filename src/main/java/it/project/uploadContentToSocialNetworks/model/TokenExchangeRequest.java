package it.project.uploadContentToSocialNetworks.model;

import feign.form.FormProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenExchangeRequest {
    @FormProperty("client_key")
    private String clientKey;
    @FormProperty("client_secret")
    private String clientSecret;
    @FormProperty("code")
    private String code;
    @FormProperty("grant_type")
    private String grantType;
    @FormProperty("redirect_uri")
    private String redirectUri;
    @FormProperty("code_verifier")
    private String codeVerifier;
}
