package org.ovirt.engine.core.bll.exportimport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute.CommandCompensationPhase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmFromOvaParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDao;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true, compensationPhase = CommandCompensationPhase.END_COMMAND)
public class ImportVmFromOvaCommand<T extends ImportVmFromOvaParameters> extends ImportVmFromExternalProviderCommand<T> {

    @Inject
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private DiskDao diskDao;

    public ImportVmFromOvaCommand(Guid cmdId) {
        super(cmdId);
    }

    public ImportVmFromOvaCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        super.init();
        adjustOvaVirtV2vDisksForManagedBlockDestination();
    }

    private void adjustOvaVirtV2vDisksForManagedBlockDestination() {
        if (CommandActionState.EXECUTE.equals(getActionState())
                && OvaImportManagedBlockSupport.isManagedBlockDestination(getStorageDomain())) {
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
    protected boolean validate() {
        if (getParameters().getProxyHostId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROXY_HOST_MUST_BE_SPECIFIED);
        }

        return super.validate();
    }

    @Override
    protected boolean isHostInSupportedClusterForProxyHost(VDS host) {
        return isVirtV2VUsed() ? super.isHostInSupportedClusterForProxyHost(host) : true;
    }

    private boolean isVirtV2VUsed() {
        return getParameters().getVm().getOrigin() != OriginType.OVIRT;
    }

    private void attachManagedBlockVolumesToProxyHostForOvaConversion() {
        if (!OvaImportManagedBlockSupport.isManagedBlockDestination(getStorageDomain())) {
            return;
        }
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

    private Map<Guid, Guid> buildOvaImageMappingsForConversion() {
        if (!getParameters().isImportAsNewEntity() || newDiskIdForDisk.isEmpty()) {
            return getParameters().getImageMappings();
        }
        Map<Guid, Guid> imageMappings = new HashMap<>();
        for (DiskImage disk : getDisks()) {
            DiskImage old = newDiskIdForDisk.get(disk.getId());
            if (old != null) {
                imageMappings.put(disk.getImageId(), old.getImageId());
            }
        }
        return imageMappings.isEmpty() ? getParameters().getImageMappings() : imageMappings;
    }

    private Map<Guid, Guid> buildOvaSourceImageIdByDiskId() {
        if (!getParameters().isImportAsNewEntity() || newDiskIdForDisk.isEmpty()) {
            return null;
        }
        return OvaImportManagedBlockSupport.ovaSourceImageIdByDiskId(
                getDisks(),
                diskId -> {
                    DiskImage old = newDiskIdForDisk.get(diskId);
                    return old != null ? old.getImageId() : null;
                });
    }

    @Override
    protected void processImages() {
        List<Guid> ovfTarImageIdsInOrder = null;
        if (CommandActionState.EXECUTE.equals(getActionState())
                && !getParameters().isImportAsNewEntity()
                && OvaImportManagedBlockSupport.isManagedBlockDestination(getStorageDomain())
                && getVm().getOrigin() == OriginType.OVIRT) {
            ovfTarImageIdsInOrder =
                    getVm().getImages().stream().map(DiskImage::getImageId).collect(Collectors.toList());
        }
        super.processImages();
        if (CollectionUtils.isNotEmpty(ovfTarImageIdsInOrder)) {
            List<DiskImage> live = getDisks();
            if (live.size() == ovfTarImageIdsInOrder.size()) {
                Map<Guid, Guid> legacy = new HashMap<>();
                for (int i = 0; i < live.size(); i++) {
                    legacy.put(live.get(i).getImageId(), ovfTarImageIdsInOrder.get(i));
                }
                getParameters().setImageMappings(legacy);
            }
        }
    }

    @Override
    protected void convert() {
        attachManagedBlockVolumesToProxyHostForOvaConversion();
        if (isVirtV2VUsed()) {
            runInternalAction(ActionType.ConvertOva,
                    buildConvertOvaParameters(),
                    createConversionStepContext(StepEnum.CONVERTING_OVA));
        } else {
            runInternalAction(ActionType.ExtractOva,
                    buildExtractOvaParameters(),
                    createConversionStepContext(StepEnum.EXTRACTING_OVA));
        }
    }

    private ConvertOvaParameters buildConvertOvaParameters() {
        ConvertOvaParameters parameters = new ConvertOvaParameters(getVmId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setVmName(getVmName());
        parameters.setDisks(getDisks());
        parameters.setImageMappings(buildOvaImageMappingsForConversion());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setClusterId(getClusterId());
        parameters.setVirtioIsoStorageDomainId(getParameters().getVirtioIsoStorageDomainId());
        parameters.setVirtioIsoName(getParameters().getVirtioIsoName());
        parameters.setNetworkInterfaces(getParameters().getVm().getInterfaces());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setPreAttachedManagedBlockDevicesByDiskId(preAttachedManagedBlockDeviceMapForOvaChildCommands());
        parameters.setOvaSourceImageIdByDiskId(buildOvaSourceImageIdByDiskId());
        return parameters;
    }

    private ConvertOvaParameters buildExtractOvaParameters() {
        ConvertOvaParameters parameters = new ConvertOvaParameters(getVmId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setVmName(getVmName());
        parameters.setDisks(getDisks());
        parameters.setImageMappings(buildOvaImageMappingsForConversion());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setClusterId(getClusterId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setPreAttachedManagedBlockDevicesByDiskId(preAttachedManagedBlockDeviceMapForOvaChildCommands());
        parameters.setOvaSourceImageIdByDiskId(buildOvaSourceImageIdByDiskId());
        return parameters;
    }

    private Map<Guid, Map<String, Object>> preAttachedManagedBlockDeviceMapForOvaChildCommands() {
        if (!OvaImportManagedBlockSupport.isManagedBlockDestination(getStorageDomain())) {
            return null;
        }
        return OvaImportManagedBlockSupport.preAttachedManagedBlockDevicesByDiskId(getVm().getDiskMap().values());
    }

    @Override
    protected AddDiskParameters buildAddDiskParameters(DiskImage image) {
        if (getParameters().getVm().getOrigin() != OriginType.OVIRT) {
            // set default value since VirtIO interface doesn't require having an appropriate controller
            // so validation will pass. This will anyway be overridden later by OVF.
            image.getDiskVmElementForVm(getVm().getId()).setDiskInterface(DiskInterface.VirtIO);
            return super.buildAddDiskParameters(image);
        }

        AddDiskParameters parameters = super.buildAddDiskParameters(image);
        if (OvaImportManagedBlockSupport.isManagedBlockDestination(getStorageDomain())
                && !getParameters().isImportAsNewEntity()) {
            return parameters;
        }
        parameters.setUsePassedDiskId(true);
        parameters.setUsePassedImageId(true);
        return parameters;
    }
}
