package org.ovirt.engine.core.bll.network.vm;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.ExternalNetworkManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RemoveVmInterfaceCommand<T extends RemoveVmInterfaceParameters> extends VmCommand<T> {

    private String interfaceName = "";

    public RemoveVmInterfaceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    protected void executeVmCommand() {
        this.setVmName(getVmStaticDao().get(getParameters().getVmId()).getName());
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
            getMacPool().freeMac(iface.getMacAddress());
        }

        // remove from db
        TransactionSupport.executeInNewTransaction(() -> {
            vmStaticDao.incrementDbGeneration(getParameters().getVmId());
            getVmNicDao().remove(getParameters().getInterfaceId());
            getDbFacade().getVmNetworkStatisticsDao().remove(getParameters().getInterfaceId());
            getDbFacade().getVmDeviceDao().remove(new VmDeviceId(getParameters().getInterfaceId(),
                    getParameters().getVmId()));
            setSucceeded(true);
            return null;
        });
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (getVm().isHostedEngine() && !getVm().isManagedHostedEngine()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_UNMANAGED_HOSTED_ENGINE);
        }

        VmDynamic vm = getVmDynamicDao().get(getParameters().getVmId());
        if (vm.getStatus() != VMStatus.Down && vm.getStatus() != VMStatus.ImageLocked
                && getDbFacade().getVmDeviceDao()
                        .get(new VmDeviceId(getParameters().getInterfaceId(), getParameters().getVmId()))
                        .getIsPlugged()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_ACTIVE_DEVICE);
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
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__INTERFACE);
    }
}
