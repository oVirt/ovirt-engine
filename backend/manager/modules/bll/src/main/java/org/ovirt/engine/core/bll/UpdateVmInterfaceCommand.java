package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.UpdateVmNic;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("NetworkName"), @CustomLogField("InterfaceName") })
public class UpdateVmInterfaceCommand<T extends AddVmInterfaceParameters> extends VmCommand<T> {

    private static final long serialVersionUID = -2404956975945588597L;
    private VmNetworkInterface oldIface;
    private boolean macAddressChanged;

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
    protected void executeVmCommand() {
        AddCustomValue("InterfaceType", (VmInterfaceType.forValue(getParameters().getInterface().getType()).getDescription()).toString());
        this.setVmName(DbFacade.getInstance().getVmStaticDao().get(getParameters().getVmId()).getvm_name());

        getParameters().getInterface().setSpeed(
                VmInterfaceType.forValue(
                        getParameters().getInterface().getType()).getSpeed());

        DbFacade.getInstance()
                .getVmNetworkInterfaceDao()
                .update(getParameters().getInterface());

        if (macAddressChanged) {
            MacPoolManager.getInstance().freeMac(oldIface.getMacAddress());
        }
        setSucceeded(true);
    }

    /**
     * Reverts the MAC addresses status before as were before:
     * <p/>
     * <li>The original MAC address is re-allocated.</li>
     * <li>The new MAC address is freed.</li>
     */
    @Override
    public void rollback() {
        super.rollback();
        if (macAddressChanged) {
            MacPoolManager.getInstance().AddMac(oldIface.getMacAddress());
            if (!Config.<Boolean> GetValue(ConfigValues.AllowDuplicateMacAddresses)) {
                MacPoolManager.getInstance().freeMac(getMacAddress());
            }
        }
    }

    @Override
    protected boolean canDoAction() {

        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(getParameters().getVmId());
        if (vmDynamic.getstatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CHANGE_STATUS_WHEN_NOT_DOWN);
            return false;
        }

        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao()
                .getAllForVm(getParameters().getVmId());
        oldIface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
            @Override
            public boolean eval(VmNetworkInterface i) {
                return i.getId().equals(getParameters().getInterface().getId());
            }
        });

        if (oldIface == null) {
            addCanDoActionMessage(VdcBllMessages.VM_INTERFACE_NOT_EXIST);
            return false;
        }

        if (!StringUtils.equals(oldIface.getName(), getParameters().getInterface().getName())) {
            if (!VmHandler.IsNotDuplicateInterfaceName(interfaces,
                         getParameters().getInterface().getName(),
                         getReturnValue().getCanDoActionMessages())) {
                return false;
            }
        }

        // check that not exceeded PCI and IDE limit
        java.util.ArrayList<VmNetworkInterface> allInterfaces = new java.util.ArrayList<VmNetworkInterface>(interfaces);
        allInterfaces.remove(oldIface);
        allInterfaces.add(getParameters().getInterface());
        VmStatic vm = DbFacade.getInstance().getVmStaticDao().get(getParameters().getVmId());

        List<Disk> allDisks = DbFacade.getInstance().getDiskDao().getAllForVm(getParameters().getVmId());
        if (!checkPciAndIdeLimit(vm.getnum_of_monitors(), allInterfaces, allDisks, getReturnValue().getCanDoActionMessages())) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
            return false;
        }

        if (getParameters().getInterface().getVmTemplateId() != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_TEMPLATE_CANNOT_BE_SET);
            return false;
        }

        // check that the exists in current cluster
        List<Network> networks = DbFacade.getInstance().getNetworkDao()
                .getAllForCluster(vm.getvds_group_id());
        if (null == LinqUtils.firstOrNull(networks, new Predicate<Network>() {
            @Override
            public boolean eval(Network n) {
                return n.getname().equals(getParameters().getInterface().getNetworkName());
            }
        })) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
            return false;
        }

        macAddressChanged = !StringUtils.equals(oldIface.getMacAddress(), getMacAddress());
        if (macAddressChanged) {
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
        }
        return true;
    }

    private String getMacAddress() {
        return getParameters().getInterface().getMacAddress();
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
    protected void setActionMessageParameters() {
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

        if (getParameters().getInterface() != null && StringUtils.isNotEmpty(getNetworkName()) && getVm() != null) {

            VmNetworkInterface iface =
                    getDbFacade().getVmNetworkInterfaceDao().get(getParameters().getInterface().getId());
            if (iface != null) {
                Network network = getNetworkDAO().getByNameAndCluster(getNetworkName(), getVm().getVdsGroupId());

                if (getParameters().getInterface().isPortMirroring()
                        && (isNetworkChanged(iface) || !iface.isPortMirroring())) {
                    permissionList.add(new PermissionSubject(network == null ? null : network.getId(),
                            VdcObjectType.Network,
                            ActionGroup.PORT_MIRRORING));
                } else {
                    // If the vNic's network is changed, the user should have permission for using the new network
                    if (isNetworkChanged(iface)) {
                        permissionList.add(new PermissionSubject(network == null ? null : network.getId(),
                                VdcObjectType.Network,
                                getActionType().getActionGroup()));
                    }
                }
            }
        }
        return permissionList;
    }

    private boolean isNetworkChanged(VmNetworkInterface iface) {
        return !getNetworkName().equals(iface.getNetworkName());
    }
}
