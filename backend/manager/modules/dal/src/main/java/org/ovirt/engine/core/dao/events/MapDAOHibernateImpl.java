package org.ovirt.engine.core.dao.events;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.event_map;
import org.ovirt.engine.core.common.businessentities.event_map_id;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class MapDAOHibernateImpl extends BaseDAOHibernateImpl<event_map, event_map_id> {
    public MapDAOHibernateImpl() {
        super(event_map.class);
    }

    public List<event_map> getByEventUpName(String event_up_name) {
        return findByCriteria(Restrictions.eq("id.eventUpName", event_up_name));
    }
}
