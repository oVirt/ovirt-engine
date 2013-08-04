package org.ovirt.engine.core.bll.network.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class AddVmTemplateInterfaceCommand<T extends AddVmTemplateInterfaceParameters>
        extends VmTemplateInterfaceCommandBase<T> {
    public AddVmTemplateInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getParameters().getInterface().setVmTemplateId(getParameters().getVmTemplateId());
        getParameters().getInterface().setId(Guid.newGuid());
        getParameters().getInterface().setSpeed(
                VmInterfaceType.forValue(
                        getParameters().getInterface().getType()).getSpeed());

        getVmNicDao().save(getParameters().getInterface());
        VmDeviceUtils.addNetworkInterfaceDevice(
                new VmDeviceId(getParameters().getInterface().getId(), getParameters().getVmTemplateId()),
                getParameters().getInterface().isPlugged());

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        List<VmNic> interfaces = getVmNicDao().getAllForTemplate(getParameters().getVmTemplateId());
        if (!interfaceNameUnique(interfaces)
                || !validate(templateExists())
                || !validate(linkedToTemplate())) {
            return false;
        }

        if (!updateVnicForBackwardCompatibility()) {
            return false;
        }

        VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());

        if (!VmCommand.checkPciAndIdeLimit(getVmTemplate().getNumOfMonitors(),
                Collections.<VmNic> singletonList(getParameters().getInterface()),
                new ArrayList<DiskImageBase>(getVmTemplate().getDiskList()), getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        Version clusterCompatibilityVersion = getVdsGroup().getcompatibility_version();
        VmNicValidator nicValidator = new VmNicValidator(getParameters().getInterface(), clusterCompatibilityVersion);

        if (!validate(nicValidator.linkedCorrectly()) || !validate(nicValidator.emptyNetworkValid())) {
            return false;
        }

        if (getParameters().getInterface().getVnicProfileId() != null) {
            // check that the network exists in current cluster
            Network interfaceNetwork =
                    NetworkHelper.getNetworkByVnicProfileId(getParameters().getInterface().getVnicProfileId());

            if (interfaceNetwork == null
                    || !NetworkHelper.isNetworkInCluster(interfaceNetwork, getVmTemplate().getVdsGroupId())) {
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
        VmNetworkInterface nic = getParameters().getInterface();

        if (nic != null && nic.getVnicProfileId() != null && getVmTemplate() != null) {
            subjects.add(new PermissionSubject(nic.getVnicProfileId(),
                    VdcObjectType.VnicProfile,
                    getActionType().getActionGroup()));
        }

        return subjects;
    }
}
