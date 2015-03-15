package org.ovirt.engine.core.bll.network.template;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
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
        if (!super.canDoAction()) {
            return false;
        }

        List<VmNic> interfaces = getVmNicDao().getAllForTemplate(getParameters().getVmTemplateId());
        if (!interfaceNameUnique(interfaces)
                || !validate(templateExists())
                || !validate(linkedToTemplate())) {
            return false;
        }

        VmTemplateHandler.updateDisksFromDb(getVmTemplate());

        // not relevant for instance types - will be checked when a VM will be created out of it
        if (getVmTemplate().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            if (!updateVnicForBackwardCompatibility()) {
                return false;
            }

            List<VmNic> interfacesForCheckPciLimit = new ArrayList<VmNic>(interfaces);
            interfacesForCheckPciLimit.add(getParameters().getInterface());
            if (!VmCommand.checkPciAndIdeLimit(getVmTemplate().getOsId(),
                    getVdsGroup().getCompatibilityVersion(),
                    getVmTemplate().getNumOfMonitors(),
                    interfacesForCheckPciLimit,
                    new ArrayList<DiskImageBase>(getVmTemplate().getDiskList()),
                    VmDeviceUtils.isVirtioScsiControllerAttached(getVmTemplate().getId()),
                    VmDeviceUtils.hasWatchdog(getVmTemplate().getId()),
                    VmDeviceUtils.isBalloonEnabled(getVmTemplate().getId()),
                    VmDeviceUtils.isSoundDeviceEnabled(getVmTemplate().getId()),
                    getReturnValue().getCanDoActionMessages())) {
                return false;
            }

            Version clusterCompatibilityVersion = getVdsGroup().getCompatibilityVersion();
            VmNicValidator nicValidator = new VmNicValidator(getParameters().getInterface(), clusterCompatibilityVersion, getVmTemplate().getOsId());

            if (!validate(nicValidator.linkedCorrectly())
                    || !validate(nicValidator.isCompatibleWithOs())
                    || !validate(nicValidator.emptyNetworkValid())
                    || !validate(nicValidator.profileValid(getVmTemplate().getVdsGroupId()))) {
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
