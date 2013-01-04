package org.ovirt.engine.core.dao.events;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.EventMap;
import org.ovirt.engine.core.common.businessentities.EventMapId;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class MapDAOHibernateImpl extends BaseDAOHibernateImpl<EventMap, EventMapId> {
    public MapDAOHibernateImpl() {
        super(EventMap.class);
    }

    public List<EventMap> getByEventUpName(String event_up_name) {
        return findByCriteria(Restrictions.eq("id.eventUpName", event_up_name));
    }
}
