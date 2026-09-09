package it.project.uploadContentToSocialNetworks.controller;

import it.project.uploadContentToSocialNetworks.service.TikTokOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthCallbackController {

    private final TikTokOAuthService oauthService;

    @GetMapping("/callback/")
    public void handleTikTokCallback(@RequestParam(required = false) String code,
                                     @RequestParam(required = false) String state,
                                     @RequestParam(required = false) String error,
                                     @RequestParam(required = false) String errorDescription) {
        oauthService.handleTikTokCallback(code, state, error, errorDescription);
    }
}
