package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("InterfaceName") })
public class AddVmTemplateInterfaceCommand<T extends AddVmTemplateInterfaceParameters> extends VmTemplateCommand<T> {
    public AddVmTemplateInterfaceCommand(T parameters) {
        super(parameters);
    }

    public String getInterfaceName() {
        return getParameters().getInterface().getName();
    }

    @Override
    protected void executeCommand() {
        AddCustomValue("InterfaceType", (VmInterfaceType.forValue(getParameters().getInterface().getType()).getDescription()).toString());
        getParameters().getInterface().setVmTemplateId(getParameters().getVmTemplateId());
        getParameters().getInterface().setId(Guid.NewGuid());
        getParameters().getInterface().setSpeed(
                VmInterfaceType.forValue(
                        getParameters().getInterface().getType()).getSpeed());

        DbFacade.getInstance()
                .getVmNetworkInterfaceDao()
                .save(getParameters().getInterface());
        // \\DbFacade.Instance.addInterfaceStatistics(AddVmTemplateInterfaceParameters.Interface.InterfaceStatistics);

        VmDeviceUtils.addNetworkInterfaceDevice(
                new VmDeviceId(getParameters().getInterface().getId(), getParameters().getVmTemplateId()),
                getParameters().getInterface().isActive());

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao()
                .getAllForTemplate(getParameters().getVmTemplateId());
        if (!VmHandler.IsNotDuplicateInterfaceName(interfaces,
                getParameters().getInterface().getName(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        if (getVmTemplate() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            return false;
        }

        if (getParameters().getInterface().getVmId() != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_VM_CANNOT_BE_SET);
            return false;
        }

        VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
        java.util.ArrayList<VmNetworkInterface> allInterfaces = new java.util.ArrayList<VmNetworkInterface>(interfaces);
        allInterfaces.add(getParameters().getInterface());

        if (!VmCommand.checkPciAndIdeLimit(getVmTemplate().getnum_of_monitors(), allInterfaces,
                new ArrayList<DiskImageBase>(getVmTemplate().getDiskList()), getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        // check that the network exists in current cluster
        List<Network> networks = DbFacade.getInstance().getNetworkDao().getAllForCluster(getVmTemplate().getvds_group_id());
        if (null == LinqUtils.firstOrNull(networks, new Predicate<Network>() {
            @Override
            public boolean eval(Network network) {
                return network.getname().equals(getNetworkName());
            }
        })) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
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
    protected void setActionMessageParameters()
    {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ADD_TEMPLATE_INTERFACE
                : AuditLogType.NETWORK_ADD_TEMPLATE_INTERFACE_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> subjects = super.getPermissionCheckSubjects();

        if (getParameters().getInterface() != null && StringUtils.isNotEmpty(getNetworkName())
                && getVmTemplate() != null) {

            Network network = getNetworkDAO().getByNameAndCluster(getNetworkName(), getVmTemplate().getvds_group_id());
            if (getParameters().getInterface().isPortMirroring()) {
                subjects.add(new PermissionSubject(network == null ? null : network.getId(),
                        VdcObjectType.Network,
                        ActionGroup.PORT_MIRRORING));
            } else {
                subjects.add(new PermissionSubject(network == null ? null : network.getId(),
                        VdcObjectType.Network,
                        getActionType().getActionGroup()));
            }
        }

        return subjects;
    }

    private String getNetworkName() {
        return getParameters().getInterface().getNetworkName();
    }
}
