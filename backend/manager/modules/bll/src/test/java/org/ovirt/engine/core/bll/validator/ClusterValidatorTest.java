package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
public class ClusterValidatorTest {
    @Mock
    private ClusterDao clusterDao;

    @Mock
    private StoragePoolDao dataCenterDao;

    @Mock
    private Cluster cluster;

    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Spy
    @InjectMocks
    private ClusterValidator validator;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels,
                new HashSet<>(Collections.singletonList(Version.getLast()))));
    }

    @Test
    public void nameNotUsed() {
        assertThat(validator.nameNotUsed(), isValid());
    }

    @Test
    public void nameIsAlreadyUsed() {
        when(clusterDao.getByName(any(), anyBoolean())).thenReturn(Collections.singletonList(mock(Cluster.class)));

        assertThat(validator.nameNotUsed(), failsWith(EngineMessage.CLUSTER_CANNOT_DO_ACTION_NAME_IN_USE));
    }

    @Test
    public void cpuTypeValidForClusterWithoutVirtService() {
        assertThat(validator.cpuTypeSupportsVirtService(), isValid());
    }

    @Test
    public void cpuTypeSupportsVirtServiceForVirtCluster() {
        when(cluster.supportsVirtService()).thenReturn(true);
        doReturn(true).when(validator).cpuExists();

        assertThat(validator.cpuTypeSupportsVirtService(), isValid());
    }

    @Test
    public void cpuTypeDoesNotSupportVirtServiceForVirtCluster() {
        when(cluster.supportsVirtService()).thenReturn(true);
        doReturn(false).when(validator).cpuExists();

        assertThat(validator.cpuTypeSupportsVirtService(), failsWith(EngineMessage.ACTION_TYPE_FAILED_CPU_NOT_FOUND));
    }

    @Test
    public void versionSupported() {
        when(cluster.getCompatibilityVersion()).thenReturn(Version.getLast());

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

        assertThat(validator.dataCenterVersionMismatch(), isValid());
    }

    @Test
    public void dataCenterVersionMismatches() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        when(cluster.getCompatibilityVersion()).thenReturn(mock(Version.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.getCompatibilityVersion()).thenReturn(Version.getLowest());
        when(dataCenterDao.get(any())).thenReturn(dataCenter);
        when(cluster.supportsVirtService()).thenReturn(true);

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

        assertThat(validator.localStoragePoolAttachedToSingleCluster(), isValid());
    }

    @Test
    public void localStoragePoolAttachedToSingleCluster() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(dataCenterDao.get(any())).thenReturn(dataCenter);

        assertThat(validator.localStoragePoolAttachedToSingleCluster(), isValid());
    }

    @Test
    public void localStoragePoolAttachedToMultipleClusters() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(dataCenterDao.get(any())).thenReturn(dataCenter);
        when(clusterDao.getAllForStoragePool(any())).thenReturn(Collections.singletonList(mock(Cluster.class)));

        assertThat(validator.localStoragePoolAttachedToSingleCluster(),
                failsWith(EngineMessage.CLUSTER_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE));
    }

    @Test
    public void clusterServiceDefined() {
        when(cluster.supportsGlusterService()).thenReturn(true);

        assertThat(validator.clusterServiceDefined(), isValid());
    }

    @Test
    public void noClusterServiceDefined() {
        assertThat(validator.clusterServiceDefined(),
                failsWith(EngineMessage.CLUSTER_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED));
    }

    @Test
    @MockedConfig("mockConfigurationWithGlusterEnabled")
    public void mixedClusterServicesSupported() {
        when(cluster.supportsGlusterService()).thenReturn(true);
        when(cluster.supportsVirtService()).thenReturn(true);

        assertThat(validator.mixedClusterServicesSupported(), isValid());
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationWithGlusterEnabled() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.AllowClusterWithVirtGlusterEnabled, true));
    }

    @Test
    @MockedConfig("mockConfigurationWithGlusterDisabled")
    public void nonMixedClusterServiceSupported() {
        when(cluster.supportsGlusterService()).thenReturn(true);
        when(cluster.supportsVirtService()).thenReturn(false);

        assertThat(validator.mixedClusterServicesSupported(), isValid());
    }

    @Test
    @MockedConfig("mockConfigurationWithGlusterDisabled")
    public void mixedClusterServicesNotSupported() {
        when(cluster.supportsGlusterService()).thenReturn(true);
        when(cluster.supportsVirtService()).thenReturn(true);

        assertThat(validator.mixedClusterServicesSupported(),
                failsWith(EngineMessage.CLUSTER_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationWithGlusterDisabled() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.AllowClusterWithVirtGlusterEnabled, false));
    }

    @Test
    public void attestationServerNotConfigured() {
        assertThat(validator.attestationServerConfigured(), isValid());
    }

    @Test
    @MockedConfig("mockConfigurationWithAttestationServer")
    public void attestationServerConfigured() {
        when(cluster.supportsTrustedService()).thenReturn(true);

        assertThat(validator.attestationServerConfigured(), isValid());
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationWithAttestationServer() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.AttestationServer, RandomUtils.instance().nextString(10)));
    }

    @Test
    @MockedConfig("mockConfigurationWithEmptyAttestationServer")
    public void attestationServerNotConfiguredProperly() {
        when(cluster.supportsTrustedService()).thenReturn(true);

        assertThat(validator.attestationServerConfigured(),
                failsWith(EngineMessage.CLUSTER_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationWithEmptyAttestationServer() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.AttestationServer, StringUtils.EMPTY));
    }

    @Test
    public void migrationSupported() {
        doReturn(true).when(validator).migrationSupportedForArch(any());

        assertThat(validator.migrationSupported(RandomUtils.instance().nextEnum(ArchitectureType.class)), isValid());
    }

    @Test
    public void migrationNotSupported() {
        doReturn(false).when(validator).migrationSupportedForArch(any());

        assertThat(validator.migrationSupported(RandomUtils.instance().nextEnum(ArchitectureType.class)),
                failsWith(EngineMessage.MIGRATION_ON_ERROR_IS_NOT_SUPPORTED));
    }
}
