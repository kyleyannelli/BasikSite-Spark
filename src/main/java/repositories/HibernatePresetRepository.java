package repositories;

import models.Preset;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class HibernatePresetRepository implements PresetRepository {
    private final SessionFactory sessionFactory;
    public HibernatePresetRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Preset> findById(Long id) {
        try(Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.createQuery("FROM models.Preset WHERE id = :id", Preset.class)
                    .setParameter("id", id)
                    .uniqueResult());
        }
    }

    @Override
    public List<Preset> findAllByUserId(Long userId) {
        try(Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM models.Preset WHERE user = :userId", Preset.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    @Override
    public Preset save(Preset preset) {
        try(Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(preset);
            session.getTransaction().commit();
            return preset;
        }
    }
}
