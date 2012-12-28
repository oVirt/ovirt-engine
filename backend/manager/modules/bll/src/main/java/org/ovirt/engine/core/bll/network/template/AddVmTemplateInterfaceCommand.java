package org.ovirt.engine.core.bll.network.template;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
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
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@SuppressWarnings("serial")
public class AddVmTemplateInterfaceCommand<T extends AddVmTemplateInterfaceParameters>
        extends VmTemplateInterfaceCommandBase<T> {
    public AddVmTemplateInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getParameters().getInterface().setVmTemplateId(getParameters().getVmTemplateId());
        getParameters().getInterface().setId(Guid.NewGuid());
        getParameters().getInterface().setSpeed(
                VmInterfaceType.forValue(
                        getParameters().getInterface().getType()).getSpeed());

        getVmNetworkInterfaceDao().save(getParameters().getInterface());
        VmDeviceUtils.addNetworkInterfaceDevice(
                new VmDeviceId(getParameters().getInterface().getId(), getParameters().getVmTemplateId()),
                getParameters().getInterface().isActive());

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        List<VmNetworkInterface> interfaces =
                getVmNetworkInterfaceDao().getAllForTemplate(getParameters().getVmTemplateId());
        if (!interfaceNameUnique(interfaces)) {
            return false;
        }

        if (!validate(templateExists())) {
            return false;
        }

        if (!validate(linkedToTemplate())) {
            return false;
        }

        VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
        java.util.ArrayList<VmNetworkInterface> allInterfaces = new java.util.ArrayList<VmNetworkInterface>(interfaces);
        allInterfaces.add(getParameters().getInterface());

        if (!VmCommand.checkPciAndIdeLimit(getVmTemplate().getnum_of_monitors(), allInterfaces,
                new ArrayList<DiskImageBase>(getVmTemplate().getDiskList()), getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        String clusterCompatibilityVersion = getVdsGroup().getcompatibility_version().getValue();
        VmNicValidator nicValidator = new VmNicValidator(getParameters().getInterface(), clusterCompatibilityVersion);

        if (!validate(nicValidator.linkedCorrectly()) || !validate(nicValidator.networkNameValid())) {
            return false;
        }

        if (getNetworkName() != null) {
            // check that the network exists in current cluster
            List<Network> networks = getNetworkDAO().getAllForCluster(getVmTemplate().getvds_group_id());
            if (null == LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                @Override
                public boolean eval(Network network) {
                    return network.getname().equals(getNetworkName());
                }
            })) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
                return false;
            }
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
}
