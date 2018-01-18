package org.ovirt.engine.core.bll.exportimport;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmFromOvaParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmFromOvaCommand<T extends ImportVmFromOvaParameters> extends ImportVmFromExternalProviderCommand<T> {

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    public ImportVmFromOvaCommand(Guid cmdId) {
        super(cmdId);
    }

    public ImportVmFromOvaCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void convert() {
        boolean useVirtV2V = getParameters().getVm().getOrigin() != OriginType.OVIRT;
        if (useVirtV2V) {
            commandCoordinatorUtil.executeAsyncCommand(
                    ActionType.ConvertOva,
                    buildConvertOvaParameters(),
                    cloneContextAndDetachFromParent());
        } else {
            commandCoordinatorUtil.executeAsyncCommand(
                    ActionType.ExtractOva,
                    buildExtractOvaParameters(),
                    cloneContextAndDetachFromParent());
        }
    }

    private ConvertOvaParameters buildConvertOvaParameters() {
        ConvertOvaParameters parameters = new ConvertOvaParameters(getVmId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setVmName(getVmName());
        parameters.setDisks(getDisks());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setClusterId(getClusterId());
        parameters.setVirtioIsoName(getParameters().getVirtioIsoName());
        parameters.setNetworkInterfaces(getParameters().getVm().getInterfaces());
        return parameters;
    }

    private ConvertOvaParameters buildExtractOvaParameters() {
        ConvertOvaParameters parameters = new ConvertOvaParameters(getVmId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setVmName(getVmName());
        parameters.setDisks(getDisks());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setClusterId(getClusterId());
        return parameters;
    }

    @Override
    protected AddDiskParameters buildAddDiskParameters(DiskImage image) {
        if (getParameters().getVm().getOrigin() != OriginType.OVIRT) {
            // set default value since VirtIO interface doesn't require having an appropriate controller
            // so validation will pass. This will anyway be overridden later by OVF.
            image.getDiskVmElementForVm(getVm().getId()).setDiskInterface(DiskInterface.VirtIO);
            return super.buildAddDiskParameters(image);
        }

        // The volume format and type is fixed for disks within oVirt's OVA files:
        image.setVolumeFormat(VolumeFormat.COW);
        image.setVolumeType(VolumeType.Sparse);
        AddDiskParameters parameters = super.buildAddDiskParameters(image);
        parameters.setUsePassedDiskId(true);
        parameters.setUsePassedImageId(true);
        return parameters;
    }
}
