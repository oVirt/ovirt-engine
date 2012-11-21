package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;

public class AuditLogDAOHibernateImpl extends BaseDAOHibernateImpl<AuditLog, Long> implements AuditLogDAO {
    public AuditLogDAOHibernateImpl() {
        super(AuditLog.class);
    }

    @Override
    public AuditLog get(long id) {
        return get(Long.valueOf(id));
    }

    @Override
    public void remove(long id) {
        remove(Long.valueOf(id));
    }

    @Override
    public List<AuditLog> getAllAfterDate(Date cutoff) {
        return findByCriteria(Restrictions.gt("logTime", cutoff));
    }

    @Override
    public List<AuditLog> getAllByVMName(String vmName) {
        throw new NotImplementedException();
    }

    @Override
    public List<AuditLog> getAllByVMName(String vmName, Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    @Override
    public List<AuditLog> getAllByVMTemplateName(String vmName) {
        throw new NotImplementedException();
    }

    @Override
    public List<AuditLog> getAllByVMTemplateName(String vmName, Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AuditLog> getAllWithQuery(String sql) {
        SQLQuery query = getSession().createSQLQuery(sql).addEntity(AuditLog.class);

        return query.list();
    }

    @Override
    public void removeAllBeforeDate(Date cutoff) {
        Query query = getSession().createQuery("delete from AuditLog where logTime < :cutoff " +
                "and processed = true " +
                "and id not in (select auditLogId from event_notification_hist)");

        query.setParameter("cutoff", cutoff);

        getSession().beginTransaction();
        query.executeUpdate();
        getSession().getTransaction().commit();
    }

    @Override
    public void removeAllForVds(Guid id, boolean configAlerts) {
        Query query = null;

        if (configAlerts) {
            query = getSession().createQuery("delete from AuditLog al where al.vdsId = :vds_id " +
                    "and al.severity >= 10");
        } else {
            query = getSession().createQuery("delete from AuditLog al where al.vds_id = :vds_id " +
                    "and al.severity >= 10 " +
                    "and al.logType >= 9000");
        }

        query.setParameter("vds_id", id);

        getSession().beginTransaction();
        query.executeUpdate();
        getSession().getTransaction().commit();
    }

    @Override
    public void removeAllOfTypeForVds(Guid id, int type) {
        Query query = getSession().createQuery("delete from AuditLog al where al.vdsId = :vds_id " +
                "and al.logType = :log_type");

        query.setParameter("vds_id", id);
        query.setParameter("log_type", type);

        getSession().beginTransaction();
        query.executeUpdate();
        getSession().getTransaction().commit();
    }

    @Override
    public int getTimeToWaitForNextPmOp(String vdsName, String event) {
        throw new NotImplementedException();
    }

    @Override
    public AuditLog getByOriginAndCustomEventId(String origin, int customEventId) {
        throw new NotImplementedException();
    }
}
