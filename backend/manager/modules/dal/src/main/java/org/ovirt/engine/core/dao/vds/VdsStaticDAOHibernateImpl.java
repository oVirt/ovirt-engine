package org.ovirt.engine.core.dao.vds;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

/**
 * <code>VdsStaticDAOHibernateImpl</code> extends {@link BaseDAOHibernateImpl} to work with instances of
 * {@link VdsStatic}.
 *
 */
public class VdsStaticDAOHibernateImpl extends BaseDAOHibernateImpl<VdsStatic, NGuid> {
    public VdsStaticDAOHibernateImpl() {
        super(VdsStatic.class);
    }
}
