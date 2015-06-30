package org.ovirt.engine.core.dao.jpa;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.utils.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A shared class for all Daos that use JPA. Note that our code (that uses Daos) does not expect to work with attached
 * entities, so returned entities are detached.
 *
 * @param <T>  The entity type
 * @param <ID> The entity key type
 */
public abstract class AbstractJpaDao<T extends BusinessEntity<ID>, ID extends Serializable>
        implements GenericDao<T, ID> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJpaDao.class);

    private final Class<T> entityType;

    @Inject
    private EntityManagerHolder entityManagerHolder;

    // Don't use. Only added because of CDI requirement
    protected AbstractJpaDao() {
        entityType = null;
    }

    protected AbstractJpaDao(Class<T> entityType) {
        Objects.requireNonNull(entityType, "entityType cannot be null");
        this.entityType = entityType;
    }

    protected EntityManager getEntityManager() {
        return entityManagerHolder.getEntityManager();
    }

    protected String getEntityName() {
            return getEntityManager().getMetamodel().entity(entityType).getName();
    }

    @Override
    public void save(T entity) {
        getEntityManager().merge(entity);
    }

    @Override
    public void update(T entity) {
        getEntityManager().merge(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public T get(ID id) {
        final T entity = getEntityManager().find(entityType, id);
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> getAll() {
        final String qlString = String.format("select e from %s e", getEntityName());
        final TypedQuery<T> query = getEntityManager().createQuery(qlString, entityType);

        return multipleResults(query);
    }

    /**
     * Runs the query and detaches its results.
     *
     * @param query
     *            the {@link TypedQuery} to be run.
     * @return the result {@code List<T>} of the detached entities.
     */
    @Transactional(readOnly = true)
    protected List<T> multipleResults(TypedQuery<T> query) {
        final List<T> resultList = query.getResultList();
        return resultList;
    }

    /**
     * Runs the query and detaches its results.
     *
     * @param query
     *            the {@link TypedQuery} to be run.
     * @return the result {@code List} of the detached entities.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    protected <O> List<O> multipleResults(Query query) {
        final List<O> resultList = query.getResultList();
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
    @Transactional(readOnly = true)
    protected T singleResult(TypedQuery<T> query) {
        try {
            final T entity = query.getSingleResult();
            // getEntityManager().detach(entity);
            return entity;
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void remove(ID id) {
        final T entity = getEntityManager().find(entityType, id);
        if (entity == null) {
            LOG.warn("Trying to remove non-existent {} with id = '{}'",
                    entityType.getSimpleName(),
                    id);
        } else {
            remove(entity);
        }
    }

    public void remove(T entity) {
        getEntityManager().remove(entity);
    }

    protected void updateQuery(final Query query) {
        query.executeUpdate();
    }

    protected Object updateQueryGetResult(Query query) {
        return query.getSingleResult();
    }
}
