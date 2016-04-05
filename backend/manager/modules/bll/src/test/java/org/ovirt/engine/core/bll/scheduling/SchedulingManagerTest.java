package org.ovirt.engine.core.bll.scheduling;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.bll.scheduling.external.ExternalSchedulerDiscovery;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.scheduling.ClusterPolicyDao;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;
import org.ovirt.engine.core.di.Injector;

@RunWith(Arquillian.class)
public class SchedulingManagerTest {

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.ExternalSchedulerEnabled, false),
            MockConfigRule.mockConfig(ConfigValues.EnableVdsLoadBalancing, false),
            MockConfigRule.mockConfig(ConfigValues.MaxSchedulerWeight, Integer.MAX_VALUE),
            MockConfigRule.mockConfig(ConfigValues.SpmVmGraceForEvenGuestDistribute, 10),
            MockConfigRule.mockConfig(ConfigValues.MigrationThresholdForEvenGuestDistribute, 5),
            MockConfigRule.mockConfig(ConfigValues.HighVmCountForEvenGuestDistribute, 10)
            );

    @Inject @Spy
    private Instance<SchedulingManager> schedulingManager;

    @Inject
    private DbFacade dbFacade;
    @Inject
    private Injector injector;

    @Produces
    private NetworkDeviceHelper networkDeviceHelper = mock(NetworkDeviceHelper.class);

    @Before
    public void initTest() {
        PolicyUnitDao policyUnitDao = mock(PolicyUnitDao.class);
        ClusterPolicyDao clusterPolicyDao = mock(ClusterPolicyDao.class);
        when(dbFacade.getPolicyUnitDao()).thenReturn(policyUnitDao);
        when(dbFacade.getClusterPolicyDao()).thenReturn(clusterPolicyDao);
        when(policyUnitDao.getAll()).thenReturn(Collections.<PolicyUnit> emptyList());
        when(clusterPolicyDao.getAll(any())).thenReturn(Collections.<ClusterPolicy> emptyList());
    }

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(
                        Injector.class,
                        CommonTestMocks.class,
                        SchedulingManager.class,
                        ExternalSchedulerDiscovery.class,
                        BasicMigrationHandler.class
                        )
                .addClasses(
                        CommonTestMocks.commonClasses()
                )
                .addAsManifestResource(
                        EmptyAsset.INSTANCE,
                        ArchivePaths.create("beans.xml")
                );
    }

    @Test
    public void testSchedule() throws Exception {
        assertNotNull(schedulingManager.get());
        verify(schedulingManager.get()).init();
    }
}
