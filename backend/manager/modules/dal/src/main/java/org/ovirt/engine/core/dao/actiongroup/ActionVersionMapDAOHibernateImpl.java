package org.ovirt.engine.core.dao.actiongroup;

import org.ovirt.engine.core.common.businessentities.ActionVersionMap;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class ActionVersionMapDAOHibernateImpl extends BaseDAOHibernateImpl<ActionVersionMap, Integer> {
    public ActionVersionMapDAOHibernateImpl() {
        super(ActionVersionMap.class);
    }
}
