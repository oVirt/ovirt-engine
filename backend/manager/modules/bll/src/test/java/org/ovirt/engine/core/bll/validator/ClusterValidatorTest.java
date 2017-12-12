package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.Strict;
import org.ovirt.engine.core.bll.CpuFlagsManagerHandler;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.network.FirewallType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(Strict.class)
public class ClusterValidatorTest {

    private static final Version SUPPORTED_VERSION = new Version(1, 1);

    private ClusterValidator validator;

    @Mock
    private DbFacade dbFacade;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private StoragePoolDao dataCenterDao;

    @Mock
    private Cluster cluster;

    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
        mockConfig(ConfigValues.SupportedClusterLevels, new HashSet<>(Collections.singletonList(new Version(3, 0))))
    );

    @Before
    public void setup() {
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);
    }

    @Test
    public void nameNotUsed() {
        when(dbFacade.getClusterDao()).thenReturn(clusterDao);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.nameNotUsed(), isValid());
    }

    @Test
    public void nameIsAlreadyUsed() {
        when(clusterDao.getByName(any(), anyBoolean())).thenReturn(Collections.singletonList(mock(Cluster.class)));
        when(dbFacade.getClusterDao()).thenReturn(clusterDao);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.nameNotUsed(), failsWith(EngineMessage.CLUSTER_CANNOT_DO_ACTION_NAME_IN_USE));
    }

    @Test
    public void cpuTypeValidForClusterWithoutVirtService() {
        assertThat(validator.cpuTypeSupportsVirtService(), isValid());
    }

    @Test
    public void cpuTypeSupportsVirtServiceForVirtCluster() {
        when(cluster.supportsVirtService()).thenReturn(true);
        validator = spy(new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler));
        doReturn(true).when(validator).cpuExists();

        assertThat(validator.cpuTypeSupportsVirtService(), isValid());
    }

    @Test
    public void cpuTypeDoesNotSupportVirtServiceForVirtCluster() {
        when(cluster.supportsVirtService()).thenReturn(true);
        validator = spy(new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler));
        doReturn(false).when(validator).cpuExists();

        assertThat(validator.cpuTypeSupportsVirtService(), failsWith(EngineMessage.ACTION_TYPE_FAILED_CPU_NOT_FOUND));
    }

    @Test
    public void versionSupported() {
        mockConfigRule.mockConfigValue(ConfigValues.SupportedClusterLevels, Collections.singleton(SUPPORTED_VERSION));
        when(cluster.getCompatibilityVersion()).thenReturn(SUPPORTED_VERSION);

        assertThat(validator.versionSupported(), isValid());
    }

    @Test
    public void versionNotSupported() {
        when(cluster.getCompatibilityVersion()).thenReturn(mock(Version.class));

        assertThat(validator.versionSupported(), failsWith(VersionSupport.getUnsupportedVersionMessage()));
    }

    @Test
    public void invalidClusterFirewallTypeForClusterVersion() {
        when(cluster.getCompatibilityVersion()).thenReturn(Version.v3_6);
        when(cluster.getFirewallType()).thenReturn(FirewallType.FIREWALLD);

        assertThat(
            validator.supportedFirewallTypeForClusterVersion(),
            failsWith(EngineMessage.UNSUPPORTED_FIREWALL_TYPE_FOR_CLUSTER_VERSION)
        );
    }

    @Test
    public void validClusterFirewallTypeForClusterVersion() {
        when(cluster.getCompatibilityVersion()).thenReturn(Version.v4_0);
        when(cluster.getFirewallType()).thenReturn(FirewallType.FIREWALLD);

        assertThat(validator.supportedFirewallTypeForClusterVersion(), isValid());
    }

    @Test
    public void dataCenterVersionMatches() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenterDao.get(any())).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.dataCenterVersionMismatch(), isValid());
    }

    @Test
    public void dataCenterVersionMismatches() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        when(cluster.getCompatibilityVersion()).thenReturn(mock(Version.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.getCompatibilityVersion()).thenReturn(SUPPORTED_VERSION);
        when(dataCenterDao.get(any())).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        when(cluster.supportsVirtService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.dataCenterVersionMismatch(),
                failsWith(EngineMessage.CLUSTER_CANNOT_ADD_COMPATIBILITY_VERSION_WITH_LOWER_STORAGE_POOL));
    }

    @Test
    public void dataCenterVersionValidWhenNotAttachedToDataCenter() {
        assertThat(validator.dataCenterVersionMismatch(), isValid());
    }

    @Test
    public void noStoragePoolAttachedToCluster() {
        assertThat(validator.localStoragePoolAttachedToSingleCluster(), isValid());
    }

    @Test
    public void nonLocalStoragePoolAttachedToSingleCluster() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        when(dataCenterDao.get(any())).thenReturn(mock(StoragePool.class));
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.localStoragePoolAttachedToSingleCluster(), isValid());
    }

    @Test
    public void localStoragePoolAttachedToSingleCluster() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(dataCenterDao.get(any())).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        when(dbFacade.getClusterDao()).thenReturn(clusterDao);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.localStoragePoolAttachedToSingleCluster(), isValid());
    }

    @Test
    public void localStoragePoolAttachedToMultipleClusters() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(dataCenterDao.get(any())).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        when(clusterDao.getAllForStoragePool(any())).thenReturn(Collections.singletonList(mock(Cluster.class)));
        when(dbFacade.getClusterDao()).thenReturn(clusterDao);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.localStoragePoolAttachedToSingleCluster(),
                failsWith(EngineMessage.CLUSTER_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE));
    }

    @Test
    public void clusterServiceDefined() {
        when(cluster.supportsGlusterService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.clusterServiceDefined(), isValid());
    }

    @Test
    public void noClusterServiceDefined() {
        assertThat(validator.clusterServiceDefined(),
                failsWith(EngineMessage.CLUSTER_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED));
    }

    @Test
    public void mixedClusterServicesSupported() {
        mockConfigRule.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, true);
        when(cluster.supportsGlusterService()).thenReturn(true);
        when(cluster.supportsVirtService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.mixedClusterServicesSupported(), isValid());
    }

    @Test
    public void nonMixedClusterServiceSupported() {
        mockConfigRule.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, false);
        when(cluster.supportsGlusterService()).thenReturn(true);
        when(cluster.supportsVirtService()).thenReturn(false);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.mixedClusterServicesSupported(), isValid());
    }

    @Test
    public void mixedClusterServicesNotSupported() {
        mockConfigRule.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, false);
        when(cluster.supportsGlusterService()).thenReturn(true);
        when(cluster.supportsVirtService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.mixedClusterServicesSupported(),
                failsWith(EngineMessage.CLUSTER_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED));
    }

    @Test
    public void attestationServerNotConfigured() {
        assertThat(validator.attestationServerConfigured(), isValid());
    }

    @Test
    public void attestationServerConfigured() {
        mockConfigRule.mockConfigValue(ConfigValues.AttestationServer, RandomUtils.instance().nextString(10));
        when(cluster.supportsTrustedService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.attestationServerConfigured(), isValid());
    }

    @Test
    public void attestationServerNotConfiguredProperly() {
        mockConfigRule.mockConfigValue(ConfigValues.AttestationServer, StringUtils.EMPTY);
        when(cluster.supportsTrustedService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler);

        assertThat(validator.attestationServerConfigured(),
                failsWith(EngineMessage.CLUSTER_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED));
    }

    @Test
    public void migrationSupported() {
        validator = spy(new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler));
        doReturn(true).when(validator).migrationSupportedForArch(any());

        assertThat(validator.migrationSupported(RandomUtils.instance().nextEnum(ArchitectureType.class)), isValid());
    }

    @Test
    public void migrationNotSupported() {
        validator = spy(new ClusterValidator(dbFacade, cluster, cpuFlagsManagerHandler));
        doReturn(false).when(validator).migrationSupportedForArch(any());

        assertThat(validator.migrationSupported(RandomUtils.instance().nextEnum(ArchitectureType.class)),
                failsWith(EngineMessage.MIGRATION_ON_ERROR_IS_NOT_SUPPORTED));
    }
}
