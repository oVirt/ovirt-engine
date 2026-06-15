package org.ovirt.engine.core.bll.exportimport;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute.CommandCompensationPhase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromOvaParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDao;

@NonTransactiveCommandAttribute(forceCompensation = true, compensationPhase = CommandCompensationPhase.END_COMMAND)
public class MbsImportVmTemplateFromOvaCommand<T extends ImportVmTemplateFromOvaParameters>
        extends ImportVmTemplateFromOvaCommand<T> {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;

    public MbsImportVmTemplateFromOvaCommand(Guid commandId) {
        super(commandId);
    }

    public MbsImportVmTemplateFromOvaCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean setAndValidateDiskProfiles() {
        return true;
    }

    @Override
    protected DiskImage adjustDisk(DiskImage image) {
        boolean targetsManagedBlock = OvaImportManagedBlockSupport.diskTargetsManagedBlockStorage(
                image,
                diskId -> {
                    Guid k = getOriginalDiskIdMap(diskId);
                    return k != null ? k : diskId;
                },
                getParameters().getImageToDestinationDomainMap(),
                getStoragePoolId(),
                storageDomainDao);
        if (targetsManagedBlock) {
            image.setVolumeFormat(VolumeFormat.RAW);
            image.setVolumeType(VolumeType.Preallocated);
            image.setActualSizeInBytes(image.getSize());
            image.setBackup(DiskBackup.None);
        } else {
            image.setVolumeFormat(VolumeFormat.COW);
            image.setVolumeType(VolumeType.Sparse);
        }
        image.setDiskVmElements(image.getDiskVmElements().stream()
                .map(dve -> DiskVmElement.copyOf(dve, image.getId(), getVmTemplateId()))
                .collect(Collectors.toList()));
        return targetsManagedBlock ? ManagedBlockStorageDisk.copyFrom(image) : image;
    }

    @Override
    protected void removeVmImages() {
        OvaImportManagedBlockSupport.removeDisksAfterFailedOvaImport(
                getVmTemplateId(),
                diskDao.getAllForVm(getVmTemplateId()).stream().map(DiskImage.class::cast).collect(Collectors.toList()),
                cloneContextAndDetachFromParent(),
                image -> OvaImportManagedBlockSupport.buildRemoveManagedBlockDiskParameters(
                        image,
                        isExecutedAsChildCommand(),
                        getActionType(),
                        getParameters()),
                (at, p, c) -> runInternalAction(at, p, c));
    }

    @Override
    protected void prepareForOvaConversion() {
        VDS host = OvaImportManagedBlockSupport.resolveOvaProxyHost(getParameters().getProxyHostId(), vdsDao);
        if (host == null) {
            return;
        }
        vmTemplateHandler.updateDisksFromDb(getVmTemplate());
        OvaImportManagedBlockSupport.attachManagedBlockDisksOnProxy(
                managedBlockStorageCommandUtil,
                getVmTemplate().getDiskList(),
                host,
                getVmTemplateId());
    }

    @Override
    protected ActionType extractOvaActionType() {
        return ActionType.MbsExtractOva;
    }

    @Override
    protected void enrichExtractOvaParameters(ConvertOvaParameters parameters) {
        parameters.setPreAttachedManagedBlockDevicesByDiskId(preAttachedManagedBlockDeviceMapForOvaChildCommands());
        Guid extractStorageDomainId = parameters.getStorageDomainId();
        if (Guid.isNullOrEmpty(extractStorageDomainId)) {
            Guid mbsDest = OvaImportManagedBlockSupport.firstManagedBlockDestinationDomainId(
                    getParameters().getImageToDestinationDomainMap(),
                    getStoragePoolId(),
                    storageDomainDao);
            if (!Guid.isNullOrEmpty(mbsDest)) {
                parameters.setStorageDomainId(mbsDest);
            }
        }
    }

    private Map<Guid, Map<String, Object>> preAttachedManagedBlockDeviceMapForOvaChildCommands() {
        if (getVmTemplate() == null) {
            return Collections.emptyMap();
        }
        return OvaImportManagedBlockSupport.preAttachedManagedBlockDevicesByDiskId(getVmTemplate().getDiskList());
    }
}
