package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmInterfaceCommand<T extends AddVmInterfaceParameters> extends AbstractVmInterfaceCommand<T> {

    private static final long serialVersionUID = -835005784345476993L;

    public AddVmInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        addCustomValue("InterfaceType",
                (VmInterfaceType.forValue(getInterface().getType()).getDescription()).toString());
        this.setVmName(getVmStaticDAO().get(getParameters().getVmId()).getName());

        boolean succeeded = false;
        boolean macAddedToPool = false;

        try {
            if (StringUtils.isEmpty(getMacAddress())) {
                getInterface().setMacAddress(MacPoolManager.getInstance().allocateNewMac());
                macAddedToPool = true;
            } else {
                macAddedToPool = addMacToPool(getMacAddress());
            }

            getInterface().setSpeed(VmInterfaceType.forValue(getInterface().getType()).getSpeed());
            getInterface().setId(Guid.NewGuid());
            getInterface().setVmId(getParameters().getVmId());

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    addInterfaceToDb(getInterface());
                    addInterfaceDeviceToDb();
                    getCompensationContext().stateChanged();
                    return null;
                }
            });

            if (getInterface().isPlugged()) {
                succeeded = activateOrDeactivateNic(getInterface().getId(), PlugAction.PLUG);
            } else {
                succeeded = true;
            }
        } finally {
            setSucceeded(succeeded);
            if (macAddedToPool && !succeeded) {
                MacPoolManager.getInstance().freeMac(getMacAddress());
            }
        }
    }

    private void addInterfaceDeviceToDb() {
        VmDevice vmDevice = VmDeviceUtils.addNetworkInterfaceDevice(
                new VmDeviceId(getInterface().getId(), getParameters().getVmId()),
                getInterface().isPlugged());
        getCompensationContext().snapshotNewEntity(vmDevice);
    }

    private void addInterfaceToDb(VmNetworkInterface vmNetworkInterface) {
        getVmNetworkInterfaceDao().save(vmNetworkInterface);
        getCompensationContext().snapshotNewEntity(vmNetworkInterface);

        getDbFacade().getVmNetworkStatisticsDao().save(vmNetworkInterface.getStatistics());
        getCompensationContext().snapshotNewEntity(vmNetworkInterface.getStatistics());
    }

    @Override
    protected boolean canDoAction() {
        VmStatic vm = getVm().getStaticData();
        if (vm == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        }

        switch (getVmDynamicDao().get(getParameters().getVmId()).getstatus()) {
        case Up:
        case Down:
        case ImageLocked:
            break;
        default:
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_ADD_INTERFACE_WHEN_VM_STATUS_NOT_UP_DOWN_LOCKED);
            return false;
        }

        List<VmNetworkInterface> interfaces = getVmNetworkInterfaceDao().getAllForVm(getParameters().getVmId());

        if (!uniqueInterfaceName(interfaces)) {
            return false;
        }

        if (!validate(vmTemplateEmpty())) {
            return false;
        }

        // check that not exceeded PCI and IDE limit
        List<VmNetworkInterface> allInterfaces = new ArrayList<VmNetworkInterface>(interfaces);
        allInterfaces.add(getInterface());

        if (!pciAndIdeWithinLimit(vm, allInterfaces)) {
            return false;
        }

        Version compatibilityVersion = getVm().getVdsGroupCompatibilityVersion();
        VmNicValidator nicValidator = new VmNicValidator(getInterface(), compatibilityVersion);

        if (!validate(nicValidator.linkedCorrectly()) || !validate(nicValidator.networkNameValid())
                || !validate(nicValidator.networkProvidedForPortMirroring())) {
            return false;
        }

        if (getNetworkName() != null) {
            // check that the network exists in current cluster
            Network interfaceNetwork = getNetworkFromDb(vm.getVdsGroupId());

            if (interfaceNetwork == null) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
                return false;
            } else if (!interfaceNetwork.isVmNetwork()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NOT_A_VM_NETWORK);
                addCanDoActionMessage(String.format("$networks %1$s", interfaceNetwork.getName()));
                return false;
            }
        }

        if (!StringUtils.isEmpty(getMacAddress())) {
            if (!validate(macAddressValid()) || !validate(macAvailable())) {
                return false;
            }
        } else if (MacPoolManager.getInstance().getAvailableMacsCount() <= 0) // check
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
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ADD_VM_INTERFACE : AuditLogType.NETWORK_ADD_VM_INTERFACE_FAILED;
    }

    /**
     * If the network is set for the interface, it requires permissions on it.<br>
     * If port-mirroring was set for the network, a permissions for the network with port-mirroring is required, else a
     * permission for using the network. The assumption is that port-mirroring already includes the network usage.
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        if (getInterface() != null && StringUtils.isNotEmpty(getNetworkName()) && getVm() != null) {

            Network network = getNetworkDAO().getByNameAndCluster(getNetworkName(), getVm().getVdsGroupId());

            if (getInterface().isPortMirroring()) {
                permissionList.add(new PermissionSubject(network == null ? null : network.getId(),
                        VdcObjectType.Network,
                        ActionGroup.PORT_MIRRORING));
            } else {
                permissionList.add(new PermissionSubject(network == null ? null : network.getId(),
                        VdcObjectType.Network,
                        getActionType().getActionGroup()));
            }
        }
        return permissionList;
    }
}
