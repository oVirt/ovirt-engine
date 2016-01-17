package org.ovirt.engine.core.bll.provider;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

public class AddProviderCommand<P extends ProviderParameters> extends CommandBase<P> {

    public AddProviderCommand(Guid commandId) {
        super(commandId);
    }

    public AddProviderCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private Provider<?> getProvider() {
        return getParameters().getProvider();
    }

    public String getProviderName() {
        return getProvider().getName();
    }

    private ProviderProxy providerProxy;

    public ProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            providerProxy = ProviderProxyFactory.getInstance().create(getProvider());
        }
        return providerProxy;
    }

    @Override
    protected boolean validate() {
        if (getProvider() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST);
        }
        ProviderProxy providerProxy = getProviderProxy();
        if (providerProxy == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NOT_SUPPORTED,
                    String.format("$providerType %1$s", getProvider().getType()));
        }
        ProviderValidator validator = getProviderProxy().getProviderValidator();
        return validate(validator.nameAvailable()) && validate(validator.validateAddProvider());
    }

    @Override
    protected void executeCommand() {
        getProvider().setId(Guid.newGuid());
        getDbFacade().getProviderDao().save(getProvider());

        ProviderProxy providerProxy = getProviderProxy();
        if (providerProxy != null) {
            providerProxy.onAddition();
        }

        getReturnValue().setActionReturnValue(getProvider().getId());
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
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__PROVIDER);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.PROVIDER_ADDED : AuditLogType.PROVIDER_ADDITION_FAILED;
    }
}
