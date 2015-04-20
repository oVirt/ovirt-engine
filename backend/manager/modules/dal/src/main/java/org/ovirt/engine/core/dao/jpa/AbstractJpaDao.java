package org.ovirt.engine.core.dao.jpa;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.dao.GenericDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A shared class for all DAOs that use JPA. Note that our code (that uses DAOs) does not expect to work with attached
 * entities, so returned entities are detached.
 *
 * @param <T>  The entity type
 * @param <ID> The entity key type
 */
public abstract class AbstractJpaDao<T extends BusinessEntity<ID>, ID extends Serializable>
        implements GenericDao<T, ID> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJpaDao.class);

    private final Class<T> entityType;
    protected String entityName;

    @PersistenceContext
    protected EntityManager entityManager;

    protected AbstractJpaDao(Class<T> entityType) {
        Objects.requireNonNull(entityType, "entityType cannot be null");

        this.entityType = entityType;
    }

    @PostConstruct
    void init() {
        entityName = entityManager.getMetamodel().entity(entityType).getName();
    }

    @Override
    public void save(T entity) {
        entityManager.persist(entity);
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
    }

    @Override
    public T get(ID id) {
        final T entity = entityManager.find(entityType, id);
        if (entity == null) {
            return null;
        }
        entityManager.detach(entity);
        return entity;
    }

    @Override
    public List<T> getAll() {
        final String qlString = String.format("select e from %s e", entityName);
        final TypedQuery<T> query = entityManager.createQuery(qlString, entityType);

        return multipleResults(query);
    }

    /**
     * Runs the query and detaches its results.
     *
     * @param query
     *            the {@link TypedQuery} to be run.
     * @return the result {@code List<T>} of the detached entities.
     */
    protected List<T> multipleResults(TypedQuery<T> query) {
        final List<T> resultList = query.getResultList();

        for (T entity : resultList) {
            entityManager.detach(entity);
        }

        return resultList;
    }

    /**
     * Runs the query, expect a single result to be returned
     *
     * @param query
     *            the {@link TypedQuery} to be run
     * @return A single object result of the given query. Detached state. Returns {@code null} in case of query does not
     *         return any result.
     */
    protected T singleResult(TypedQuery<T> query) {
        try {
            final T entity = query.getSingleResult();
            entityManager.detach(entity);
            return entity;
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void remove(ID id) {
        final T entity = entityManager.find(entityType, id);
        if (entity == null) {
            LOG.warn("Trying to remove non-existent {} with id = '{}'",
                    entityType.getSimpleName(),
                    id);
        } else {
            entityManager.remove(entity);
        }
    }
}
