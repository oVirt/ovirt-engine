package org.ovirt.engine.core.bll.scheduling;

import static org.mockito.Mockito.mock;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.aaa.SsoSessionUtils;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.di.Injector;

@Singleton
public class CommonTestMocks {

    // providers of common dependencies
    @Produces
    private DbFacadeLocator dbFacadeLocator = mock(DbFacadeLocator.class);
    @Produces
    private QuotaManager quotaManager = mock(QuotaManager.class);
    @Produces
    private HostDeviceManager hostDeviceManager = mock(HostDeviceManager.class);
    @Produces
    private JobDao jobDao = mock(JobDao.class);
    @Produces
    SsoSessionUtils sessionUtils = mock(SsoSessionUtils.class);
    @Produces
    private SlaValidator slaValidator = mock(SlaValidator.class);
    @Produces
    private VmOverheadCalculator vmOverheadCalculator = mock(VmOverheadCalculator.class);

    public static Class<?>[] commonClasses() {
        return new Class<?>[] {
                CommonTestMocks.class,
                Injector.class,
                SessionDataContainer.class
        };
    }

}
