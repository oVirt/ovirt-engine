package org.ovirt.engine.core.bll.network.vm;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VnicProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

public class AddVnicProfileCommand<T extends VnicProfileParameters> extends VnicProfileCommandBase<T> {

    public AddVnicProfileCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        VnicProfileValidator validator = new VnicProfileValidator(getVnicProfile());
        return validate(validator.vnicProfileIsSet())
                && validate(validator.networkExists())
                && validate(validator.networkQosExistsOrNull())
                && validate(validator.vnicProfileForVmNetworkOnly())
                && validate(validator.vnicProfileNameNotUsed())
                && validate(validator.portMirroringNotSetIfExternalNetwork())
                && validator.validateCustomProperties(getReturnValue().getCanDoActionMessages())
                && validate(validator.passthroughProfileContainsSupportedProperties());
    }

    @Override
    protected void executeCommand() {
        getVnicProfile().setId(Guid.newGuid());
        getVnicProfileDao().save(getVnicProfile());
        NetworkHelper.addPermissionsOnVnicProfile(getCurrentUser().getId(),
                getVnicProfile().getId(),
                getParameters().isPublicUse());
        getReturnValue().setActionReturnValue(getVnicProfile().getId());
        setSucceeded(true);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.ADD_VNIC_PROFILE
                : AuditLogType.ADD_VNIC_PROFILE_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid networkId = getVnicProfile() == null ? null : getVnicProfile().getNetworkId();

        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }
}
