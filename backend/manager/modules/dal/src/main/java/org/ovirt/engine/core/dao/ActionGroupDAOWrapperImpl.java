package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.Session;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.action_version_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.actiongroup.ActionVersionMapDAOHibernateImpl;
import org.ovirt.engine.core.dao.actiongroup.RoleGroupMapDAOHibernateImpl;

public class ActionGroupDAOWrapperImpl extends BaseDAOWrapperImpl implements ActionGroupDAO {
    private RoleGroupMapDAOHibernateImpl roleGroupMapDAO = new RoleGroupMapDAOHibernateImpl();
    private ActionVersionMapDAOHibernateImpl actionVersionMapDAO = new ActionVersionMapDAOHibernateImpl();

    @Override
    public void setSession(Session session) {
        super.setSession(session);

        roleGroupMapDAO.setSession(session);
    }

    @Override
    public List<ActionGroup> getAllForRole(Guid id) {
        return roleGroupMapDAO.getAllForRole(id);
    }

    @Override
    public action_version_map getActionVersionMapByActionType(VdcActionType action_type) {
        return actionVersionMapDAO.get(action_type.getValue());
    }

    @Override
    public void addActionVersionMap(action_version_map map) {
        actionVersionMapDAO.save(map);
    }

    @Override
    public void removeActionVersionMap(VdcActionType action_type) {
        actionVersionMapDAO.remove(action_type.getValue());
    }
}
