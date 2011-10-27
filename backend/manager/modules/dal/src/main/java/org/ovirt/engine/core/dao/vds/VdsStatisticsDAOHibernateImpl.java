package org.ovirt.engine.core.dao.vds;

import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

/**
 * <code>VdsStatisticsDAOHibernateImpl</code> provides a DAO for working with instances of {@link VdsStatistics}.
 *
 */
public class VdsStatisticsDAOHibernateImpl extends BaseDAOHibernateImpl<VdsStatistics, Guid> {
    public VdsStatisticsDAOHibernateImpl() {
        super(VdsStatistics.class);
    }
}
