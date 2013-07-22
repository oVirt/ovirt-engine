package org.ovirt.engine.core.bll.network.vm;

import java.util.List;
import java.util.regex.Pattern;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractVmInterfaceCommand<T extends AddVmInterfaceParameters> extends VmCommand<T> {

    public AbstractVmInterfaceCommand(T parameters) {
        super(parameters);
    }

    protected boolean activateOrDeactivateNic(VmNetworkInterface nic, PlugAction plugAction) {
        VdcReturnValueBase returnValue =
                getBackend().runInternalAction(VdcActionType.ActivateDeactivateVmNic,
                        createActivateDeactivateParameters(nic, plugAction));
        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
        }

        return returnValue.getSucceeded();
    }


    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
    }

    private ActivateDeactivateVmNicParameters createActivateDeactivateParameters(VmNetworkInterface nic,
            PlugAction plugAction) {
        ActivateDeactivateVmNicParameters parameters =
                new ActivateDeactivateVmNicParameters(nic, plugAction);
        parameters.setVmId(getParameters().getVmId());

        return parameters;
    }

    private void propagateFailure(VdcReturnValueBase internalReturnValue) {
        getReturnValue().getExecuteFailedMessages().addAll(internalReturnValue.getExecuteFailedMessages());
        getReturnValue().setFault(internalReturnValue.getFault());
        getReturnValue().getCanDoActionMessages().addAll(internalReturnValue.getCanDoActionMessages());
        getReturnValue().setCanDoAction(internalReturnValue.getCanDoAction());
    }

    protected boolean addMacToPool(String macAddress) {
        if (MacPoolManager.getInstance().addMac(macAddress)) {
            return true;
        } else {
            throw new VdcBLLException(VdcBllErrors.MAC_ADDRESS_IS_IN_USE);
        }
    }

    protected ValidationResult macAvailable() {
        Boolean allowDupMacs = Config.<Boolean> GetValue(ConfigValues.AllowDuplicateMacAddresses);
        return MacPoolManager.getInstance().isMacInUse(getMacAddress()) && !allowDupMacs
                ? new ValidationResult(VdcBllMessages.NETWORK_MAC_ADDRESS_IN_USE)
                : ValidationResult.VALID;
    }

    protected ValidationResult macAddressValid() {
        return Pattern.matches(ValidationUtils.INVALID_NULLABLE_MAC_ADDRESS, getMacAddress())
                ? new ValidationResult(VdcBllMessages.NETWORK_INVALID_MAC_ADDRESS)
                : ValidationResult.VALID;
    }

    protected boolean uniqueInterfaceName(List<VmNic> interfaces) {
        return VmHandler.IsNotDuplicateInterfaceName(interfaces, getInterfaceName(),
                getReturnValue().getCanDoActionMessages());
    }

    protected boolean pciAndIdeWithinLimit(VmStatic vm, List<VmNic> allInterfaces) {
        List<Disk> allDisks = getDiskDao().getAllForVm(getVmId());

        return checkPciAndIdeLimit(vm.getNumOfMonitors(), allInterfaces, allDisks,
                getReturnValue().getCanDoActionMessages());
    }

    protected ValidationResult vmTemplateEmpty() {
        return getInterface().getVmTemplateId() != null
                ? new ValidationResult(VdcBllMessages.NETWORK_INTERFACE_TEMPLATE_CANNOT_BE_SET)
                : ValidationResult.VALID;
    }

    protected Network getNetworkFromDb(Guid vdsGroupId, String networkName) {
        if (networkName == null) {
            return null;
        }

        for (Network network : getNetworkDAO().getAllForCluster(vdsGroupId)) {
            if (network.getName().equals(networkName)) {
                return network;
            }
        }
        return null;
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

    public String getNetworkName() {
        return getInterface().getNetworkName();
    }
}
