package org.ovirt.engine.core.bll.network.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Version;


public class UpdateVmTemplateInterfaceCommand<T extends AddVmTemplateInterfaceParameters>
        extends VmTemplateInterfaceCommandBase<T> {

    public UpdateVmTemplateInterfaceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        getVmNicDao().update(getParameters().getInterface());
        VmDevice vmDevice =
                getDbFacade().getVmDeviceDao().get(new VmDeviceId(getParameters().getInterface().getId(),
                        getParameters().getVmTemplateId()));
        vmDevice.setIsPlugged(getParameters().getInterface().isPlugged());
        vmDevice.setDevice(getParameters().getInterface().isPassthrough() ? VmDeviceType.HOST_DEVICE.getName()
                : VmDeviceType.BRIDGE.getName());
        getDbFacade().getVmDeviceDao().update(vmDevice);
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (!validate(linkedToTemplate())) {
            return false;
        }

        List<VmNic> interfaces = getVmNicDao().getAllForTemplate(getParameters().getVmTemplateId());
        if (!validate(templateExists())) {
            return false;
        }

        // Interface oldIface = interfaces.First(i => i.id ==
        // AddVmInterfaceParameters.Interface.id);
        VmNic oldIface = interfaces.stream()
                .filter(i -> i.getId().equals(getParameters().getInterface().getId())).findFirst().orElse(null);

        if (oldIface == null) {
            addValidationMessage(EngineMessage.VM_INTERFACE_NOT_EXIST);
            return false;
        }

        if (!updateVnicForBackwardCompatibility(oldIface)) {
            return false;
        }

        // not relevant for instance types - will be checked when a VM will be created out of it
        if (getVmTemplate().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            Version clusterCompatibilityVersion = getCluster().getCompatibilityVersion();
            VmNicValidator nicValidator = new VmNicValidator(getParameters().getInterface(), clusterCompatibilityVersion, getVmTemplate().getOsId());
            if (!validate(nicValidator.isCompatibleWithOs())
                    || !validate(nicValidator.profileValid(getVmTemplate().getClusterId()))
                    || !validate(nicValidator.typeMatchesProfile())
                    || !validate(nicValidator.passthroughIsLinked())) {
                return false;
            }

            if (!checkPciAndIdeLimit(oldIface, new ArrayList<>(interfaces), clusterCompatibilityVersion)) {
                return false;
            }
        }

        if (!StringUtils.equals(oldIface.getName(), getInterfaceName()) && !interfaceNameUnique(interfaces)) {
            return false;
        }

        return true;
    }

    private boolean checkPciAndIdeLimit(VmNic oldIface,
            List<VmNic> interfaces,
            Version clusterCompatibilityVersion) {

        interfaces.remove(oldIface);
        interfaces.add(getParameters().getInterface());

        return VmCommand.checkPciAndIdeLimit(getVmTemplate().getOsId(),
                clusterCompatibilityVersion,
                getVmTemplate().getNumOfMonitors(),
                interfaces,
                getTemplateDiskVmElements(),
                VmDeviceUtils.hasVirtioScsiController(getVmTemplate().getId()),
                VmDeviceUtils.hasWatchdog(getVmTemplate().getId()),
                VmDeviceUtils.hasMemoryBalloon(getVmTemplate().getId()),
                VmDeviceUtils.hasSoundDevice(getVmTemplate().getId()),
                getReturnValue().getValidationMessages());
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
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_TEMPLATE_INTERFACE
                : AuditLogType.NETWORK_UPDATE_TEMPLATE_INTERFACE_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        VmNic nic = getParameters().getInterface();

        if (nic != null && nic.getVnicProfileId() != null && getVmTemplate() != null) {
            VmNic oldNic = getVmNicDao().get(nic.getId());

            if (oldNic == null || isVnicProfileChanged(oldNic, nic)) {
                permissionList.add(new PermissionSubject(nic.getVnicProfileId(),
                        VdcObjectType.VnicProfile,
                        getActionType().getActionGroup()));
            }
        }

        return permissionList;
    }

    private boolean isVnicProfileChanged(VmNic oldNic, VmNic newNic) {
        return !Objects.equals(oldNic.getVnicProfileId(), newNic.getVnicProfileId());
    }
}
