package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * Shared managed-block logic for OVA import (VM and template): proxy attach, device maps for child Convert/Extract
 * commands, per-disk destination checks, and failed-import disk cleanup (MBS vs IRS).
 */
public final class OvaImportManagedBlockSupport {

    private OvaImportManagedBlockSupport() {
    }

    public static boolean isManagedBlockDestination(StorageDomain storageDomain) {
        return storageDomain != null && storageDomain.getStorageType().isManagedBlockStorage();
    }

    public static boolean hasAnyManagedBlockDestination(
            Map<Guid, Guid> imageToDestinationDomainMap,
            Guid storagePoolId,
            StorageDomainDao storageDomainDao) {
        return firstManagedBlockDestinationDomainId(imageToDestinationDomainMap, storagePoolId, storageDomainDao)
                != null;
    }

    public static Guid firstManagedBlockDestinationDomainId(
            Map<Guid, Guid> imageToDestinationDomainMap,
            Guid storagePoolId,
            StorageDomainDao storageDomainDao) {
        if (imageToDestinationDomainMap == null || imageToDestinationDomainMap.isEmpty()) {
            return null;
        }
        for (Guid destId : imageToDestinationDomainMap.values()) {
            if (Guid.isNullOrEmpty(destId)) {
                continue;
            }
            StorageDomain sd = storageDomainDao.getForStoragePool(destId, storagePoolId);
            if (isManagedBlockDestination(sd)) {
                return destId;
            }
        }
        return null;
    }

    public static boolean diskTargetsManagedBlockStorage(
            DiskImage image,
            Function<Guid, Guid> currentDiskIdToKeyForDestMap,
            Map<Guid, Guid> imageToDestinationDomainMap,
            Guid storagePoolId,
            StorageDomainDao storageDomainDao) {
        Guid key = currentDiskIdToKeyForDestMap.apply(image.getId());
        Guid destDomainId = imageToDestinationDomainMap.get(key);
        if (destDomainId == null) {
            return false;
        }
        StorageDomain sd = storageDomainDao.getForStoragePool(destDomainId, storagePoolId);
        return isManagedBlockDestination(sd);
    }

    public static VDS resolveOvaProxyHost(Guid proxyHostId, VdsDao vdsDao) {
        if (Guid.isNullOrEmpty(proxyHostId)) {
            return null;
        }
        VDS host = vdsDao.get(proxyHostId);
        if (host == null) {
            throw new EngineException(EngineError.GeneralException, "Conversion proxy host not found");
        }
        return host;
    }

    public static void attachManagedBlockDisksOnProxy(
            ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil,
            Collection<? extends DiskImage> disks,
            VDS host,
            Guid vmOrTemplateEntityId) {
        List<ManagedBlockStorageDisk> mbs = DisksFilter.filterManagedBlockStorageDisks(disks);
        if (mbs.isEmpty()) {
            return;
        }
        if (!managedBlockStorageCommandUtil.attachManagedBlockStorageDisksOnHost(mbs, host, vmOrTemplateEntityId)) {
            throw new EngineException(
                    EngineError.StorageException,
                    "Failed to connect or attach managed-block volumes on the OVA proxy host");
        }
    }

    public static Map<Guid, Map<String, Object>> preAttachedManagedBlockDevicesByDiskId(Iterable<? extends Disk> disks) {
        Map<Guid, Map<String, Object>> out = new HashMap<>();
        for (Disk d : disks) {
            if (d instanceof ManagedBlockStorageDisk) {
                ManagedBlockStorageDisk m = (ManagedBlockStorageDisk) d;
                if (m.getDevice() != null) {
                    out.put(m.getId(), new HashMap<>(m.getDevice()));
                }
            }
        }
        return out.isEmpty() ? null : out;
    }

    public static Map<Guid, Guid> ovaSourceImageIdByDiskId(
            Iterable<DiskImage> disks,
            Function<Guid, Guid> currentDiskIdToOvaSourceImageId) {
        Map<Guid, Guid> out = new HashMap<>();
        for (DiskImage disk : disks) {
            Guid sourceImageId = currentDiskIdToOvaSourceImageId.apply(disk.getId());
            if (sourceImageId != null) {
                out.put(disk.getId(), sourceImageId);
            }
        }
        return out.isEmpty() ? null : out;
    }

    @FunctionalInterface
    public interface InternalActionInvoker {
        void invoke(ActionType actionType, ActionParametersBase parameters, CommandContext commandContext);
    }

    public static void removeDisksAfterFailedOvaImport(
            Guid vmOrTemplateId,
            List<DiskImage> images,
            CommandContext ctx,
            Function<DiskImage, RemoveDiskParameters> removeManagedBlockParameters,
            InternalActionInvoker invoker) {
        Set<Guid> activeImageIds = new HashSet<>();
        for (DiskImage image : images) {
            if (Boolean.TRUE.equals(image.getActive())) {
                activeImageIds.add(image.getImageId());
            }
        }

        List<DiskImage> nonManagedBlock = new ArrayList<>();
        for (DiskImage image : images) {
            if (!activeImageIds.contains(image.getImageId())) {
                continue;
            }
            if (image.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
                invoker.invoke(
                        ActionType.RemoveManagedBlockStorageDisk,
                        removeManagedBlockParameters.apply(image),
                        ctx);
            } else {
                nonManagedBlock.add(image);
            }
        }

        if (!nonManagedBlock.isEmpty()) {
            invoker.invoke(
                    ActionType.RemoveAllVmImages,
                    new RemoveAllVmImagesParameters(vmOrTemplateId, nonManagedBlock),
                    ctx);
        }
    }

    public static RemoveDiskParameters buildRemoveManagedBlockDiskParameters(
            DiskImage image,
            boolean executedAsChildCommand,
            ActionType commandActionType,
            ActionParametersBase commandParameters) {
        RemoveDiskParameters result = new RemoveDiskParameters(image.getId());
        result.setStorageDomainId(image.getStorageIds().get(0));
        result.setParentCommand(executedAsChildCommand ? commandParameters.getParentCommand() : commandActionType);
        result.setParentParameters(
                executedAsChildCommand ? commandParameters.getParentParameters() : commandParameters);
        result.setEntityInfo(commandParameters.getEntityInfo());
        result.setForceDelete(false);
        result.setShouldBeLogged(false);
        result.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return result;
    }
}
