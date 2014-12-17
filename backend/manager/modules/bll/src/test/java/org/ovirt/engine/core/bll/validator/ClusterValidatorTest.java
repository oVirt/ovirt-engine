package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmRngDevice.Source;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class ClusterValidatorTest {

    private static final Version SUPPORTED_VERSION = new Version(1, 1);

    private ClusterValidator validator;

    @Mock
    private DbFacade dbFacade;

    @Mock
    private VdsGroupDAO clusterDao;

    @Mock
    private StoragePoolDAO dataCenterDao;

    @Mock
    private VDSGroup cluster;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Before
    public void setup() {
        mockConfigRule.mockConfigValue(ConfigValues.SupportedClusterLevels, Collections.singleton(SUPPORTED_VERSION));
        validator = new ClusterValidator(dbFacade, cluster);
    }

    @Test
    public void nameNotUsed() {
        when(clusterDao.getByName(any(String.class), any(Boolean.class))).thenReturn(Collections.<VDSGroup> emptyList());
        when(dbFacade.getVdsGroupDao()).thenReturn(clusterDao);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.nameNotUsed(), isValid());
    }

    @Test
    public void nameIsAlreadyUsed() {
        when(clusterDao.getByName(any(String.class), any(Boolean.class))).thenReturn(Collections.<VDSGroup> singletonList(mock(VDSGroup.class)));
        when(dbFacade.getVdsGroupDao()).thenReturn(clusterDao);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.nameNotUsed(), failsWith(VdcBllMessages.VDS_GROUP_CANNOT_DO_ACTION_NAME_IN_USE));
    }

    @Test
    public void cpuTypeValidForClusterWithoutVirtService() {
        assertThat(validator.cpuTypeSupportsVirtService(), isValid());
    }

    @Test
    public void cpuTypeSupportsVirtServiceForVirtCluster() {
        when(cluster.supportsVirtService()).thenReturn(true);
        validator = spy(new ClusterValidator(dbFacade, cluster));
        doReturn(true).when(validator).cpuExists();

        assertThat(validator.cpuTypeSupportsVirtService(), isValid());
    }

    @Test
    public void cpuTypeDoesNotSupportVirtServiceForVirtCluster() {
        when(cluster.supportsVirtService()).thenReturn(true);
        validator = spy(new ClusterValidator(dbFacade, cluster));
        doReturn(false).when(validator).cpuExists();

        assertThat(validator.cpuTypeSupportsVirtService(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_CPU_NOT_FOUND));
    }

    @Test
    public void versionSupported() {
        when(cluster.getcompatibility_version()).thenReturn(SUPPORTED_VERSION);

        assertThat(validator.versionSupported(), isValid());
    }

    @Test
    public void versionNotSupported() {
        when(cluster.getcompatibility_version()).thenReturn(mock(Version.class));

        assertThat(validator.versionSupported(), failsWith(VersionSupport.getUnsupportedVersionMessage()));
    }

    @Test
    public void dataCenterVersionValidWhenNotAttachedToDataCenter() {
        assertThat(validator.dataCenterVersionMismatch(), isValid());
    }

    @Test
    public void dataCenterVersionMatches() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        when(cluster.getcompatibility_version()).thenReturn(SUPPORTED_VERSION);
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.getcompatibility_version()).thenReturn(SUPPORTED_VERSION);
        when(dataCenterDao.get(any(Guid.class))).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.dataCenterVersionMismatch(), isValid());
    }

    @Test
    public void dataCenterVersionMismatches() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        when(cluster.getcompatibility_version()).thenReturn(mock(Version.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.getcompatibility_version()).thenReturn(SUPPORTED_VERSION);
        when(dataCenterDao.get(any(Guid.class))).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.dataCenterVersionMismatch(),
                failsWith(VdcBllMessages.VDS_GROUP_CANNOT_ADD_COMPATIBILITY_VERSION_WITH_LOWER_STORAGE_POOL));
    }

    @Test
    public void dataCenterExists() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        when(dataCenterDao.get(any(Guid.class))).thenReturn(mock(StoragePool.class));
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.dataCenterExists(), isValid());
    }

    @Test
    public void dataCenterNotExistsWhenClusterIsOrphan() {
        assertThat(validator.dataCenterExists(), isValid());
    }

    @Test
    public void dataCenterDoesNotExist() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.dataCenterExists(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST));
    }

    @Test
    public void noStoragePoolAttachedToCluster() {
        assertThat(validator.localStoragePoolAttachedToSingleCluster(), isValid());
    }

    @Test
    public void nonLocalStoragePoolAttachedToSingleCluster() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        when(dataCenterDao.get(any(Guid.class))).thenReturn(mock(StoragePool.class));
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.localStoragePoolAttachedToSingleCluster(), isValid());
    }

    @Test
    public void localStoragePoolAttachedToSingleCluster() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(dataCenterDao.get(any(Guid.class))).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        when(clusterDao.getAllForStoragePool(any(Guid.class))).thenReturn(Collections.<VDSGroup> emptyList());
        when(dbFacade.getVdsGroupDao()).thenReturn(clusterDao);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.localStoragePoolAttachedToSingleCluster(), isValid());
    }

    @Test
    public void localStoragePoolAttachedToMultipleClusters() {
        when(cluster.getStoragePoolId()).thenReturn(mock(Guid.class));
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(dataCenterDao.get(any(Guid.class))).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        when(clusterDao.getAllForStoragePool(any(Guid.class))).thenReturn(Collections.<VDSGroup> singletonList(mock(VDSGroup.class)));
        when(dbFacade.getVdsGroupDao()).thenReturn(clusterDao);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.localStoragePoolAttachedToSingleCluster(),
                failsWith(VdcBllMessages.VDS_GROUP_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE));
    }

    @Test
    public void qosBaloonSupportNotRequired() {
        assertThat(validator.qosBaloonSupported(), isValid());
    }

    @Test
    public void qosBaloonSupported() {
        when(cluster.getcompatibility_version()).thenReturn(Version.v3_3);
        when(cluster.isEnableBallooning()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.qosBaloonSupported(), isValid());
    }

    @Test
    public void qosBaloonNotSupported() {
        when(cluster.getcompatibility_version()).thenReturn(mock(Version.class));
        when(cluster.isEnableBallooning()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.qosBaloonSupported(), failsWith(VdcBllMessages.QOS_BALLOON_NOT_SUPPORTED));
    }

    @Test
    public void glusterServiceSupported() {
        when(cluster.getcompatibility_version()).thenReturn(mock(Version.class));
        when(cluster.supportsGlusterService()).thenReturn(true);
        validator = spy(new ClusterValidator(dbFacade, cluster));
        doReturn(true).when(validator).glusterFeatureEnabled();

        assertThat(validator.glusterServiceSupported(), isValid());
    }

    @Test
    public void glusterServiceNotRequired() {
        when(cluster.getcompatibility_version()).thenReturn(mock(Version.class));
        when(cluster.supportsGlusterService()).thenReturn(false);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.glusterServiceSupported(), isValid());
    }

    @Test
    public void glusterServiceNotSupported() {
        when(cluster.getcompatibility_version()).thenReturn(mock(Version.class));
        when(cluster.supportsGlusterService()).thenReturn(true);
        validator = spy(new ClusterValidator(dbFacade, cluster));
        doReturn(false).when(validator).glusterFeatureEnabled();

        assertThat(validator.glusterServiceSupported(),
                failsWith(VdcBllMessages.GLUSTER_NOT_SUPPORTED, "compatibilityVersion", null));
    }

    @Test
    public void clusterServiceDefined() {
        when(cluster.supportsGlusterService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.clusterServiceDefined(), isValid());
    }

    @Test
    public void noClusterServiceDefined() {
        assertThat(validator.clusterServiceDefined(),
                failsWith(VdcBllMessages.VDS_GROUP_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED));
    }

    @Test
    public void mixedClusterServicesSupported() {
        mockConfigRule.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, true);
        when(cluster.supportsGlusterService()).thenReturn(true);
        when(cluster.supportsVirtService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.mixedClusterServicesSupported(), isValid());
    }

    @Test
    public void nonMixedClusterServiceSupported() {
        mockConfigRule.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, false);
        when(cluster.supportsGlusterService()).thenReturn(true);
        when(cluster.supportsVirtService()).thenReturn(false);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.mixedClusterServicesSupported(), isValid());
    }

    @Test
    public void mixedClusterServicesNotSupported() {
        mockConfigRule.mockConfigValue(ConfigValues.AllowClusterWithVirtGlusterEnabled, false);
        when(cluster.supportsGlusterService()).thenReturn(true);
        when(cluster.supportsVirtService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.mixedClusterServicesSupported(),
                failsWith(VdcBllMessages.VDS_GROUP_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED));
    }

    @Test
    public void attestationServerNotConfigured() {
        assertThat(validator.attestationServerConfigured(), isValid());
    }

    @Test
    public void attestationServerConfigured() {
        mockConfigRule.mockConfigValue(ConfigValues.AttestationServer, RandomUtils.instance().nextString(10));
        when(cluster.supportsTrustedService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.attestationServerConfigured(), isValid());
    }

    @Test
    public void attestationServerNotConfiguredProperly() {
        mockConfigRule.mockConfigValue(ConfigValues.AttestationServer, StringUtils.EMPTY);
        when(cluster.supportsTrustedService()).thenReturn(true);
        validator = new ClusterValidator(dbFacade, cluster);

        assertThat(validator.attestationServerConfigured(),
                failsWith(VdcBllMessages.VDS_GROUP_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED));
    }

    @Test
    public void migrationSupported() {
        when(cluster.getcompatibility_version()).thenReturn(mock(Version.class));
        validator = spy(new ClusterValidator(dbFacade, cluster));
        doReturn(true).when(validator).migrationSupportedForArch(any(ArchitectureType.class));

        assertThat(validator.migrationSupported(RandomUtils.instance().nextEnum(ArchitectureType.class)), isValid());
    }

    @Test
    public void migrationNotSupported() {
        when(cluster.getcompatibility_version()).thenReturn(mock(Version.class));
        validator = spy(new ClusterValidator(dbFacade, cluster));
        doReturn(false).when(validator).migrationSupportedForArch(any(ArchitectureType.class));

        assertThat(validator.migrationSupported(RandomUtils.instance().nextEnum(ArchitectureType.class)),
                failsWith(VdcBllMessages.MIGRATION_ON_ERROR_IS_NOT_SUPPORTED));
    }

    @Test
    public void virtIoRngNotRequired() {
        assertThat(validator.virtIoRngSupported(), isValid());
    }

    @Test
    public void virtIoRngSupported() {
        when(cluster.getRequiredRngSources()).thenReturn(Collections.singleton(RandomUtils.instance()
                .nextEnum(Source.class)));
        validator = spy(new ClusterValidator(dbFacade, cluster));
        doReturn(true).when(validator).virtIoRngSupportedInCluster();

        assertThat(validator.virtIoRngSupported(), isValid());
    }

    @Test
    public void virtIoRngNotSupported() {
        when(cluster.getRequiredRngSources()).thenReturn(Collections.singleton(RandomUtils.instance()
                .nextEnum(Source.class)));
        validator = spy(new ClusterValidator(dbFacade, cluster));
        doReturn(false).when(validator).virtIoRngSupportedInCluster();

        assertThat(validator.virtIoRngSupported(),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_RNG_SOURCE_NOT_SUPPORTED));
    }
}
