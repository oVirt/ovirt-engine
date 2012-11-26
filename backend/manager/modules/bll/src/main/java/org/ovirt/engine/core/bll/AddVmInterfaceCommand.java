package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
@CustomLogFields({ @CustomLogField("InterfaceName") })
public class AddVmInterfaceCommand<T extends AddVmInterfaceParameters> extends VmCommand<T> {

    private static final long serialVersionUID = -835005784345476993L;

    public AddVmInterfaceCommand(T parameters) {
        super(parameters);
    }

    public String getInterfaceName() {
        return getParameters().getInterface().getName();
    }

    @Override
    protected void executeVmCommand() {
        AddCustomValue("InterfaceType",
                (VmInterfaceType.forValue(getParameters().getInterface().getType()).getDescription()).toString());
        this.setVmName(DbFacade.getInstance().getVmStaticDao().get(getParameters().getVmId()).getvm_name());
        if (StringUtils.isEmpty(getMacAddress())) {
            getParameters().getInterface().setMacAddress(MacPoolManager.getInstance().allocateNewMac());
        }

        getParameters().getInterface().setSpeed(
                VmInterfaceType.forValue(
                        getParameters().getInterface().getType()).getSpeed());

        getParameters().getInterface().setId(Guid.NewGuid());
        getParameters().getInterface().setVmId(getParameters().getVmId());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                addInterfaceToDb(getParameters().getInterface());
                addInterfaceDeviceToDb();
                getCompensationContext().stateChanged();
                return null;
            }
        });

        boolean succeeded = true;
        if (getParameters().getInterface().isActive()) {
            succeeded = activateNic();
        }
        setSucceeded(succeeded);
    }

    private void addInterfaceDeviceToDb() {
        VmDevice vmDevice = VmDeviceUtils.addNetworkInterfaceDevice(
                new VmDeviceId(getParameters().getInterface().getId(), getParameters().getVmId()),
                getParameters().getInterface().isActive());
        getCompensationContext().snapshotNewEntity(vmDevice);
    }

    private void addInterfaceToDb(VmNetworkInterface vmNetworkInterface) {
        DbFacade dbFacade = DbFacade.getInstance();

        dbFacade.getVmNetworkInterfaceDao().save(vmNetworkInterface);
        getCompensationContext().snapshotNewEntity(vmNetworkInterface);

        dbFacade.getVmNetworkStatisticsDao().save(vmNetworkInterface.getStatistics());
        getCompensationContext().snapshotNewEntity(vmNetworkInterface.getStatistics());
    }

    private boolean activateNic() {
        ActivateDeactivateVmNicParameters activateParameters = createActivateDeactivateParameters();
        VdcReturnValueBase activateVmNicReturnValue =
                Backend.getInstance().runInternalAction(VdcActionType.ActivateDeactivateVmNic,
                        activateParameters,
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        if (!activateVmNicReturnValue.getSucceeded()) {
            propagateFailure(activateVmNicReturnValue);
        }
        return activateVmNicReturnValue.getSucceeded();
    }

    private ActivateDeactivateVmNicParameters createActivateDeactivateParameters() {
        ActivateDeactivateVmNicParameters activateDeactivateVmNicParameters =
                new ActivateDeactivateVmNicParameters(getParameters().getInterface().getId(), PlugAction.PLUG);
        activateDeactivateVmNicParameters.setVmId(getParameters().getVmId());
        return activateDeactivateVmNicParameters;
    }

    @Override
    protected boolean canDoAction() {
        VmStatic vm = getVm().getStaticData();
        if (vm == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        }

        switch (DbFacade.getInstance().getVmDynamicDao().get(getParameters().getVmId()).getstatus()) {
        case Up:
        case Down:
        case ImageLocked:
            break;
        default:
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_ADD_INTERFACE_WHEN_VM_STATUS_NOT_UP_DOWN_LOCKED);
            return false;
        }

        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao()
                .getAllForVm(getParameters().getVmId());

        if (!VmHandler.IsNotDuplicateInterfaceName(interfaces,
                getParameters().getInterface().getName(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        if (getParameters().getInterface().getVmTemplateId() != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_TEMPLATE_CANNOT_BE_SET);
            return false;
        }

        // check that not exceeded PCI and IDE limit
        List<VmNetworkInterface> allInterfaces = new ArrayList<VmNetworkInterface>(interfaces);
        allInterfaces.add(getParameters().getInterface());

        List<Disk> allDisks = DbFacade.getInstance().getDiskDao().getAllForVm(getParameters().getVmId());
        if (!checkPciAndIdeLimit(vm.getnum_of_monitors(), allInterfaces, allDisks, getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        // check that the exists in current cluster
        List<Network> networks = DbFacade.getInstance().getNetworkDao().getAllForCluster(vm.getvds_group_id());

        Network interfaceNetwork = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
            @Override
            public boolean eval(Network network) {
                return network.getname().equals(getParameters().getInterface().getNetworkName());
            }
        });

        if (interfaceNetwork == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
            return false;
        } else if (!interfaceNetwork.isVmNetwork()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NOT_A_VM_NETWORK);
            addCanDoActionMessage(String.format("$networks %1$s", interfaceNetwork.getname()));
            return false;
        }

        if (!StringUtils.isEmpty(getMacAddress())) {
            Regex re = new Regex(ValidationUtils.INVALID_NULLABLE_MAC_ADDRESS);
            if (re.IsMatch(getMacAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INVALID_MAC_ADDRESS);
                return false;
            }

            Boolean allowDupMacs = Config.<Boolean> GetValue(ConfigValues.AllowDuplicateMacAddresses);
            // this must be the last check because it adds the mac address to the pool
            if (!MacPoolManager.getInstance().AddMac(getMacAddress())
                    && !allowDupMacs) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_MAC_ADDRESS_IN_USE);
                return false;
            }
        } else if (MacPoolManager.getInstance().getavailableMacsCount() <= 0) // check
                                                                              // if
                                                                              // we
                                                                              // have
                                                                              // mac
                                                                              // address
                                                                              // in
                                                                              // pool
        {
            addCanDoActionMessage(VdcBllMessages.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES);
            return false;
        }

        return true;
    }

    private String getMacAddress() {
        return getParameters().getInterface().getMacAddress();
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    /**
     * Set the parameters for bll messages, such as type and action,
     */
    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ADD_VM_INTERFACE : AuditLogType.NETWORK_ADD_VM_INTERFACE_FAILED;
    }

    @Override
    public void rollback() {
        super.rollback();
        try {
            MacPoolManager.getInstance().freeMac(getMacAddress());
        } catch (java.lang.Exception e) {
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        if (getParameters().getInterface() != null && getVm() != null && getParameters().getInterface().isPortMirroring()) {
            permissionList.add(new PermissionSubject(getVm().getStoragePoolId(),
                    VdcObjectType.StoragePool,
                    ActionGroup.PORT_MIRRORING));
        }
        return permissionList;
    }

    private void propagateFailure(VdcReturnValueBase internalReturnValue) {
        getReturnValue().getExecuteFailedMessages().addAll(internalReturnValue.getExecuteFailedMessages());
        getReturnValue().setFault(internalReturnValue.getFault());
        getReturnValue().getCanDoActionMessages().addAll(internalReturnValue.getCanDoActionMessages());
        getReturnValue().setCanDoAction(internalReturnValue.getCanDoAction());
    }
}
