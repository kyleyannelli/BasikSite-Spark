package repositories;

import models.AuthToken;

import java.util.Set;

public interface AuthTokenRepository {
    AuthToken findByValue(String value);
    Set<AuthToken> findByUserId(long userId);
}
