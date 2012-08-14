package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.action.HotPlugUnplugVmNicParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.vdscommands.HotPlugUnplgNicVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Attach or detach a virtual network interface to the VM in case it is in a valid status. If the VM is down, simply
 * update the device, if it is Up - HotPlug / HotUnPlug the virtual network interface
 */
@NonTransactiveCommandAttribute
public class HotPlugUnplugVmNicCommand<T extends HotPlugUnplugVmNicParameters> extends VmCommand<T> {

    private VmDevice vmDevice;

    public HotPlugUnplugVmNicCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;

        if (isActivateDeactivateAllowedForVmStatus(getVm().getstatus())) {
            setVdsId(getVm().getrun_on_vds().getValue());
            // HotPlug in the host needs to be called only if the Vm is UP
            if (VmHandler.isHotPlugNicAllowedForVmStatus(getVm().getstatus())) {
                returnValue = canPerformHotPlug();
            }
        } else {
            addCanDoActionMessage(VdcBllMessages.HOT_PLUG_NIC_VM_STATUS_ILLEGAL);
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

    @Override
    protected void ExecuteVmCommand() {
        // HotPlug in the host is called only if the Vm is UP
        if (VmHandler.isHotPlugNicAllowedForVmStatus(getVm().getstatus())) {
            runVdsCommand(getParameters().getAction().getCommandType(),
                    new HotPlugUnplgNicVDSParameters(getVm().getrun_on_vds().getValue(),
                            getVm().getId(),
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

}
