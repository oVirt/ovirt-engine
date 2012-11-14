package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;

/**
 * <code>VdsGroupDAOHibernateImpl</code> provides an implementation of {@link VdsGroupDAO} based on Hibernate.
 *
 */
public class VdsGroupDAOHibernateImpl extends BaseDAOHibernateImpl<VDSGroup, Guid> implements VdsGroupDAO {
    public VdsGroupDAOHibernateImpl() {
        super(VDSGroup.class);
    }

    @Override
    public VDSGroup get(Guid id, Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    @Override
    public VDSGroup getByName(final String name, Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    @Override
    public VDSGroup getWithRunningVms(Guid id) {
        Query query = getSession().getNamedQuery("vdsgroup_with_running_vms");

        query.setParameter("vds_group_id", id);

        return (VDSGroup) query.uniqueResult();
    }

    @Override
    public List<VDSGroup> getAll(Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    @Override
    public List<VDSGroup> getAllForStoragePool(Guid id) {
        return findByCriteria(Restrictions.eq("storagePool", id));
    }

    @Override
    public List<VDSGroup> getAllForStoragePool(Guid id, Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    @Override
    public List<VDSGroup> getAllWithQuery(String query) {
        return findAllWithSQL(query);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDSGroup> getClustersWithPermittedAction(Guid userId, ActionGroup actionGroup) {
        Query query = getSession().getNamedQuery("fn_perms_get_vds_groups_with_with_permitted_action");

        query.setParameter("v_user_id", userId).setParameter("v_action_group_id", actionGroup.getId());

        return (List<VDSGroup>) query.uniqueResult();
    }
}
