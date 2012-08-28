package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.PlugUnplugVmNicParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.HotPlugUnplgNicVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.InterfaceDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Attach or detach a virtual network interface to the VM in case it is in a valid status. If the VM is down, simply
 * update the device, if it is Up - HotPlug / HotUnPlug the virtual network interface
 */
@NonTransactiveCommandAttribute
public class PlugUnplugVmNicCommand<T extends PlugUnplugVmNicParameters> extends VmCommand<T> {

    private VmDevice vmDevice;

    public PlugUnplugVmNicCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;

        if (isActivateDeactivateAllowedForVmStatus(getVm().getstatus())) {
            // HotPlug in the host needs to be called only if the Vm is UP
            if (VmHandler.isHotPlugNicAllowedForVmStatus(getVm().getstatus())) {
                setVdsId(getVm().getrun_on_vds().getValue());
                returnValue = canPerformHotPlug();
                if (returnValue && !networkAttachedToVds(getNetworkName(), getVdsId())) {
                    addCanDoActionMessage(VdcBllMessages.PLUG_UNPLUG_NETWORK_NOT_IN_VDS);
                    returnValue = false;
                }
            }
        } else {
            addCanDoActionMessage(VdcBllMessages.PLUG_UNPLUG_NIC_VM_STATUS_ILLEGAL);
            returnValue = false;
        }

        if (returnValue) {
            vmDevice = getVmDeviceDao().get(new VmDeviceId(getParameters().getNicId(), getParameters().getVmId()));
            if (vmDevice == null) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.VM_INTERFACE_NOT_EXIST);
            }
        }

        return returnValue;
    }

    private String getNetworkName() {
        VmNetworkInterface vmNetworkInterface = getVmNetworkInterfaceDAO().get(getParameters().getNicId());
        return vmNetworkInterface == null ? null : vmNetworkInterface.getNetworkName();
    }

    @Override
    protected void ExecuteVmCommand() {
        // HotPlug in the host is called only if the Vm is UP
        if (VmHandler.isHotPlugNicAllowedForVmStatus(getVm().getstatus())) {
            runVdsCommand(getParameters().getAction().getCommandType(),
                    new HotPlugUnplgNicVDSParameters(getVdsId(), getVm().getId(),
                            DbFacade.getInstance().getVmNetworkInterfaceDAO().get(getParameters().getNicId()),
                            vmDevice));
        }
        // In any case, the device is updated
        TransactionSupport.executeInNewTransaction(updateDevice());
        VmDeviceUtils.updateBootOrderInVmDevice(getVm().getStaticData());
        setSucceeded(true);
    }

    private TransactionMethod<Void> updateDevice() {
        return new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                vmDevice.setIsPlugged(getParameters().getAction() == PlugAction.PLUG ? true : false);
                getVmDeviceDao().update(vmDevice);
                return null;
            }
        };
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage((getParameters().getAction() == PlugAction.PLUG) ?
                VdcBllMessages.VAR__ACTION__ACTIVATE : VdcBllMessages.VAR__ACTION__DEACTIVATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
    }

    private boolean isActivateDeactivateAllowedForVmStatus(VMStatus vmStatus) {
        return vmStatus == VMStatus.Up || vmStatus == VMStatus.Down;
    }

    private boolean networkAttachedToVds(String networkName, Guid vdsId) {
        if (networkName != null) {
            List<VdsNetworkInterface> listOfInterfaces = getInterfaceDAO().getAllInterfacesForVds(vdsId);
            for (VdsNetworkInterface vdsNetworkInterface : listOfInterfaces) {
                if (networkName.equals(vdsNetworkInterface.getNetworkName())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected InterfaceDAO getInterfaceDAO() {
        return getDbFacade().getInterfaceDAO();
    }
}
