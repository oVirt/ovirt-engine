package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.ArrayList;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
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

    public UpdateVmInterfaceCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
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
        // LINQ 29456
        // Interface oldIface = interfaces.First(i => i.id ==
        // AddVmInterfaceParameters.Interface.id);
        VmNetworkInterface oldIface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
            @Override
            public boolean eval(VmNetworkInterface i) {
                return i.getId().equals(getParameters().getInterface().getId());
            }
        });
        // LINQ 29456
        if (!StringHelper.EqOp(oldIface.getName(), getParameters().getInterface().getName())) {
            if (!VmHandler.IsNotDuplicateInterfaceName(interfaces,
                         getParameters().getInterface().getName(),
                         getReturnValue().getCanDoActionMessages())) {
                return false;
            }
        }

        // check if user change the mac
        boolean macChanged = false;
        if (!StringHelper.EqOp(oldIface.getMacAddress(), getParameters().getInterface().getMacAddress())) {
            Regex re = new Regex(ValidationUtils.INVALID_NULLABLE_MAC_ADDRESS);
            if (re.IsMatch(getParameters().getInterface().getMacAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INVALID_MAC_ADDRESS);
                return false;
            }

            macChanged = true;
            MacPoolManager.getInstance().freeMac(oldIface.getMacAddress());

            if (!MacPoolManager.getInstance().AddMac(getParameters().getInterface().getMacAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_MAC_ADDRESS_IN_USE);
                return false;
            }
        }

        // check that not exceeded PCI and IDE limit
        java.util.ArrayList<VmNetworkInterface> allInterfaces = new java.util.ArrayList<VmNetworkInterface>(interfaces);
        allInterfaces.remove(oldIface);
        allInterfaces.add(getParameters().getInterface());
        VmStatic vm = DbFacade.getInstance().getVmStaticDAO().get(getParameters().getVmId());
        // LINQ 29456
        // List<DiskImageBase> allDisks =
        // DbFacade.Instance.GetImagesByVmGuid(AddVmInterfaceParameters.VmId).Select(a
        // => (DiskImageBase)a).ToList();
        // if (!CheckPCIAndIDELimit(vm.num_of_monitors, allInterfaces,
        // allDisks))
        // {
        // ReturnValue.CanDoActionMessages.Add(VdcBllMessages.VAR__ACTION__UPDATE.toString());
        // ReturnValue.CanDoActionMessages.Add(VdcBllMessages.VAR__TYPE__INTERFACE.toString());
        // return false;
        // }

        List allDisks = DbFacade.getInstance().getDiskImageDAO().getAllForVm(getParameters().getVmId());
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
            List<VmNetworkInterface> ifaces = new ArrayList<VmNetworkInterface>(interfaces);
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
        // LINQ 29456
        // if (null == null) //LINQ 29456 networks.FirstOrDefault(n => n.name ==
        // AddVmInterfaceParameters.Interface.network_name))
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
        addValidationGroup(UpdateEntity.class);
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
}
