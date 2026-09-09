package it.project.uploadContentToSocialNetworks.service;

import it.project.uploadContentToSocialNetworks.model.TikTokUser;
import it.project.uploadContentToSocialNetworks.repository.TikTokUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TikTokUserService {

    private final TikTokUserRepository tikTokUserRepository;

    public Optional<TikTokUser> getUserByTelegramId(Long telegramId) {
        return tikTokUserRepository.getByTelegramId(telegramId);
    }

    public Optional<TikTokUser> getUserByState(UUID state) {
        return tikTokUserRepository.getByState(state);
    }

    public TikTokUser save(TikTokUser TikTokUser) {
        return tikTokUserRepository.save(TikTokUser);
    }

}
