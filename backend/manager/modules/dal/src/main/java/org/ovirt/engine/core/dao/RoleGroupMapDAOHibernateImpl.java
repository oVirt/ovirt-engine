package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.common.businessentities.RoleGroupMapId;
import org.ovirt.engine.core.compat.Guid;

public class RoleGroupMapDAOHibernateImpl extends BaseDAOHibernateImpl<RoleGroupMap, RoleGroupMapId> implements RoleGroupMapDAO {
    public RoleGroupMapDAOHibernateImpl() {
        super(RoleGroupMap.class);
    }

    @Override
    public RoleGroupMap getByActionGroupAndRole(ActionGroup group, Guid id) {
        return get(new RoleGroupMapId(id, group));
    }

    @Override
    public List<RoleGroupMap> getAllForRole(Guid id) {
        return findByCriteria(Restrictions.eq("id.roleId", id));
    }

    @Override
    public void remove(ActionGroup group, Guid id) {
        remove(new RoleGroupMapId(id, group));
    }
}
