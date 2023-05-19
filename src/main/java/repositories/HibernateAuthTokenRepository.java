package repositories;

import models.AuthToken;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class HibernateAuthTokenRepository implements AuthTokenRepository {
    private final SessionFactory sessionFactory;
    public HibernateAuthTokenRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    @Override
    public Optional<AuthToken> findByValue(String value) {
        try(Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.createQuery("FROM models.AuthToken WHERE value = :value", AuthToken.class)
                    .setParameter("value", value)
                    .uniqueResult());
        }
    }

    @Override
    public List<AuthToken> findByUserId(long userId) {
        try(Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM models.AuthToken WHERE user = :userId", AuthToken.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    @Override
    public AuthToken save(AuthToken authToken) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.saveOrUpdate(authToken);
            transaction.commit();
            return authToken;
        }
    }
}
