package org.ovirt.engine.core.bll.network.vm;

import java.util.List;

import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.VnicProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class UpdateVnicProfileCommand<T extends VnicProfileParameters>
        extends VnicProfileCommandBase<T> implements RenamedEntityInfoProvider {

    private VnicProfile oldVnicProfile;

    public UpdateVnicProfileCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        VnicProfileValidator validator = createVnicProfileValidator();
        return validate(validator.vnicProfileIsSet())
                && validate(validator.vnicProfileExists())
                && validate(validator.vnicProfileNameNotUsed())
                && validate(validator.networkNotChanged())
                && validate(validator.networkQosExistsOrNull())
                && validate(validator.portMirroringNotChangedIfUsedByVms())
                && validate(validator.portMirroringNotSetIfExternalNetwork())
                && validator.validateCustomProperties(getReturnValue().getValidationMessages())
                && validate(validator.passthroughNotChangedIfUsedByVms())
                && validate(validator.passthroughProfileContainsSupportedProperties())
                && validate(validator.validNetworkFilterId());
    }

    @Override
    protected void executeCommand() {
        getOldVnicProfile();
        getVnicProfileDao().update(getVnicProfile());
        setSucceeded(true);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.UPDATE_VNIC_PROFILE
                : AuditLogType.UPDATE_VNIC_PROFILE_FAILED;
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.VnicProfile.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return getOldVnicProfile().getName();
    }

    @Override
    public String getEntityNewName() {
        return getVnicProfile().getName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {
    }

    private VnicProfile getOldVnicProfile() {
        if (oldVnicProfile == null) {
            oldVnicProfile = getDbFacade().getVnicProfileDao().get(getVnicProfile().getId());
        }

        return oldVnicProfile;
    }
}
