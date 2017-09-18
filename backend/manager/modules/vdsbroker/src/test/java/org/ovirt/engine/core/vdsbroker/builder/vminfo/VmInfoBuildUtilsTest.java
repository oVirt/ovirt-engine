package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils.MB_TO_BYTES;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.di.InjectorRule;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@RunWith(MockitoJUnitRunner.class)
public class VmInfoBuildUtilsTest {

    private static final Guid CLUSTER_ID = Guid.newGuid();

    @ClassRule
    public static InjectorRule injectorRule = new InjectorRule();

    @Mock
    private NetworkDao networkDao;
    @Mock
    private NetworkFilterDao networkFilterDao;
    @Mock
    private NetworkQoSDao networkQosDao;
    @Mock
    private StorageQosDao storageQosDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private VnicProfileDao vnicProfileDao;
    @Mock
    private ClusterFeatureDao clusterFeatureDao;

    @Mock
    private AuditLogDirector auditLogDirector;

    @InjectMocks
    private VmInfoBuildUtils underTest;

    private StorageQos qos;

    private VmDevice vmDevice;

    private DiskImage diskImage = new DiskImage();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.LibgfApiSupported, Version.v4_1, false)
    );

    @Before
    public void setUp() {
        diskImage.setDiskProfileId(Guid.newGuid());

        qos = new StorageQos();
        qos.setId(Guid.newGuid());

        vmDevice = new VmDevice();
    }

    void assertIoTune(Map<String, Long> ioTune,
                      long totalBytesSec, long readBytesSec, long writeBytesSec,
                      long totalIopsSec, long readIopsSec, long writeIopsSec) {
        assertEquals(ioTune.get(VdsProperties.TotalBytesSec).longValue(), totalBytesSec);
        assertEquals(ioTune.get(VdsProperties.ReadBytesSec).longValue(), readBytesSec);
        assertEquals(ioTune.get(VdsProperties.WriteBytesSec).longValue(), writeBytesSec);

        assertEquals(ioTune.get(VdsProperties.TotalIopsSec).longValue(), totalIopsSec);
        assertEquals(ioTune.get(VdsProperties.ReadIopsSec).longValue(), readIopsSec);
        assertEquals(ioTune.get(VdsProperties.WriteIopsSec).longValue(), writeIopsSec);
    }

    @Test
    public void testHandleIoTune() {
        when(storageQosDao.getQosByDiskProfileId(diskImage.getDiskProfileId())).thenReturn(qos);
        qos.setMaxThroughput(100);
        qos.setMaxIops(10000);

        underTest.handleIoTune(vmDevice, underTest.loadStorageQos(diskImage));

        assertIoTune(getIoTune(vmDevice), 100L * MB_TO_BYTES, 0, 0, 10000, 0, 0);
    }

    @Test
    public void testNoStorageQuotaAssigned() {
        underTest.handleIoTune(vmDevice, underTest.loadStorageQos(diskImage));
        assertNull(vmDevice.getSpecParams());
    }

    @Test
    public void testNoCpuProfileAssigned() {
        diskImage.setDiskProfileId(null);
        underTest.handleIoTune(vmDevice, underTest.loadStorageQos(diskImage));
        assertNull(vmDevice.getSpecParams());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> getIoTune(VmDevice vmDevice) {
        return (Map<String, Long>) vmDevice.getSpecParams().get(VdsProperties.Iotune);
    }

    private Set<SupportedAdditionalClusterFeature> getSupportedAdditionalClusterFeatures(Boolean enabled) {
        SupportedAdditionalClusterFeature clusterFeature = new SupportedAdditionalClusterFeature();
        AdditionalFeature feature = new AdditionalFeature(Guid.newGuid(), VmInfoBuildUtils.VDSM_LIBGF_CAP_NAME, Version.v4_1, null, null);
        clusterFeature.setFeature(feature);
        clusterFeature.setEnabled(enabled);
        return Collections.singleton(clusterFeature);
    }

    @Test
    public void testGetNetworkDiskTypeForV41ClusterEnabled() {
        VM vm = new VM();
        vm.setClusterCompatibilityVersion(Version.v4_1);
        vm.setClusterId(CLUSTER_ID);
        doReturn(getSupportedAdditionalClusterFeatures(true)).when(clusterFeatureDao).getSupportedFeaturesByClusterId(CLUSTER_ID);
        assertEquals(VdsProperties.NETWORK, underTest.getNetworkDiskType(vm, StorageType.GLUSTERFS).get());
    }

    @Test
    public void testGetNetworkDiskTypeForV41() {
        VM vm = new VM();
        vm.setClusterCompatibilityVersion(Version.v4_1);
        vm.setClusterId(CLUSTER_ID);
        doReturn(getSupportedAdditionalClusterFeatures(false)).when(clusterFeatureDao).getSupportedFeaturesByClusterId(CLUSTER_ID);
        assertEquals(false, underTest.getNetworkDiskType(vm, StorageType.GLUSTERFS).isPresent());
    }
}
