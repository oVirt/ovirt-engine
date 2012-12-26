package org.ovirt.engine.core.dao;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Session;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ActionVersionMap;
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
    public ActionVersionMap getActionVersionMapByActionType(VdcActionType action_type) {
        return actionVersionMapDAO.get(action_type.getValue());
    }

    @Override
    public void addActionVersionMap(ActionVersionMap map) {
        actionVersionMapDAO.save(map);
    }

    @Override
    public void removeActionVersionMap(VdcActionType action_type) {
        actionVersionMapDAO.remove(action_type.getValue());
    }

    @Override
    public List<ActionVersionMap> getAllActionVersionMap() {
        throw new NotImplementedException();
    }
}
