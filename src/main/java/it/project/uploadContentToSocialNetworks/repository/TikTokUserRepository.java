package it.project.uploadContentToSocialNetworks.repository;

import it.project.uploadContentToSocialNetworks.model.TikTokUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TikTokUserRepository extends JpaRepository<TikTokUser, Long> {
    Optional<TikTokUser> getByTelegramId(Long telegramId);

    Optional<TikTokUser> getByState(UUID state);
}
