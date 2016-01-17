package org.ovirt.engine.core.bll.provider;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class UpdateProviderCommand<P extends ProviderParameters> extends CommandBase<P>
        implements RenamedEntityInfoProvider {

    private Provider<?> oldProvider;

    public UpdateProviderCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateProviderCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private Provider<?> getProvider() {
        return getParameters().getProvider();
    }

    private Provider<?> getOldProvider() {
        if (oldProvider == null) {
            oldProvider = getProviderDao().get(getProvider().getId());
        }

        return oldProvider;
    }

    public String getProviderName() {
        return getOldProvider().getName();
    }

    @Override
    protected boolean validate() {
        ProviderValidator validatorOld = new ProviderValidator(getOldProvider());
        ProviderValidator validatorNew = new ProviderValidator(getProvider());
        return validate(validatorOld.providerIsSet())
                && (nameKept() || validate(validatorNew.nameAvailable()))
                && validate(providerTypeNotChanged(getOldProvider(), getProvider()));
    }

    private ValidationResult providerTypeNotChanged(Provider<?> oldProvider, Provider<?> newProvider) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_CHANGE_PROVIDER_TYPE)
                .when(oldProvider.getType() != newProvider.getType());
    }

    private boolean nameKept() {
        return getOldProvider().getName().equals(getProvider().getName());
    }

    @Override
    protected void executeCommand() {
        getProviderDao().update(getProvider());

        ProviderProxy providerProxy = ProviderProxyFactory.getInstance().create(getProvider());
        if (providerProxy != null) {
            providerProxy.onModification();
        }

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__PROVIDER);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.PROVIDER_UPDATED : AuditLogType.PROVIDER_UPDATE_FAILED;
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.PROVIDER.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return getOldProvider().getName();
    }

    @Override
    public String getEntityNewName() {
        return getProvider().getName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {
    }
}
