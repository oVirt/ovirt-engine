package org.ovirt.engine.core.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;

import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.jpa.AbstractJpaDao;
import org.ovirt.engine.core.utils.transaction.TransactionalInterceptor;
import org.springframework.stereotype.Component;


/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@link VdsDAO} that uses previously written code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 *
 */
@Interceptors({ TransactionalInterceptor.class })
@ApplicationScoped
@Component
public class VdsStatisticsDAODbFacadeImpl extends AbstractJpaDao<VdsStatistics, Guid> implements VdsStatisticsDAO {

    public VdsStatisticsDAODbFacadeImpl() {
        super(VdsStatistics.class);
    }
}
