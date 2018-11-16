package org.ovirt.engine.core.bll.gluster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.utils.GlusterAuditLogUtil;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.StorageDeviceDao;
import org.ovirt.engine.core.utils.ExecutorServiceExtension;

@ExtendWith({MockitoExtension.class, ExecutorServiceExtension.class})
public class StorageDeviceSyncJobTest {
    private static final Guid CLUSTER_GUID = new Guid("CC111111-1111-1111-1111-111111111111");

    private static final Guid HOST_ID_WITH_NEW_DEVICES = new Guid("00000000-0000-0000-0000-000000000000");
    private static final Guid HOST_ID_WITH_DEVICES_DELETED = new Guid("00000000-0000-0000-0000-000000000001");
    private static final Guid HOST_ID_WITH_DEVICES_CHANGED = new Guid("00000000-0000-0000-0000-000000000002");

    private static final Guid DEVICE_WITHOUT_ANYCHANGE = new Guid("00000000-0000-0000-0000-000000000000");
    private static final Guid DEVICE_WITH_CHANGE = new Guid("00000000-0000-0000-0000-000000000001");
    private static final Guid DEVICE_WITH_NAME_CHANGE = new Guid("00000000-0000-0000-0000-000000000002");
    private static final Guid DEVICE_WITH_DEVUUID_BUT_NAME_CHANGED = new Guid("00000000-0000-0000-0000-000000000003");

    @Mock
    private StorageDeviceDao storageDeviceDao;

    @Mock
    private GlusterUtil glusterUtil;

    @Mock
    private ClusterDao clusterDao;

    @InjectMocks
    @Spy
    private StorageDeviceSyncJob syncJob;

    @Mock
    private GlusterAuditLogUtil logUtil;

    @BeforeEach
    public void init() {
        doReturn(getClusters()).when(clusterDao).getAll();
        doReturn(getAllUpServers()).when(glusterUtil).getAllUpServers(CLUSTER_GUID);
        doReturn(getStorageDevices(HOST_ID_WITH_NEW_DEVICES)).when(storageDeviceDao)
                .getStorageDevicesInHost(HOST_ID_WITH_NEW_DEVICES);
        doReturn(getStorageDevices(HOST_ID_WITH_DEVICES_CHANGED)).when(storageDeviceDao)
                .getStorageDevicesInHost(HOST_ID_WITH_DEVICES_CHANGED);
        doReturn(getStorageDevices(HOST_ID_WITH_DEVICES_DELETED)).when(storageDeviceDao)
                .getStorageDevicesInHost(HOST_ID_WITH_DEVICES_DELETED);

    }

    private List<VDS> getAllUpServers() {
        VDS vds1 = new VDS();
        vds1.setId(HOST_ID_WITH_NEW_DEVICES);
        VDS vds2 = new VDS();
        vds2.setId(HOST_ID_WITH_DEVICES_CHANGED);
        VDS vds3 = new VDS();
        vds3.setId(HOST_ID_WITH_DEVICES_DELETED);
        return Arrays.asList(vds1, vds2, vds3);

    }

    private Object getStorageDevicesVDSReturnVal(Guid hostId) {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(true);
        if (HOST_ID_WITH_NEW_DEVICES.equals(hostId)) {
            vdsRetValue.setReturnValue(Arrays.asList(getStorageDevice("sda", null),
                    getStorageDevice("sdb", null),
                    getStorageDevice("sdc", null)));
        } else if (HOST_ID_WITH_DEVICES_CHANGED.equals(hostId)) {
            List<StorageDevice> devices = new ArrayList<>();
            devices.add(getStorageDevice("device-without-anychange", DEVICE_WITHOUT_ANYCHANGE));
            devices.add(getStorageDevice("new-device-with-name-change", DEVICE_WITH_NAME_CHANGE));
            StorageDevice device = getStorageDevice("device-with-change", DEVICE_WITH_CHANGE);
            device.setMountPoint("/temp-mount");
            device.setFsType("XFS");
            device.setSize(12345678L);
            devices.add(device);
            device =
                    getStorageDevice("device-with-devuuid-but-name-changed-1", DEVICE_WITH_DEVUUID_BUT_NAME_CHANGED);
            device.setDevUuid("123456");
            devices.add(device);
            devices.add(getStorageDevice("device-with-devuuid-but-name-changed", null));
            vdsRetValue.setReturnValue(devices);
        } else {
            vdsRetValue.setReturnValue(Collections.emptyList());
        }
        return vdsRetValue;
    }

    private void mockVdsCommand() {
        doReturn(getStorageDevicesVDSReturnVal(HOST_ID_WITH_NEW_DEVICES)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetStorageDeviceList),
                        argThat(isHostMathces(HOST_ID_WITH_NEW_DEVICES)));

        doReturn(getStorageDevicesVDSReturnVal(HOST_ID_WITH_DEVICES_CHANGED)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetStorageDeviceList),
                        argThat(isHostMathces(HOST_ID_WITH_DEVICES_CHANGED)));

        doReturn(getStorageDevicesVDSReturnVal(HOST_ID_WITH_DEVICES_DELETED)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetStorageDeviceList),
                        argThat(isHostMathces(HOST_ID_WITH_DEVICES_DELETED)));

    }

    private ArgumentMatcher<VdsIdVDSCommandParametersBase> isHostMathces(final Guid hostId) {
        return argument -> hostId.equals(argument.getVdsId());
    }

    @Test
    public void testRefreshStorageDevices() {
        mockVdsCommand();
        syncJob.refreshStorageDevices();
        verify(storageDeviceDao, times(5)).save(any());
        verify(storageDeviceDao, times(2)).removeAllInBatch(any());
        verify(storageDeviceDao, times(1)).updateAllInBatch(any());
    }

    private List<Cluster> getClusters() {
        List<Cluster> list = new ArrayList<>();
        list.add(createCluster(Version.v4_3, CLUSTER_GUID));
        return list;
    }

    private Cluster createCluster(Version v, Guid id) {
        Cluster cluster = new Cluster();
        cluster.setId(id);
        cluster.setName("cluster");
        cluster.setGlusterService(true);
        cluster.setVirtService(false);
        cluster.setCompatibilityVersion(v);
        return cluster;
    }

    private StorageDevice getStorageDevice(String name, Guid id) {
        StorageDevice storageDevice = new StorageDevice();
        storageDevice.setCanCreateBrick(true);
        storageDevice.setDescription("Test Device" + name);
        storageDevice.setDevPath("/dev/" + name);
        storageDevice.setDevType("SCSI");
        storageDevice.setName(name);
        storageDevice.setSize(10000L);
        if (id == null) {
            storageDevice.setId(Guid.newGuid());
        } else {
            storageDevice.setId(id);
        }
        return storageDevice;
    }

    private List<StorageDevice> getStorageDevices(Guid hostId) {
        if (HOST_ID_WITH_DEVICES_DELETED.equals(hostId)) {
            return Arrays.asList(getStorageDevice("sda", null),
                    getStorageDevice("sdb", null),
                    getStorageDevice("sdc", null));
        } else if (HOST_ID_WITH_DEVICES_CHANGED.equals(hostId)) {
            List<StorageDevice> deviceList = new ArrayList<>();
            deviceList.add(getStorageDevice("device-without-anychange", DEVICE_WITHOUT_ANYCHANGE));
            deviceList.add(getStorageDevice("device-with-change", DEVICE_WITH_CHANGE));
            deviceList.add(getStorageDevice("device-with-name-change", DEVICE_WITH_NAME_CHANGE));
            StorageDevice device =
                    getStorageDevice("device-with-devuuid-but-name-changed", DEVICE_WITH_DEVUUID_BUT_NAME_CHANGED);
            device.setDevUuid("123456");
            device.setSize(999999L);
            deviceList.add(device);
            return deviceList;
        } else {
            return Collections.emptyList();
        }
    }

}
