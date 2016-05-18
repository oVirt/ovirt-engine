package org.ovirt.engine.core.bll.network.vm;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.MacAddressValidator;
import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;

public abstract class AbstractVmInterfaceCommand<T extends AddVmInterfaceParameters> extends VmCommand<T> {

    public AbstractVmInterfaceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected boolean activateOrDeactivateNewNic(VmNic nic, PlugAction plugAction) {
        return activateOrDeactivateNic(nic, plugAction, true);
    }

    protected boolean activateOrDeactivateExistingNic(VmNic nic, PlugAction plugAction) {
        return activateOrDeactivateNic(nic, plugAction, false);
    }

    private boolean activateOrDeactivateNic(VmNic nic, PlugAction plugAction, boolean newNic) {
        ActivateDeactivateVmNicParameters parameters = new ActivateDeactivateVmNicParameters(nic, plugAction, newNic);
        parameters.setVmId(getParameters().getVmId());

        VdcReturnValueBase returnValue =
                runInternalAction(VdcActionType.ActivateDeactivateVmNic, parameters, cloneContextAndDetachFromParent());
        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
        }

        return returnValue.getSucceeded();
    }

    protected void bumpVmVersion() {
        final VmStatic vmStaticData = getVm().getStaticData();
        getCompensationContext().snapshotEntity(vmStaticData);
        vmStaticDao.incrementDbGeneration(vmStaticData.getId());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__INTERFACE);
    }

    protected boolean addMacToPool(String macAddress) {
        if (getMacPool().addMac(macAddress)) {
            return true;
        } else {
            throw new EngineException(EngineError.MAC_ADDRESS_IS_IN_USE);
        }
    }

    protected ValidationResult macAvailable() {
        return new MacAddressValidator(getMacPool(), getMacAddress()).isMacAssignableValidator();
    }

    protected boolean uniqueInterfaceName(List<VmNic> interfaces) {
        return VmHandler.isNotDuplicateInterfaceName(interfaces, getInterfaceName(),
                getReturnValue().getValidationMessages());
    }

    protected boolean pciAndIdeWithinLimit(VM vm, List<VmNic> allInterfaces) {
        List<DiskVmElement> diskVmElements = getDiskVmElementDao().getAllForVm(getVmId());

        return checkPciAndIdeLimit(vm.getOs(),
                vm.getCompatibilityVersion(),
                vm.getNumOfMonitors(), allInterfaces, diskVmElements,
                VmDeviceUtils.hasVirtioScsiController(getVmId()),
                VmDeviceUtils.hasWatchdog(getVmId()),
                VmDeviceUtils.hasMemoryBalloon(getVmId()),
                VmDeviceUtils.hasSoundDevice(getVmId()),
                getReturnValue().getValidationMessages());
    }

    protected ValidationResult vmTemplateEmpty() {
        return getInterface().getVmTemplateId() != null
                ? new ValidationResult(EngineMessage.NETWORK_INTERFACE_TEMPLATE_CANNOT_BE_SET)
                : ValidationResult.VALID;
    }

    protected boolean updateVnicForBackwardCompatibility(VmNic oldNic) {
        if (!validate(VnicProfileHelper.updateNicForBackwardCompatibility(getParameters().getInterface(),
                oldNic,
                getParameters().getNetworkName(),
                getParameters().isPortMirroring(),
                getVm().getStaticData(),
                getCurrentUser()))) {
            return false;
        }

        return true;
    }

    protected boolean updateVnicForBackwardCompatibility() {
        return updateVnicForBackwardCompatibility(null);
    }

    protected ValidationResult vmStatusLegal(VMStatus status) {
        return status == VMStatus.Up || status == VMStatus.Down || status == VMStatus.ImageLocked
                ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.NETWORK_CANNOT_ADD_INTERFACE_WHEN_VM_STATUS_NOT_UP_DOWN_LOCKED);
    }

    protected String getMacAddress() {
        return getInterface().getMacAddress();
    }

    protected VmNetworkInterface getInterface() {
        return getParameters().getInterface();
    }

    public String getInterfaceName() {
        return getInterface().getName();
    }
}
