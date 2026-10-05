package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateOvaParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockExecutor;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockReturnValue;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ManagedBlockStorageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DeviceInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

public class CreateOvaCommandTest extends BaseCommandTest {

    @Spy
    @InjectMocks
    private CreateOvaCommand<CreateOvaParameters> command =
            new CreateOvaCommand<>(new CreateOvaParameters(), CommandContext.createContext("test"));

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private ManagedBlockExecutor managedBlockExecutor;

    @Mock
    private ManagedBlockStorageDao managedBlockStorageDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private ImagesHandler imagesHandler;

    @Mock
    private VmDao vmDao;

    @Mock
    private ManagedBlockStorage mbs;

    private final Guid sdId = Guid.newGuid();
    private final Guid vdsId = Guid.newGuid();

    @BeforeEach
    public void setUp() {
        command.setVdsId(vdsId);
        command.setVds(new VDS());
        doNothing().when(command).persistCommandIfNeeded();

        StorageDomain sd = new StorageDomain();
        sd.setStorageType(StorageType.MANAGED_BLOCK_STORAGE);
        when(storageDomainDao.get(sdId)).thenReturn(sd);
        when(managedBlockStorageDao.get(sdId)).thenReturn(mbs);
        when(mbs.getAllDriverOptions()).thenReturn(Map.of());

        doReturn(okAction()).when(command)
                .runInternalAction(eq(ActionType.DisconnectManagedBlockStorageDevice), any());
        doReturn(okAction()).when(command)
                .runInternalAction(eq(ActionType.AnsibleImageMeasure), any(), any());
    }

    @Test
    public void mbsActiveConnectFailsDoesNotDeleteLiveVolumeNorDisconnect() throws Exception {
        addMbsDisk(true);
        stubConnect(false);

        assertThrows(EngineException.class, () -> command.executeCommand());

        // should never delete an active image
        verify(managedBlockExecutor, never())
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.DELETE_VOLUME), any());
        verify(command, never())
                .runInternalAction(eq(ActionType.DisconnectManagedBlockStorageDevice), any());
    }

    @Test
    public void mbsNonActiveConnectFailsDeletesTempVolumeExactlyOnce() throws Exception {
        addMbsDisk(false);
        stubCreateVolume();
        stubConnect(false);

        assertThrows(EngineException.class, () -> command.executeCommand());

        verify(managedBlockExecutor, times(1))
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.DELETE_VOLUME), any());
        verify(command, never())
                .runInternalAction(eq(ActionType.DisconnectManagedBlockStorageDevice), any());
    }

    @Test
    public void mbsActiveAttachFailsDisconnectsButDoesNotDelete() throws Exception {
        addMbsDisk(true);
        stubConnect(true);
        stubAttach(false, null);

        assertThrows(EngineException.class, () -> command.executeCommand());

        verify(command, times(1))
                .runInternalAction(eq(ActionType.DisconnectManagedBlockStorageDevice), any());
        verify(managedBlockExecutor, never())
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.DELETE_VOLUME), any());
    }

    @Test
    public void mbsNonActiveAttachFailsDisconnectsThenDeletesOnce() throws Exception {
        addMbsDisk(false);
        stubCreateVolume();
        stubConnect(true);
        stubAttach(false, null);

        assertThrows(EngineException.class, () -> command.executeCommand());

        InOrder inOrder = inOrder(command, managedBlockExecutor);
        inOrder.verify(command)
                .runInternalAction(eq(ActionType.DisconnectManagedBlockStorageDevice), any());
        inOrder.verify(managedBlockExecutor)
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.DELETE_VOLUME), any());
        verify(managedBlockExecutor, times(1))
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.DELETE_VOLUME), any());
    }

    @Test
    public void mbsActiveHappyPathRecordsPathAndNoTempVolume() throws Exception {
        DiskImage disk = addMbsDisk(true);
        stubConnect(true);
        stubAttach(true, Map.of(DeviceInfoReturn.PATH, "/dev/foo"));

        command.executeCommand();

        assertEquals("/dev/foo", command.getParameters().getDiskIdToPath().get(disk.getId()));
        assertTrue(command.getParameters().getMbsSnapshotImageToTempVolume().isEmpty());
        verify(managedBlockExecutor, never())
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.CREATE_VOLUME_FROM_SNAPSHOT), any());
    }

    @Test
    public void mbsNonActiveHappyPathCreatesVolumeAndRecordsMapping() throws Exception {
        DiskImage disk = addMbsDisk(false);
        Guid tempVolume = Guid.newGuid();
        when(managedBlockExecutor
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.CREATE_VOLUME_FROM_SNAPSHOT), any()))
                .thenReturn(new ManagedBlockReturnValue(0, tempVolume.toString()));
        stubConnect(true);
        stubAttach(true, Map.of(DeviceInfoReturn.PATH, "/dev/bar"));

        command.executeCommand();

        assertEquals(tempVolume,
                command.getParameters().getMbsSnapshotImageToTempVolume().get(disk.getImageId()));
        assertEquals("/dev/bar", command.getParameters().getDiskIdToPath().get(disk.getId()));
    }

    @Test
    public void mbsTeardownActiveDisconnectsButDoesNotDelete() throws Exception {
        addMbsDisk(true);
        teardown();

        verify(command, times(1))
                .runInternalAction(eq(ActionType.DisconnectManagedBlockStorageDevice), any());
        verify(managedBlockExecutor, never())
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.DELETE_VOLUME), any());
    }

    @Test
    public void mbsTeardownNonActiveDisconnectsDeletesAndForgetsTempVolume() throws Exception {
        DiskImage disk = addMbsDisk(false);
        Guid tempVolume = Guid.newGuid();
        command.getParameters().getMbsSnapshotImageToTempVolume().put(disk.getImageId(), tempVolume);

        teardown();

        verify(command, times(1))
                .runInternalAction(eq(ActionType.DisconnectManagedBlockStorageDevice), any());
        verify(managedBlockExecutor, times(1))
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.DELETE_VOLUME), any());
        assertFalse(command.getParameters().getMbsSnapshotImageToTempVolume().containsKey(disk.getImageId()));
    }

    @Test
    public void diskPrepareUsesImagePath() {
        DiskImage disk = addDisk();
        PrepareImageReturn prepared = mock(PrepareImageReturn.class);
        when(prepared.getImagePath()).thenReturn("/dev/img");
        VDSReturnValue result = new VDSReturnValue();
        result.setReturnValue(prepared);
        when(imagesHandler.prepareImage(any(), any(), any(), any(), any())).thenReturn(result);

        command.executeCommand();

        assertEquals("/dev/img", command.getParameters().getDiskIdToPath().get(disk.getId()));
        verify(command, never())
                .runInternalAction(eq(ActionType.ConnectManagedBlockStorageDevice), any());
    }

    @Test
    public void teardownTemplateTearsDownDisk() {
        addDisk();
        teardown();

        verify(imagesHandler, times(1)).teardownImage(any(), any(), any(), any(), any());
    }

    @Test
    public void teardownRunningVmSkipsDisk() {
        addDisk();
        VM vm = new VM();
        vm.setStatus(VMStatus.Up);
        Guid vmId = Guid.newGuid();
        when(vmDao.get(vmId)).thenReturn(vm);
        command.getParameters().setEntityType(VmEntityType.VM);
        command.getParameters().setEntityId(vmId);
        command.getParameters().setPhase(CreateOvaParameters.Phase.PACK_OVA);
        command.performNextOperation(0);

        verify(imagesHandler, never()).teardownImage(any(), any(), any(), any(), any());
    }

    @Test
    public void teardownDownVmTearsDownDisk() {
        addDisk();
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        Guid vmId = Guid.newGuid();
        when(vmDao.get(vmId)).thenReturn(vm);
        command.getParameters().setEntityType(VmEntityType.VM);
        command.getParameters().setEntityId(vmId);
        command.getParameters().setPhase(CreateOvaParameters.Phase.PACK_OVA);
        command.performNextOperation(0);

        verify(imagesHandler, times(1)).teardownImage(any(), any(), any(), any(), any());
    }

    @Test
    public void performNextOperationInTeardownPhaseReturnsFalse() {
        command.getParameters().setPhase(CreateOvaParameters.Phase.TEARDOWN);

        assertFalse(command.performNextOperation(0));
        verify(imagesHandler, never()).teardownImage(any(), any(), any(), any(), any());
    }

    private DiskImage addDisk() {
        Guid imageSdId = Guid.newGuid();
        StorageDomain sd = new StorageDomain();
        sd.setStorageType(StorageType.NFS);
        when(storageDomainDao.get(imageSdId)).thenReturn(sd);
        return addDisk(imageSdId, true);
    }

    private DiskImage addMbsDisk(boolean active) {
        return addDisk(sdId, active);
    }

    private DiskImage addDisk(Guid storageDomainId, boolean active) {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        disk.setImageId(Guid.newGuid());
        disk.setActive(active);
        disk.setStorageIds(List.of(storageDomainId));
        disk.setStoragePoolId(Guid.newGuid());
        command.getParameters().setDisks(List.of(disk));
        command.getParameters().setProxyHostId(vdsId);
        return disk;
    }

    private void teardown() {
        command.getParameters().setEntityType(VmEntityType.TEMPLATE);
        command.getParameters().setPhase(CreateOvaParameters.Phase.PACK_OVA);
        command.performNextOperation(0);
    }

    private void stubConnect(boolean succeeded) {
        ActionReturnValue result = new ActionReturnValue();
        result.setSucceeded(succeeded);
        if (succeeded) {
            result.setActionReturnValue(new HashMap<String, Object>());
        }
        doReturn(result).when(command)
                .runInternalAction(eq(ActionType.ConnectManagedBlockStorageDevice), any());
    }

    private void stubAttach(boolean succeeded, Object returnValue) {
        VDSReturnValue result = new VDSReturnValue();
        result.setSucceeded(succeeded);
        result.setReturnValue(returnValue);
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.AttachManagedBlockStorageVolume), any()))
                .thenReturn(result);
    }

    private void stubCreateVolume() throws Exception {
        when(managedBlockExecutor
                .runCommand(eq(ManagedBlockExecutor.ManagedBlockCommand.CREATE_VOLUME_FROM_SNAPSHOT), any()))
                .thenReturn(new ManagedBlockReturnValue(0, Guid.newGuid().toString()));
    }

    private static ActionReturnValue okAction() {
        ActionReturnValue result = new ActionReturnValue();
        result.setSucceeded(true);
        return result;
    }
}
