package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmFromOvaParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmFromOvaCommand<T extends ImportVmFromOvaParameters> extends ImportVmFromExternalProviderCommand<T> {

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    private VmDeviceUtils vmDeviceUtils;

    public ImportVmFromOvaCommand(Guid cmdId) {
        super(cmdId);
    }

    public ImportVmFromOvaCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void convert() {
        commandCoordinatorUtil.executeAsyncCommand(
                ActionType.ConvertOva,
                buildConvertOvaParameters(),
                cloneContextAndDetachFromParent());
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
        return parameters;
    }

    @Override
    protected void endSuccessfully() {
        if (getParameters().getVm().getOrigin() != OriginType.OVIRT) {
            super.endSuccessfully();
            return;
        }

        // This command uses compensation so if we won't execute the following block in a new
        // transaction then the images might be updated within this transaction scope and block
        // RemoveVm that also tries to update the images later on
        TransactionSupport.executeInNewTransaction(() -> {
            endActionOnDisks();
            return null;
        });
        if (!extractOva()) {
            log.error("Failed to extract OVA file");
            removeVm();
            getReturnValue().setEndActionTryAgain(false);
            return;
        }
        addImportedDevices();
        unlockVm();
        setSucceeded(true);
    }

    private void addImportedDevices() {
        // Disks devices were already added
        getVm().setImages(new ArrayList<>());
        vmDeviceUtils.addImportedDevices(getVm().getStaticData(), getParameters().isImportAsNewEntity(), false);
    }

    @Override
    protected void addNetworkInterfaceDevices() {
        if (getParameters().getVm().getOrigin() != OriginType.OVIRT) {
            super.addNetworkInterfaceDevices();
        }
    }

    private boolean extractOva() {
        return runInternalAction(ActionType.ExtractOva,
                buildExtractOvaParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext()))
                .getSucceeded();
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
    protected AddDiskParameters buildAddDiskParameters(DiskImage image, boolean isBoot) {
        if (getParameters().getVm().getOrigin() != OriginType.OVIRT) {
            return super.buildAddDiskParameters(image, isBoot);
        }

        // The volume format and type is fixed for disks within oVirt's OVA files:
        image.setVolumeFormat(VolumeFormat.COW);
        image.setVolumeType(VolumeType.Sparse);
        AddDiskParameters parameters = super.buildAddDiskParameters(image, isBoot);
        parameters.setUsePassedDiskId(true);
        parameters.setUsePassedImageId(true);
        return parameters;
    }
}
