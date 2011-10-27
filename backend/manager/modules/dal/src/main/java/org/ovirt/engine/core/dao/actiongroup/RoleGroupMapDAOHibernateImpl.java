package org.ovirt.engine.core.dao.actiongroup;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class RoleGroupMapDAOHibernateImpl extends BaseDAOHibernateImpl<RoleGroupMap, Guid> {
    public RoleGroupMapDAOHibernateImpl() {
        super(RoleGroupMap.class);
    }

    public List<ActionGroup> getAllForRole(Guid id) {
        List<RoleGroupMap> maps = findByCriteria(Restrictions.eq("id.roleId", id));
        List<ActionGroup> result = new ArrayList<ActionGroup>();

        for(RoleGroupMap map: maps) {
            result.add(map.getActionGroup());
        }

        return result;
    }
}
