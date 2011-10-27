package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.compat.Guid;

/**
 * <code>BaseDAOHibernateImpl</code> captures the common functions for DAOs.
 *
 */
public abstract class BaseDAOHibernateImpl<T, PK extends Serializable> {
    private static ThreadLocal<Session> sessions = new ThreadLocal<Session>();

    private Class<T> type;

    public BaseDAOHibernateImpl(Class<T> type) {
        this.type = type;
    }

    public void setSession(Session session) {
        sessions.set(session);
    }

    protected Session getSession() {
        return sessions.get();
    }

    @SuppressWarnings("unchecked")
    public T get(PK id) {
        Session session = getSession();
        T result = (T) session.get(type, id);

        return result;
    }

    public T getByName(final String name) {
        return findOneByCriteria(Restrictions.eq("name", name));
    }

    public List<T> getAll() {
        return findByCriteria();
    }

    /**
     * Supports returning a single object based on specified criteria.
     *
     * @param criteria
     *            the search criteria
     * @return the object
     */
    @SuppressWarnings("unchecked")
    protected T findOneByCriteria(Criterion... criteria) {
        Criteria filter = getSession().createCriteria(type);

        for (Criterion criterion : criteria) {
            filter.add(criterion);
        }

        return (T) filter.uniqueResult();
    }

    /**
     * Retrieves all instances with the specified query.
     *
     * @param query
     *            the query
     * @return the list of results
     */
    @SuppressWarnings("unchecked")
    public List<T> findAllWithSQL(String sql) {
        Session session = getSession();
        SQLQuery query = session.createSQLQuery(sql);

        return (List<T>) query.list();
    }

    /**
     * Supports returning lists of objects based on specified criteria.
     *
     * @param criteria
     *            the search criteria
     * @return the list of objects
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criteria) {
        Session session = getSession();
        Criteria filter = session.createCriteria(type);

        for (Criterion criterion : criteria) {
            filter.add(criterion);
        }

        List<T> result = filter.list();

        return result;
    }

    public void save(T instance) {
        Session session = getSession();

        session.beginTransaction();
        session.save(instance);
        session.getTransaction().commit();
    }

    public void update(T instance) {
        save(instance);
    }

    public void updateStatus(T instance) {
        //TODO
    }

    public void remove(PK id) {
        T instance = get(id);

        if (instance != null) {
            Session session = getSession();

            session.beginTransaction();
            session.delete(instance);
            session.getTransaction().commit();
        }
    }

    /**
     * Takes as input a series of comma-delimited UUID values. It then converts them into a List containing Guid objects
     * and returns that.
     *
     * @param ids
     *            the ids
     * @return the list of Guids
     */
    protected List<Guid> convertIdsToGuids(String ids) {
        List<Guid> filter = new ArrayList<Guid>();
        StringTokenizer tokens = new StringTokenizer(ids, ",", false);

        while (tokens.hasMoreElements()) {
            filter.add(new Guid(tokens.nextToken()));
        }
        return filter;
    }
}
