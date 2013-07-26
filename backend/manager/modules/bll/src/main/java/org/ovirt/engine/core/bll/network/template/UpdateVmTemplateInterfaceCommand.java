package org.ovirt.engine.core.bll.network.template;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class UpdateVmTemplateInterfaceCommand<T extends AddVmTemplateInterfaceParameters>
        extends VmTemplateInterfaceCommandBase<T> {
    public UpdateVmTemplateInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getVmNicDao().update(getParameters().getInterface());
        VmDevice vmDevice =
                getDbFacade().getVmDeviceDao().get(new VmDeviceId(getParameters().getInterface().getId(),
                        getParameters().getVmTemplateId()));
        vmDevice.setIsPlugged(getParameters().getInterface().isPlugged());
        getDbFacade().getVmDeviceDao().update(vmDevice);
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        if (!validate(linkedToTemplate())) {
            return false;
        }

        List<VmNic> interfaces = getVmNicDao().getAllForTemplate(getParameters().getVmTemplateId());

        if (!validate(templateExists())) {
            return false;
        }

        // Interface oldIface = interfaces.First(i => i.id ==
        // AddVmInterfaceParameters.Interface.id);
        VmNic oldIface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNic>() {
            @Override
            public boolean eval(VmNic i) {
                return i.getId().equals(getParameters().getInterface().getId());
            }
        });

        Version clusterCompatibilityVersion = getVdsGroup().getcompatibility_version();
        VmNicValidator nicValidator = new VmNicValidator(getParameters().getInterface(), clusterCompatibilityVersion);

        if (!validate(nicValidator.linkedCorrectly()) || !validate(nicValidator.emptyNetworkValid())) {
            return false;
        }

        if (!StringUtils.equals(oldIface.getName(), getInterfaceName()) && !interfaceNameUnique(interfaces)) {
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
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    /**
     * Set the parameters for bll messages, such as type and action,
     */
    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
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
        return !ObjectUtils.equals(oldNic.getVnicProfileId(), newNic.getVnicProfileId());
    }
}
