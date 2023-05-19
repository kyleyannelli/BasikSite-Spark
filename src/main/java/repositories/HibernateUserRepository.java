package repositories;

import models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateUserRepository implements UserRepository {
    private final Session session;

    public HibernateUserRepository(Session session) {
        this.session = session;
    }

    @Override
    public User findById(long id) {
        return session.get(User.class, id);
    }

    @Override
    public User findByUsername(String username) {
        return session.createQuery("FROM USERS WHERE username = :username", User.class)
                .setParameter("username", username)
                .uniqueResult();
    }

    @Override
    public User save(User user) {
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(user);
        transaction.commit();
        return user;
    }
}
