package repositories;

import models.User;

public interface UserRepository {
    User find(long id);
    User find(String username);
    User save(User user);
}
