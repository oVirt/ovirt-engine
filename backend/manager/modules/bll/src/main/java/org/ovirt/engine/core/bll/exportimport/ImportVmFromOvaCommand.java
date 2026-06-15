package org.ovirt.engine.core.bll.exportimport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute.CommandCompensationPhase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmFromOvaParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true, compensationPhase = CommandCompensationPhase.END_COMMAND)
public class ImportVmFromOvaCommand<T extends ImportVmFromOvaParameters> extends ImportVmFromExternalProviderCommand<T> {

    public ImportVmFromOvaCommand(Guid cmdId) {
        super(cmdId);
    }

    public ImportVmFromOvaCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
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

    protected boolean isVirtV2VUsed() {
        return getParameters().getVm().getOrigin() != OriginType.OVIRT;
    }

    @Override
    protected void processImages() {
        // Capture OVF tar names (image.imageId == <File ovf:href>) before super.processImages
        // mints fresh disk ids; persist via imageMappings (command is reloaded before convert()).
        List<Guid> ovfTarImageIds = getVm().getImages().stream()
                .map(DiskImage::getImageId)
                .collect(Collectors.toList());
        super.processImages();
        List<DiskImage> live = getDisks();
        if (live.size() == ovfTarImageIds.size()) {
            Map<Guid, Guid> tarNameByDiskId = new LinkedHashMap<>();
            for (int i = 0; i < live.size(); i++) {
                tarNameByDiskId.put(live.get(i).getId(), ovfTarImageIds.get(i));
            }
            getParameters().setImageMappings(tarNameByDiskId);
        }
    }

    @Override
    protected void convert() {
        prepareForOvaConversion();
        if (isVirtV2VUsed()) {
            runInternalAction(convertOvaActionType(),
                    buildConvertOvaParameters(),
                    createConversionStepContext(StepEnum.CONVERTING_OVA));
        } else {
            runInternalAction(extractOvaActionType(),
                    buildExtractOvaParameters(),
                    createConversionStepContext(StepEnum.EXTRACTING_OVA));
        }
    }

    protected void prepareForOvaConversion() {
    }

    protected ActionType convertOvaActionType() {
        return ActionType.ConvertOva;
    }

    protected ActionType extractOvaActionType() {
        return ActionType.ExtractOva;
    }

    protected ConvertOvaParameters buildConvertOvaParameters() {
        ConvertOvaParameters parameters = new ConvertOvaParameters(getVmId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setVmName(getVmName());
        parameters.setDisks(getDisks());
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
        parameters.setOvaTarNamesByIndex(buildOvaTarNamesByIndex());
        enrichConvertOvaParameters(parameters);
        return parameters;
    }

    protected ConvertOvaParameters buildExtractOvaParameters() {
        ConvertOvaParameters parameters = new ConvertOvaParameters(getVmId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setVmName(getVmName());
        parameters.setDisks(getDisks());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setClusterId(getClusterId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setOvaTarNamesByIndex(buildOvaTarNamesByIndex());
        enrichExtractOvaParameters(parameters);
        return parameters;
    }

    protected void enrichConvertOvaParameters(ConvertOvaParameters parameters) {
    }

    protected void enrichExtractOvaParameters(ConvertOvaParameters parameters) {
    }

    private List<String> buildOvaTarNamesByIndex() {
        Map<Guid, Guid> tarNameByDiskId = getParameters().getImageMappings();
        if (tarNameByDiskId == null || tarNameByDiskId.isEmpty()) {
            return null;
        }
        return getDisks().stream()
                .map(d -> {
                    Guid tarName = tarNameByDiskId.get(d.getId());
                    return tarName != null ? tarName.toString() : null;
                })
                .collect(Collectors.toList());
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
        if (shouldUsePassedDiskAndImageIdsForOvaDiskImport()) {
            parameters.setUsePassedDiskId(true);
            parameters.setUsePassedImageId(true);
        }
        return parameters;
    }

    protected boolean shouldUsePassedDiskAndImageIdsForOvaDiskImport() {
        return true;
    }
}
