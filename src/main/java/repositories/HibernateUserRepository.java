package repositories;

import models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Optional;

public class HibernateUserRepository implements UserRepository {
    private final SessionFactory sessionFactory;

    public HibernateUserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User findById(long id) {
        try(Session session = sessionFactory.openSession()) {
            return session.get(User.class, id);
        }
    }

    @Override
    public User findByUsernameAndTag(String username, String tag) {
        try(Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM models.User WHERE username = :username AND tagId = :tagId", User.class)
                    .setParameter("username", username)
                    .setParameter("tagId", tag)
                    .uniqueResult();
        }
    }

    @Override
    public User save(User user) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.saveOrUpdate(user);
            transaction.commit();
            return user;
        }
    }

    @Override
    public boolean doesUsernameAndTagExist(String username, String tagId) {
        try(Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM models.User WHERE username = :username AND tagId = :tagId", User.class)
                    .setParameter("username", username)
                    .setParameter("tagId", tagId)
                    .uniqueResult() == null ? false : true;
        }
    }

    @Override
    public int getCountByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(*) FROM models.User WHERE username = :username", Long.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        }
    }
}
