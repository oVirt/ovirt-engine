package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.HotPlugUnplugVmNicParameters;
import org.ovirt.engine.core.common.PlugAction;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.vdscommands.HotPlugUnplgNicVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Plug or unplug a virtual network interface to the VM either its running or not.
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

        if (getVm().getstatus() == VMStatus.Up) {
            setVdsId(getVm().getrun_on_vds().getValue());
            returnValue = isHotPlugSupported() && isOSSupportingHotPlug();
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
        if (getVm().getstatus() == VMStatus.Up) {
            runVdsCommand(getParameters().getAction().getCommandType(),
                    new HotPlugUnplgNicVDSParameters(getVm().getrun_on_vds().getValue(),
                            getVm().getId(),
                            DbFacade.getInstance().getVmNetworkInterfaceDAO().get(getParameters().getNicId()),
                            vmDevice));
        }
        TransactionSupport.executeInNewTransaction(updateDevice());
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

}
