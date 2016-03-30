package org.ovirt.engine.core.bll.scheduling;

import static org.mockito.Mockito.mock;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CpuFlagsManagerHandler;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.aaa.SsoSessionUtils;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.EngineSessionDao;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@Singleton
public class CommonTestMocks {

    // providers of common dependencies
    @Produces
    private BackendInternal backendInternal = mock(BackendInternal.class);
    @Produces
    private DbFacade dbFacade = mock(DbFacade.class);
    @Produces
    private AuditLogDirector auditLogDirector = mock(AuditLogDirector.class);
    @Produces
    private ResourceManager resourceManager = mock(ResourceManager.class);
    @Produces
    private PolicyUnitDao policyUnitDao = mock(PolicyUnitDao.class);
    @Produces
    private QuotaManager quotaManager = mock(QuotaManager.class);
    @Produces
    private EngineSessionDao engineSessionDao = mock(EngineSessionDao.class);
    @Produces
    private CpuFlagsManagerHandler cpuFlagsManagerHandler = mock(CpuFlagsManagerHandler.class);
    @Produces
    private ClusterDao clusterDao = mock(ClusterDao.class);
    @Produces
    private HostDeviceManager hostDeviceManager = mock(HostDeviceManager.class);
    @Produces
    private VdsDynamicDao vdsDynamicDao = mock(VdsDynamicDao.class);
    @Produces
    private SsoSessionUtils ssoSessionUtils = mock(SsoSessionUtils.class);
    @Produces
    private JobDao jobDao = mock(JobDao.class);
    @Produces
    private LabelDao labelDao = mock(LabelDao.class);

    public static Class<?>[] commonClasses() {
        return new Class<?>[] {
                CommonTestMocks.class,
                Injector.class,
                SessionDataContainer.class
        };
    }

}
