package org.ovirt.engine.core.dao.vds;

import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

/**
 * <code>VdsDynamicDAOHibernateImpl</code> extends {@link BaseDAOHibernateImpl} to work with instances of
 * {@link VdsDynamic}.
 *
 */
public class VdsDynamicDAOHibernateImpl extends BaseDAOHibernateImpl<VdsDynamic, NGuid> {
    public VdsDynamicDAOHibernateImpl() {
        super(VdsDynamic.class);
    }
}
