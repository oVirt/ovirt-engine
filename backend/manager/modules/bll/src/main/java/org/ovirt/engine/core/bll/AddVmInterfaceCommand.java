package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("InterfaceName") })
public class AddVmInterfaceCommand<T extends AddVmInterfaceParameters> extends VmCommand<T> {

    public AddVmInterfaceCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
    }

    public String getInterfaceName() {
        return getParameters().getInterface().getName();
    }

    @Override
    protected void ExecuteVmCommand() {
        AddCustomValue("InterfaceType",
                (VmInterfaceType.forValue(getParameters().getInterface().getType()).getInterfaceTranslation()).toString());
        this.setVmName(DbFacade.getInstance().getVmStaticDAO().get(getParameters().getVmId()).getvm_name());
        if (StringHelper.isNullOrEmpty(getParameters().getInterface().getMacAddress())) {
            String mac = null;
            RefObject<String> tempRefObject = new RefObject<String>(mac);
            MacPoolManager.getInstance().allocateNewMac(tempRefObject);
            mac = tempRefObject.argvalue;
            getParameters().getInterface().setMacAddress(mac);
        }

        getParameters().getInterface().setSpeed(
                VmInterfaceType.forValue(
                        getParameters().getInterface().getType()).getSpeed());

        getParameters().getInterface().setId(Guid.NewGuid());
        getParameters().getInterface().setVmId(getParameters().getVmId());
        DbFacade.getInstance()
                .getVmNetworkInterfaceDAO()
                .save(getParameters().getInterface());
        DbFacade.getInstance()
                .getVmNetworkStatisticsDAO()
                .save(getParameters().getInterface().getStatistics());

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {

        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDAO().get(getParameters().getVmId());
        if (vmDynamic.getstatus() != VMStatus.Down && vmDynamic.getstatus() != VMStatus.ImageLocked) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CHANGE_STATUS_WHEN_NOT_DOWN);
            return false;
        }

        VmStatic vm = DbFacade.getInstance().getVmStaticDAO().get(getParameters().getVmId());

        if (vm == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        }
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDAO()
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
        java.util.ArrayList<VmNetworkInterface> allInterfaces = new java.util.ArrayList<VmNetworkInterface>(interfaces);
        allInterfaces.add(getParameters().getInterface());
        // LINQ 29456
        // List<DiskImageBase> allDisks =
        // DbFacade.Instance.GetImagesByVmGuid(AddVmInterfaceParameters.VmId).Select(a
        // => (DiskImageBase)a).ToList();
        // if (!CheckPCIAndIDELimit(vm.num_of_monitors, allInterfaces,
        // allDisks))
        // {
        // return false;
        // }

        List<DiskImageBase> allDisks = LinqUtils.foreach(
                DbFacade.getInstance().getDiskImageDAO().getAllForVm(getParameters().getVmId()),
                new Function<DiskImage, DiskImageBase>() {
                    @Override
                    public DiskImageBase eval(DiskImage diskImage) {
                        return diskImage;
                    }
                });
        if (!CheckPCIAndIDELimit(vm.getnum_of_monitors(), allInterfaces, allDisks, getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        // check that the number of interfaces does not exceed limit. Necessary
        // only for version 2.2.
        boolean limitNumOfNics = Config.<Boolean> GetValue(ConfigValues.LimitNumberOfNetworkInterfaces, getVm()
                .getvds_group_compatibility_version().toString());
        if (limitNumOfNics) {
            boolean numOfNicsLegal = validateNumberOfNics(interfaces, getParameters().getInterface());
            if (!numOfNicsLegal) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_EXITED_MAX_INTERFACES);
                return false;
            }
        }

        // this must be the last check because it's add mac to the pool
        if (!StringHelper.isNullOrEmpty(getParameters().getInterface().getMacAddress())) {
            Regex re = new Regex(ValidationUtils.INVALID_NULLABLE_MAC_ADDRESS);
            if (re.IsMatch(getParameters().getInterface().getMacAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INVALID_MAC_ADDRESS);
                return false;
            }

            if (!MacPoolManager.getInstance().AddMac(getParameters().getInterface().getMacAddress())) {
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

        // check that the exists in current cluster
        List<network> networks = DbFacade.getInstance().getNetworkDAO().getAllForCluster(vm.getvds_group_id());
        // LINQ 29456
        // if (null == null) //LINQ 29456 networks.FirstOrDefault(n => n.name ==
        // AddVmInterfaceParameters.Interface.network_name))
        if (null == LinqUtils.firstOrNull(networks, new Predicate<network>() {
            @Override
            public boolean eval(network network) {
                return network.getname().equals(getParameters().getInterface().getNetworkName());
            }
        })) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
            return false;
        }

        return super.canDoAction();
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
    public void Rollback() {
        super.Rollback();
        try {
            MacPoolManager.getInstance().freeMac(getParameters().getInterface().getMacAddress());
        } catch (java.lang.Exception e) {
        }
    }
}
