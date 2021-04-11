package org.ovirt.engine.core.bll.network.template;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.network.VmNicDao;

public class AddVmTemplateInterfaceCommand<T extends AddVmTemplateInterfaceParameters>
        extends VmTemplateInterfaceCommandBase<T> {

    @Inject
    private VmNicDao vmNicDao;

    public AddVmTemplateInterfaceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        getParameters().getInterface().setVmId(getParameters().getVmTemplateId());
        getParameters().getInterface().setId(Guid.newGuid());
        getParameters().getInterface().setSpeed(
                VmInterfaceType.forValue(
                        getParameters().getInterface().getType()).getSpeed());

        vmNicDao.save(getParameters().getInterface());
        getVmDeviceUtils().addInterface(
                getParameters().getVmTemplateId(),
                getParameters().getInterface().getId(),
                getParameters().getInterface().isPlugged(),
                getParameters().getInterface().isPassthrough());

        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        List<VmNic> interfaces = vmNicDao.getAllForTemplate(getParameters().getVmTemplateId());
        if (!interfaceNameUnique(interfaces)
                || !validate(templateExists())
                || !validate(linkedToTemplate())) {
            return false;
        }

        vmTemplateHandler.updateDisksFromDb(getVmTemplate());

        // not relevant for instance types - will be checked when a VM will be created out of it
        if (getVmTemplate().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            if (!updateVnicForBackwardCompatibility()) {
                return false;
            }

            List<VmNic> interfacesForCheckPciLimit = new ArrayList<>(interfaces);
            interfacesForCheckPciLimit.add(getParameters().getInterface());
            if (!validate(VmValidator.checkPciAndIdeLimit(getVmTemplate().getOsId(),
                    getCluster().getCompatibilityVersion(),
                    getVmTemplate().getNumOfMonitors(),
                    interfacesForCheckPciLimit,
                    getTemplateDiskVmElements(),
                    getVmDeviceUtils().hasVirtioScsiController(getVmTemplate().getId()),
                    getVmDeviceUtils().hasWatchdog(getVmTemplate().getId()),
                    getVmDeviceUtils().hasSoundDevice(getVmTemplate().getId())))) {
                return false;
            }

            Version clusterCompatibilityVersion = getCluster().getCompatibilityVersion();
            VmNicValidator nicValidator = new VmNicValidator(getParameters().getInterface(), clusterCompatibilityVersion, getVmTemplate().getOsId());

            return validate(nicValidator.isCompatibleWithOs())
                    && validate(nicValidator.profileValid(getVmTemplate().getClusterId()))
                    && validate(nicValidator.typeMatchesProfile())
                    && validate(nicValidator.passthroughIsLinked());
        }

        return true;
    }

    private ValidationResult linkedToTemplate() {
        return getParameters().getInterface().getVmId() == null ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.NETWORK_INTERFACE_VM_CANNOT_BE_SET);
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
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
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
