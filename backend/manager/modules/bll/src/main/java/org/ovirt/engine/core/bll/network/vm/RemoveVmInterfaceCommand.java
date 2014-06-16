package org.ovirt.engine.core.bll.network.vm;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.network.ExternalNetworkManager;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RemoveVmInterfaceCommand<T extends RemoveVmInterfaceParameters> extends VmCommand<T> {

    private String interfaceName = "";

    public RemoveVmInterfaceCommand(T parameters) {
        super(parameters);
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    protected void executeVmCommand() {
        this.setVmName(getVmStaticDAO().get(getParameters().getVmId()).getName());
        VmNic iface = getVmNicDao().get(getParameters().getInterfaceId());

        if (iface != null) {
            interfaceName = iface.getName();

            // Get Interface type.
            String interType = VmInterfaceType.forValue(iface.getType()).getDescription();
            if (interType != null) {
                addCustomValue("InterfaceType", interType);
            }

            new ExternalNetworkManager(iface).deallocateIfExternal();

            // return mac to pool
            MacPoolManager.getInstance().freeMac(iface.getMacAddress());
        }

        // remove from db
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getVmNicDao().remove(getParameters().getInterfaceId());
                getDbFacade().getVmNetworkStatisticsDao().remove(getParameters().getInterfaceId());
                getDbFacade().getVmDeviceDao().remove(new VmDeviceId(getParameters().getInterfaceId(),
                        getParameters().getVmId()));
                setSucceeded(true);
                return null;
            }
        });
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        VmDynamic vm = getVmDynamicDao().get(getParameters().getVmId());
        if (vm.getStatus() != VMStatus.Down && vm.getStatus() != VMStatus.ImageLocked
                && getDbFacade().getVmDeviceDao()
                        .get(new VmDeviceId(getParameters().getInterfaceId(), getParameters().getVmId()))
                        .getIsPlugged()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_ACTIVE_DEVICE);
            return false;
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_VM_INTERFACE
                : AuditLogType.NETWORK_REMOVE_VM_INTERFACE_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
    }
}
