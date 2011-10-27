package org.ovirt.engine.core.dao.actiongroup;

import org.ovirt.engine.core.common.businessentities.action_version_map;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class ActionVersionMapDAOHibernateImpl extends BaseDAOHibernateImpl<action_version_map, Integer> {
    public ActionVersionMapDAOHibernateImpl() {
        super(action_version_map.class);
    }
}
