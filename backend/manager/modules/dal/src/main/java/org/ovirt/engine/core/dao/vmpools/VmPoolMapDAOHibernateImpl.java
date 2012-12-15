package org.ovirt.engine.core.dao.vmpools;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class VmPoolMapDAOHibernateImpl extends BaseDAOHibernateImpl<VmPoolMap, Guid> {
    public VmPoolMapDAOHibernateImpl() {
        super(VmPoolMap.class);
    }

    public VmPoolMap getByVmGuid(Guid vmId) {
        return findOneByCriteria(Restrictions.eq("vmId", vmId));
    }

    public List<VmPoolMap> getVmPoolsMapByVmPoolId(NGuid vmPoolId) {
        return findByCriteria(Restrictions.eq("vmPoolId", vmPoolId));
    }
}
