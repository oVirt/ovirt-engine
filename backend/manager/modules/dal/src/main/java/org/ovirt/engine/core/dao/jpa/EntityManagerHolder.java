package org.ovirt.engine.core.dao.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.springframework.stereotype.Component;

@ApplicationScoped
@Component
public class EntityManagerHolder {
    private ThreadLocal<EntityManager> threadLocal = new ThreadLocal<EntityManager>();

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    public EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();
        if (em == null || !em.isOpen()) {
            em = entityManagerFactory.createEntityManager();
            threadLocal.set(em);
        }
        return em;
    }

    public EntityManager getEntityManagerDontCreate() {
        EntityManager em = threadLocal.get();
        return em;
    }

    public void nullEntityManager() {
        threadLocal.set(null);
    }
}
