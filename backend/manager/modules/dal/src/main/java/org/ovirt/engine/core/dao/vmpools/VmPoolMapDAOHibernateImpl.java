package org.ovirt.engine.core.dao.vmpools;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class VmPoolMapDAOHibernateImpl extends BaseDAOHibernateImpl<vm_pool_map, Guid> {
    public VmPoolMapDAOHibernateImpl() {
        super(vm_pool_map.class);
    }

    public vm_pool_map getByVmGuid(Guid vmId) {
        return findOneByCriteria(Restrictions.eq("vmId", vmId));
    }

    public List<vm_pool_map> getVmPoolsMapByVmPoolId(NGuid vmPoolId) {
        return findByCriteria(Restrictions.eq("vmPoolId", vmPoolId));
    }
}
