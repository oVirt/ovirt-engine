package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.action.gluster.CreateBrickParameters;
import org.ovirt.engine.core.common.businessentities.CacheModeType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.RaidType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.compat.Guid;

public class CreateBrickCommandTest extends BaseCommandTest {

    private final Guid HOST_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    @Mock
    private VDS vds;

    @Mock
    private GlusterUtil glusterUtil;

    @Mock
    private Cluster cluster;

    /**
     * The command under test.
     */
    @InjectMocks
    @Spy
    private CreateBrickCommand cmd = new CreateBrickCommand(new CreateBrickParameters(), null);

    @Test
    public void validateSucceeds() {
        doReturn(new CreateBrickParameters(HOST_ID,
                "brick1",
                "/gluster_bricks/brick1",
                RaidType.RAID0,
                null,
                null, Collections.singletonList(getStorageDevice("sda")),
                getStorageDevice("sdb"),
                CacheModeType.writethrough,
                10))
                .when(cmd).getParameters();
        prepareMocks(VDSStatus.Up);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsForCluster() {
        prepareMocks(VDSStatus.Up);
        mockIsGlusterEnabled(false);
        assertFalse(cmd.validate());

        mockIsGlusterEnabled(true);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsForVdsNonUp() {
        prepareMocks(VDSStatus.Down);
        assertFalse(cmd.validate());

        doReturn(VDSStatus.Error).when(vds).getStatus();
        assertFalse(cmd.validate());

        doReturn(VDSStatus.Maintenance).when(vds).getStatus();
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsForNoStorageDevice() {
        doReturn(new CreateBrickParameters(HOST_ID,
                "brick1",
                "/gluster_bricks/brick1",
                RaidType.RAID0,
                null,
                null, Collections.emptyList(),
                getStorageDevice("sdd"),
                CacheModeType.writethrough,
                10))
                .when(cmd).getParameters();
        prepareMocks(VDSStatus.Up);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsForDeviceAlreadyInUse() {
        StorageDevice storageDevice = getStorageDevice("sda");
        storageDevice.setCanCreateBrick(false);
        doReturn(new CreateBrickParameters(HOST_ID,
                "brick1",
                "/gluster_bricks/brick1",
                RaidType.RAID0,
                null,
                null, Collections.singletonList(storageDevice),
                getStorageDevice("sda"),
                CacheModeType.writethrough,
                10))
                .when(cmd).getParameters();
        prepareMocks(VDSStatus.Up);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsForDifferentStorageDevice() {
        StorageDevice storageDevice1 = getStorageDevice("sda");
        StorageDevice storageDevice2 = getStorageDevice("sdb");
        storageDevice2.setDevType("SDA");

        doReturn(new CreateBrickParameters(HOST_ID,
                "brick1",
                "/gluster_bricks/brick1",
                RaidType.RAID0,
                null,
                null, Arrays.asList(storageDevice1, storageDevice2),
                getStorageDevice("sdb"),
                CacheModeType.writethrough,
                10))
                .when(cmd).getParameters();
        prepareMocks(VDSStatus.Up);
        assertFalse(cmd.validate());
    }

    protected void prepareMocks(VDSStatus status) {
        when(cmd.getCluster()).thenReturn(cluster);
        doReturn(vds).when(cmd).getVds();
        doReturn(status).when(vds).getStatus();
        mockIsGlusterEnabled(true);
    }

    private void mockIsGlusterEnabled(boolean glusterService) {
        when(cluster.supportsGlusterService()).thenReturn(glusterService);
    }

    private StorageDevice getStorageDevice(String name) {
        StorageDevice storageDevice = new StorageDevice();
        storageDevice.setCanCreateBrick(true);
        storageDevice.setDescription("Test Device" + name);
        storageDevice.setDevPath("/dev/" + name);
        storageDevice.setDevType("SCSI");
        storageDevice.setName(name);
        storageDevice.setSize(10000L);
        storageDevice.setId(Guid.newGuid());
        return storageDevice;
    }
}
