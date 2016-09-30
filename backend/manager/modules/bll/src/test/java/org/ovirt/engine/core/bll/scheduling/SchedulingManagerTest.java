package org.ovirt.engine.core.bll.scheduling;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.HashSet;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Spy;
import org.ovirt.engine.arquillian.IntegrationTest;
import org.ovirt.engine.arquillian.TransactionalTestBase;
import org.ovirt.engine.core.bll.CpuFlagsManagerHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.bll.scheduling.external.ExternalSchedulerDiscovery;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@Category(IntegrationTest.class)
public class SchedulingManagerTest extends TransactionalTestBase {

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
            mockConfig(ConfigValues.ExternalSchedulerEnabled, false),
            mockConfig(ConfigValues.EnableVdsLoadBalancing, false),
            mockConfig(ConfigValues.MaxSchedulerWeight, 381),
            mockConfig(ConfigValues.SupportedClusterLevels, new HashSet<Version>()),
            mockConfig(ConfigValues.SpmVmGraceForEvenGuestDistribute, 5),
            mockConfig(ConfigValues.MigrationThresholdForEvenGuestDistribute, 5),
            mockConfig(ConfigValues.HighVmCountForEvenGuestDistribute, 5)
    );

    @Inject @Spy
    private Instance<SchedulingManager> schedulingManager;

    @Deployment(name = "SchedulingManagerTest")
    public static JavaArchive deploy() {
        Class<?>[] classes = {
                SchedulingManager.class,
                Mocks.class,
                AuditLogDirector.class,
                MigrationHandler.class,
                BasicMigrationHandler.class,
                CpuFlagsManagerHandler.class,
                ExternalSchedulerDiscovery.class
        };
        return createDeployment(classes);
    }

    @Test
    public void testSchedule() throws Exception {
        assertNotNull(schedulingManager.get());
        verify(schedulingManager.get()).init();
        assertNotNull(schedulingManager.get().getDefaultClusterPolicy());
    }

    @Singleton
    private static class Mocks {
        @Produces @Singleton
        private ResourceManager resourceManager = mock(ResourceManager.class);
        @Produces @Singleton
        private BackendInternal backendInternal = mock(BackendInternal.class);
        @Produces @Singleton
        private NetworkDeviceHelper networkDeviceHelper = mock(NetworkDeviceHelper.class);
    }
}
