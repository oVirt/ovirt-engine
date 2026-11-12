package org.ovirt.engine.core.bll.exportimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;

@ExtendWith(MockitoExtension.class)
public class OvaImportManagedBlockSupportTest {

    private static final Guid STORAGE_POOL_ID = Guid.newGuid();
    private static final Guid NON_MBS_DOMAIN_ID = Guid.newGuid();
    private static final Guid MBS_DOMAIN_ID = Guid.newGuid();

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;

    private static StorageDomain storageDomain(StorageType type) {
        StorageDomain sd = new StorageDomain();
        sd.setStorageType(type);
        return sd;
    }

    private void mockDomains() {
        when(storageDomainDao.getForStoragePool(NON_MBS_DOMAIN_ID, STORAGE_POOL_ID))
                .thenReturn(storageDomain(StorageType.ISCSI));
        when(storageDomainDao.getForStoragePool(MBS_DOMAIN_ID, STORAGE_POOL_ID))
                .thenReturn(storageDomain(StorageType.MANAGED_BLOCK_STORAGE));
    }

    private static DiskImage imageDisk(boolean active) {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        disk.setImageId(Guid.newGuid());
        disk.setActive(active);
        return disk;
    }

    private static ManagedBlockStorageDisk mbsDisk(Map<String, Object> device) {
        ManagedBlockStorageDisk disk = new ManagedBlockStorageDisk();
        disk.setId(Guid.newGuid());
        disk.setImageId(Guid.newGuid());
        disk.setActive(true);
        disk.setStorageIds(new ArrayList<>(Collections.singletonList(MBS_DOMAIN_ID)));
        disk.setDevice(device);
        return disk;
    }

    @Test
    public void isManagedBlockDestination() {
        assertTrue(OvaImportManagedBlockSupport.isManagedBlockDestination(
                storageDomain(StorageType.MANAGED_BLOCK_STORAGE)));
        assertFalse(OvaImportManagedBlockSupport.isManagedBlockDestination(storageDomain(StorageType.ISCSI)));
        assertFalse(OvaImportManagedBlockSupport.isManagedBlockDestination(null));
    }

    @Test
    public void firstManagedBlockDestinationDomainIdSkipsNonMbsAndEmptyIds() {
        mockDomains();
        Map<Guid, Guid> destinations = new LinkedHashMap<>();
        destinations.put(Guid.newGuid(), Guid.Empty);
        destinations.put(Guid.newGuid(), NON_MBS_DOMAIN_ID);
        destinations.put(Guid.newGuid(), MBS_DOMAIN_ID);

        assertEquals(MBS_DOMAIN_ID, OvaImportManagedBlockSupport.firstManagedBlockDestinationDomainId(
                destinations, STORAGE_POOL_ID, storageDomainDao));
        assertTrue(OvaImportManagedBlockSupport.hasAnyManagedBlockDestination(
                destinations, STORAGE_POOL_ID, storageDomainDao));
    }

    @Test
    public void firstManagedBlockDestinationDomainIdWithoutMbs() {
        Guid unknownDomainId = Guid.newGuid();
        when(storageDomainDao.getForStoragePool(NON_MBS_DOMAIN_ID, STORAGE_POOL_ID))
                .thenReturn(storageDomain(StorageType.ISCSI));
        Map<Guid, Guid> destinations = new LinkedHashMap<>();
        destinations.put(Guid.newGuid(), NON_MBS_DOMAIN_ID);
        destinations.put(Guid.newGuid(), unknownDomainId);

        assertNull(OvaImportManagedBlockSupport.firstManagedBlockDestinationDomainId(
                destinations, STORAGE_POOL_ID, storageDomainDao));
        assertFalse(OvaImportManagedBlockSupport.hasAnyManagedBlockDestination(
                destinations, STORAGE_POOL_ID, storageDomainDao));
    }

    @Test
    public void firstManagedBlockDestinationDomainIdWithEmptyMap() {
        assertNull(OvaImportManagedBlockSupport.firstManagedBlockDestinationDomainId(
                null, STORAGE_POOL_ID, storageDomainDao));
        assertNull(OvaImportManagedBlockSupport.firstManagedBlockDestinationDomainId(
                Collections.emptyMap(), STORAGE_POOL_ID, storageDomainDao));
        verifyNoInteractions(storageDomainDao);
    }

    @Test
    public void resolveDestinationDomainIdUsesKeyMapping() {
        Guid diskId = Guid.newGuid();
        Guid ovfImageId = Guid.newGuid();
        Map<Guid, Guid> destinations = Collections.singletonMap(ovfImageId, MBS_DOMAIN_ID);

        assertEquals(MBS_DOMAIN_ID, OvaImportManagedBlockSupport.resolveDestinationDomainId(
                diskId, id -> diskId.equals(id) ? ovfImageId : null, destinations));
        assertNull(OvaImportManagedBlockSupport.resolveDestinationDomainId(
                Guid.newGuid(), id -> null, destinations));
        assertNull(OvaImportManagedBlockSupport.resolveDestinationDomainId(
                Guid.Empty, Function.identity(), destinations));
        assertNull(OvaImportManagedBlockSupport.resolveDestinationDomainId(
                diskId, Function.identity(), null));
    }

    @Test
    public void diskTargetsManagedBlockStorage() {
        mockDomains();
        DiskImage mbsTarget = imageDisk(true);
        DiskImage iscsiTarget = imageDisk(true);
        DiskImage unmapped = imageDisk(true);
        Map<Guid, Guid> destinations = new HashMap<>();
        destinations.put(mbsTarget.getId(), MBS_DOMAIN_ID);
        destinations.put(iscsiTarget.getId(), NON_MBS_DOMAIN_ID);

        assertTrue(OvaImportManagedBlockSupport.diskTargetsManagedBlockStorage(
                mbsTarget, Function.identity(), destinations, STORAGE_POOL_ID, storageDomainDao));
        assertFalse(OvaImportManagedBlockSupport.diskTargetsManagedBlockStorage(
                iscsiTarget, Function.identity(), destinations, STORAGE_POOL_ID, storageDomainDao));
        assertFalse(OvaImportManagedBlockSupport.diskTargetsManagedBlockStorage(
                unmapped, Function.identity(), destinations, STORAGE_POOL_ID, storageDomainDao));
    }

    @Test
    public void resolveOvaProxyHost() {
        Guid hostId = Guid.newGuid();
        VDS host = new VDS();
        host.setId(hostId);
        when(vdsDao.get(hostId)).thenReturn(host);

        assertSame(host, OvaImportManagedBlockSupport.resolveOvaProxyHost(hostId, vdsDao));
        assertNull(OvaImportManagedBlockSupport.resolveOvaProxyHost(null, vdsDao));
        assertNull(OvaImportManagedBlockSupport.resolveOvaProxyHost(Guid.Empty, vdsDao));
    }

    @Test
    public void resolveOvaProxyHostFailsWhenHostIsMissing() {
        assertThrows(EngineException.class,
                () -> OvaImportManagedBlockSupport.resolveOvaProxyHost(Guid.newGuid(), vdsDao));
    }

    @Test
    public void attachManagedBlockDisksOnProxyAttachesOnlyMbsDisks() {
        VDS host = new VDS();
        Guid vmId = Guid.newGuid();
        ManagedBlockStorageDisk mbs = mbsDisk(null);
        when(managedBlockStorageCommandUtil.attachManagedBlockStorageDisksOnHost(anyCollection(), eq(host), eq(vmId)))
                .thenReturn(true);

        OvaImportManagedBlockSupport.attachManagedBlockDisksOnProxy(
                managedBlockStorageCommandUtil, Arrays.asList(imageDisk(true), mbs), host, vmId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<ManagedBlockStorageDisk>> attached = ArgumentCaptor.forClass(Collection.class);
        verify(managedBlockStorageCommandUtil).attachManagedBlockStorageDisksOnHost(attached.capture(), eq(host),
                eq(vmId));
        assertEquals(Collections.singletonList(mbs), new ArrayList<>(attached.getValue()));
    }

    @Test
    public void attachManagedBlockDisksOnProxyWithoutMbsDisks() {
        OvaImportManagedBlockSupport.attachManagedBlockDisksOnProxy(
                managedBlockStorageCommandUtil, Collections.singletonList(imageDisk(true)), new VDS(), Guid.newGuid());

        verifyNoInteractions(managedBlockStorageCommandUtil);
    }

    @Test
    public void attachManagedBlockDisksOnProxyFailsWhenAttachFails() {
        when(managedBlockStorageCommandUtil.attachManagedBlockStorageDisksOnHost(anyCollection(), any(), any()))
                .thenReturn(false);

        assertThrows(EngineException.class,
                () -> OvaImportManagedBlockSupport.attachManagedBlockDisksOnProxy(
                        managedBlockStorageCommandUtil,
                        Collections.singletonList(mbsDisk(null)),
                        new VDS(),
                        Guid.newGuid()));
    }

    @Test
    public void preAttachedDevicesRoundTrip() {
        Map<String, Object> device = new HashMap<>();
        device.put("path", "/dev/mapper/mbs-volume");
        ManagedBlockStorageDisk attached = mbsDisk(device);
        ManagedBlockStorageDisk notAttached = mbsDisk(null);
        List<Disk> disks = Arrays.asList(attached, notAttached, imageDisk(true));

        Map<Guid, Map<String, Object>> devices =
                OvaImportManagedBlockSupport.preAttachedManagedBlockDevicesByDiskId(disks);

        assertEquals(Collections.singleton(attached.getId()), devices.keySet());
        assertEquals(device, devices.get(attached.getId()));
        assertNotSame(device, devices.get(attached.getId()));

        // the child command reloads the disks without device info
        ManagedBlockStorageDisk reloaded = mbsDisk(null);
        reloaded.setId(attached.getId());
        ManagedBlockStorageDisk otherReloaded = mbsDisk(null);
        OvaImportManagedBlockSupport.applyPreAttachedManagedBlockDevices(
                Arrays.asList(reloaded, otherReloaded), devices);

        assertEquals(device, reloaded.getDevice());
        assertNotSame(devices.get(attached.getId()), reloaded.getDevice());
        assertNull(otherReloaded.getDevice());
    }

    @Test
    public void preAttachedDevicesWithNullInput() {
        assertTrue(OvaImportManagedBlockSupport.preAttachedManagedBlockDevicesByDiskId(null).isEmpty());

        ManagedBlockStorageDisk disk = mbsDisk(null);
        OvaImportManagedBlockSupport.applyPreAttachedManagedBlockDevices(Collections.singletonList(disk), null);
        OvaImportManagedBlockSupport.applyPreAttachedManagedBlockDevices(null, Collections.emptyMap());
        assertNull(disk.getDevice());
    }

    private static class RecordingInvoker implements OvaImportManagedBlockSupport.InternalActionInvoker {
        private final List<ActionType> actions = new ArrayList<>();
        private final List<ActionParametersBase> parameters = new ArrayList<>();

        @Override
        public void invoke(ActionType actionType, ActionParametersBase params, CommandContext commandContext) {
            actions.add(actionType);
            parameters.add(params);
        }
    }

    @Test
    public void removeDisksAfterFailedOvaImportSplitsMbsAndImageDisks() {
        Guid vmId = Guid.newGuid();
        ManagedBlockStorageDisk mbs = mbsDisk(null);
        DiskImage active = imageDisk(true);
        DiskImage snapshot = imageDisk(false);
        RecordingInvoker invoker = new RecordingInvoker();

        OvaImportManagedBlockSupport.removeDisksAfterFailedOvaImport(
                vmId,
                Arrays.asList(mbs, active, snapshot),
                null,
                image -> new RemoveDiskParameters(image.getId()),
                invoker);

        assertEquals(Arrays.asList(ActionType.RemoveManagedBlockStorageDisk, ActionType.RemoveAllVmImages),
                invoker.actions);
        assertEquals(mbs.getId(), ((RemoveDiskParameters) invoker.parameters.get(0)).getDiskId());
        RemoveAllVmImagesParameters removeImages = (RemoveAllVmImagesParameters) invoker.parameters.get(1);
        assertEquals(vmId, removeImages.getVmId());
        assertEquals(Collections.singletonList(active), removeImages.getImages());
    }

    @Test
    public void removeDisksAfterFailedOvaImportWithOnlyMbsDisks() {
        RecordingInvoker invoker = new RecordingInvoker();

        OvaImportManagedBlockSupport.removeDisksAfterFailedOvaImport(
                Guid.newGuid(),
                Arrays.asList(mbsDisk(null), mbsDisk(null)),
                null,
                image -> new RemoveDiskParameters(image.getId()),
                invoker);

        assertEquals(Arrays.asList(ActionType.RemoveManagedBlockStorageDisk,
                ActionType.RemoveManagedBlockStorageDisk), invoker.actions);
    }

    @Test
    public void buildRemoveManagedBlockDiskParametersForTopLevelCommand() {
        ManagedBlockStorageDisk disk = mbsDisk(null);
        ActionParametersBase commandParameters = new ActionParametersBase();
        EntityInfo entityInfo = new EntityInfo(VdcObjectType.VM, Guid.newGuid());
        commandParameters.setEntityInfo(entityInfo);

        RemoveDiskParameters result = OvaImportManagedBlockSupport.buildRemoveManagedBlockDiskParameters(
                disk, false, ActionType.MbsImportVmFromOva, commandParameters);

        assertEquals(disk.getId(), result.getDiskId());
        assertEquals(MBS_DOMAIN_ID, result.getStorageDomainId());
        assertEquals(ActionType.MbsImportVmFromOva, result.getParentCommand());
        assertSame(commandParameters, result.getParentParameters());
        assertSame(entityInfo, result.getEntityInfo());
        assertFalse(result.getForceDelete());
        assertFalse(result.getShouldBeLogged());
        assertEquals(EndProcedure.COMMAND_MANAGED, result.getEndProcedure());
    }

    @Test
    public void buildRemoveManagedBlockDiskParametersForChildCommand() {
        ActionParametersBase parentParameters = new ActionParametersBase();
        ActionParametersBase commandParameters = new ActionParametersBase();
        commandParameters.setParentCommand(ActionType.ImportVmFromOva);
        commandParameters.setParentParameters(parentParameters);

        RemoveDiskParameters result = OvaImportManagedBlockSupport.buildRemoveManagedBlockDiskParameters(
                mbsDisk(null), true, ActionType.MbsImportVmFromOva, commandParameters);

        assertEquals(ActionType.ImportVmFromOva, result.getParentCommand());
        assertSame(parentParameters, result.getParentParameters());
    }
}
