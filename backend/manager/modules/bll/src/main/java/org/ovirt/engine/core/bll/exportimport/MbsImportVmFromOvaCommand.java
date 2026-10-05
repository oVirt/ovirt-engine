package org.ovirt.engine.core.bll.exportimport;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute.CommandCompensationPhase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmFromOvaParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDao;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true, compensationPhase = CommandCompensationPhase.END_COMMAND)
public class MbsImportVmFromOvaCommand<T extends ImportVmFromOvaParameters> extends ImportVmFromOvaCommand<T> {

    @Inject
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private DiskDao diskDao;

    public MbsImportVmFromOvaCommand(Guid cmdId) {
        super(cmdId);
    }

    public MbsImportVmFromOvaCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        super.init();
        adjustOvaVirtV2vDisksForManagedBlockDestination();
    }

    private void adjustOvaVirtV2vDisksForManagedBlockDestination() {
        if (CommandActionState.EXECUTE.equals(getActionState())) {
            getVm().getImages().forEach(image -> {
                image.setVolumeFormat(VolumeFormat.RAW);
                image.setVolumeType(VolumeType.Preallocated);
                image.setActualSizeInBytes(image.getSize());
                image.setBackup(DiskBackup.None);
            });
            log.info(
                    "OVA import to managed-block: target disk metadata set to RAW/preallocated for virt-v2v (VM '{}' {}).",
                    getVm().getName(),
                    getVm().getId());
        }
    }

    @Override
    protected boolean setAndValidateDiskProfiles() {
        return true;
    }

    @Override
    protected DiskImage adjustDisk(DiskImage image) {
        DiskImage adjusted = super.adjustDisk(image);
        return ManagedBlockStorageDisk.copyFrom(adjusted);
    }

    @Override
    protected void removeVmImages() {
        OvaImportManagedBlockSupport.removeDisksAfterFailedOvaImport(
                getVmId(),
                diskDao.getAllForVm(getVmId()).stream().map(DiskImage.class::cast).collect(Collectors.toList()),
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
        vmHandler.updateDisksFromDb(getVm());
        OvaImportManagedBlockSupport.attachManagedBlockDisksOnProxy(
                managedBlockStorageCommandUtil,
                getVm().getDiskList(),
                host,
                getVmId());
    }

    @Override
    protected ActionType convertOvaActionType() {
        return ActionType.MbsConvertOva;
    }

    @Override
    protected ActionType extractOvaActionType() {
        return ActionType.MbsExtractOva;
    }

    @Override
    protected void enrichConvertOvaParameters(ConvertOvaParameters parameters) {
        parameters.setPreAttachedManagedBlockDevicesByDiskId(preAttachedManagedBlockDeviceMapForOvaChildCommands());
    }

    @Override
    protected void enrichExtractOvaParameters(ConvertOvaParameters parameters) {
        parameters.setPreAttachedManagedBlockDevicesByDiskId(preAttachedManagedBlockDeviceMapForOvaChildCommands());
    }

    private Map<Guid, Map<String, Object>> preAttachedManagedBlockDeviceMapForOvaChildCommands() {
        if (getVm() == null || getVm().getDiskMap() == null) {
            return Collections.emptyMap();
        }
        return OvaImportManagedBlockSupport.preAttachedManagedBlockDevicesByDiskId(getVm().getDiskMap().values());
    }

    @Override
    protected boolean shouldUsePassedDiskAndImageIdsForOvaDiskImport() {
        return getParameters().isImportAsNewEntity();
    }
}
