package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.UpdateVmNic;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("NetworkName"), @CustomLogField("InterfaceName") })
public class UpdateVmInterfaceCommand<T extends AddVmInterfaceParameters> extends VmCommand<T> {

    private static final long serialVersionUID = -2404956975945588597L;

    public UpdateVmInterfaceCommand(T parameters) {
        super(parameters);
    }

    public String getInterfaceName() {
        return getParameters().getInterface().getName();
    }

    public String getNetworkName() {
        return getParameters().getInterface().getNetworkName();
    }

    @Override
    protected void ExecuteVmCommand() {
        AddCustomValue("InterfaceType", (VmInterfaceType.forValue(getParameters().getInterface().getType()).getInterfaceTranslation()).toString());
        this.setVmName(DbFacade.getInstance().getVmStaticDAO().get(getParameters().getVmId()).getvm_name());

        getParameters().getInterface().setSpeed(
                VmInterfaceType.forValue(
                        getParameters().getInterface().getType()).getSpeed());

        DbFacade.getInstance()
                .getVmNetworkInterfaceDAO()
                .update(getParameters().getInterface());

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {

        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDAO().get(getParameters().getVmId());
        if (vmDynamic.getstatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CHANGE_STATUS_WHEN_NOT_DOWN);
            return false;
        }

        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDAO()
                .getAllForVm(getParameters().getVmId());
        VmNetworkInterface oldIface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
            @Override
            public boolean eval(VmNetworkInterface i) {
                return i.getId().equals(getParameters().getInterface().getId());
            }
        });

        if (!StringHelper.EqOp(oldIface.getName(), getParameters().getInterface().getName())) {
            if (!VmHandler.IsNotDuplicateInterfaceName(interfaces,
                         getParameters().getInterface().getName(),
                         getReturnValue().getCanDoActionMessages())) {
                return false;
            }
        }

        if (!StringHelper.EqOp(oldIface.getMacAddress(), getParameters().getInterface().getMacAddress())) {
            Regex re = new Regex(ValidationUtils.INVALID_NULLABLE_MAC_ADDRESS);
            if (re.IsMatch(getParameters().getInterface().getMacAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INVALID_MAC_ADDRESS);
                return false;
            }

            MacPoolManager.getInstance().freeMac(oldIface.getMacAddress());

            Boolean allowDupMacs = Config.<Boolean> GetValue(ConfigValues.AllowDuplicateMacAddresses);
            if (!MacPoolManager.getInstance().AddMac(getParameters().getInterface().getMacAddress())
                    && !allowDupMacs) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_MAC_ADDRESS_IN_USE);
                return false;
            }
        }

        // check that not exceeded PCI and IDE limit
        java.util.ArrayList<VmNetworkInterface> allInterfaces = new java.util.ArrayList<VmNetworkInterface>(interfaces);
        allInterfaces.remove(oldIface);
        allInterfaces.add(getParameters().getInterface());
        VmStatic vm = DbFacade.getInstance().getVmStaticDAO().get(getParameters().getVmId());

        List<Disk> allDisks = DbFacade.getInstance().getDiskDao().getAllForVm(getParameters().getVmId());
        if (!CheckPCIAndIDELimit(vm.getnum_of_monitors(), allInterfaces, allDisks, getReturnValue().getCanDoActionMessages())) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
            return false;
        }

        // check that the number of interfaces does not exceed limit. Necessary
        // only for versions 2.1, 2.2.
        boolean limitNumOfNics = Config.<Boolean> GetValue(ConfigValues.LimitNumberOfNetworkInterfaces, getVm()
                .getvds_group_compatibility_version().toString());
        if (limitNumOfNics) {
            interfaces.remove(oldIface);
            boolean numOfNicsLegal = validateNumberOfNics(interfaces, getParameters().getInterface());
            interfaces.add(oldIface);
            if (!numOfNicsLegal) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_EXITED_MAX_INTERFACES);
                return false;
            }
        }

        if (getParameters().getInterface().getVmTemplateId() != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_TEMPLATE_CANNOT_BE_SET);
            return false;
        }

        // check that the exists in current cluster
        List<network> networks = DbFacade.getInstance().getNetworkDAO()
                .getAllForCluster(vm.getvds_group_id());
        if (null == LinqUtils.firstOrNull(networks, new Predicate<network>() {
            @Override
            public boolean eval(network n) {
                return n.getname().equals(getParameters().getInterface().getNetworkName());
            }
        })) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
            return false;
        }
        return true;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateVmNic.class);
        return super.getValidationGroups();
    }

    /**
     * Set the parameters for bll messages, such as type and action,
     */
    @Override
    protected void setActionMessageParameters()
    {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_VM_INTERFACE
                : AuditLogType.NETWORK_UPDATE_VM_INTERFACE_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        if (getParameters().getInterface() != null && getVm() != null && getParameters().getInterface().isPortMirroring()) {
            permissionList.add(new PermissionSubject(getVm().getstorage_pool_id(),
                    VdcObjectType.StoragePool,
                    ActionGroup.PORT_MIRRORING));
        }
        return permissionList;
    }
}
