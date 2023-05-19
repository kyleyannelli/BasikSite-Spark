package repositories;

import models.User;

public interface UserRepository {
    User findById(long id);
    User findByUsernameAndTag(String username, String tagId);
    User save(User user);
    boolean doesUsernameAndTagExist(String username, String tagId);
    int getCountByUsername(String username);
}
