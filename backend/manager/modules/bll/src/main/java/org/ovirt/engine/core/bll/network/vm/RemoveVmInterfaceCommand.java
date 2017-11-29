package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.ExternalNetworkManagerFactory;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RemoveVmInterfaceCommand<T extends RemoveVmInterfaceParameters> extends VmCommand<T> {

    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private ExternalNetworkManagerFactory externalNetworkManagerFactory;
    @Inject
    private SnapshotsManager snapshotsManager;

    private String interfaceName = "";

    public RemoveVmInterfaceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    protected void executeVmCommand() {
        this.setVmName(vmStaticDao.get(getParameters().getVmId()).getName());
        VmNic iface = vmNicDao.get(getParameters().getInterfaceId());

        if (iface != null) {
            interfaceName = iface.getName();

            // Get Interface type.
            String interType = VmInterfaceType.forValue(iface.getType()).getDescription();
            if (interType != null) {
                addCustomValue("InterfaceType", interType);
            }

            externalNetworkManagerFactory.create(iface).deallocateIfExternal();
        }

        // remove from db
        TransactionSupport.executeInNewTransaction(() -> {
            vmStaticDao.incrementDbGeneration(getParameters().getVmId());
            vmNicDao.remove(getParameters().getInterfaceId());
            vmNetworkStatisticsDao.remove(getParameters().getInterfaceId());
            vmDeviceDao.remove(new VmDeviceId(getParameters().getInterfaceId(), getParameters().getVmId()));

            // return mac to pool
            if (iface != null) {

                String macOfNicBeingRemoved = iface.getMacAddress();
                MacIsNotReservedInSnapshotAndCanBeReleased canBeReleased =
                        new MacIsNotReservedInSnapshotAndCanBeReleased();
                if (canBeReleased.macCanBeReleased(macOfNicBeingRemoved, getVm(), snapshotsManager)) {
                    getMacPool().freeMac(macOfNicBeingRemoved);
                }
            }

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

        VmDynamic vm = vmDynamicDao.get(getParameters().getVmId());
        if (vm.getStatus() != VMStatus.Down && vm.getStatus() != VMStatus.ImageLocked
                && vmDeviceDao
                        .get(new VmDeviceId(getParameters().getInterfaceId(), getParameters().getVmId()))
                        .isPlugged()) {
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

    @Override
    protected boolean shouldUpdateHostedEngineOvf() {
        return true;
    }
}
