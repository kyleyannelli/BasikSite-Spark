package repositories;

import models.User;

public interface UserRepository {
    User findById(long id);
    User findByUsername(String username);
    User save(User user);
}
