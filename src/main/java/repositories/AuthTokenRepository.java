package repositories;

import models.AuthToken;

import java.util.List;
import java.util.Optional;

public interface AuthTokenRepository {
    Optional<AuthToken> findByValue(String value);
    List<AuthToken> findByUserId(long userId);
    AuthToken save(AuthToken authToken);
    void setIsActive(AuthToken authToken, boolean isActive);
}
