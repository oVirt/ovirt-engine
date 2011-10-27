package org.ovirt.engine.core.dao.vmpools;

import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map_id;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class TimeLeaseVmPoolMapDAOHibernateImpl extends BaseDAOHibernateImpl<time_lease_vm_pool_map, time_lease_vm_pool_map_id> {
    public TimeLeaseVmPoolMapDAOHibernateImpl() {
        super(time_lease_vm_pool_map.class);
    }
}
