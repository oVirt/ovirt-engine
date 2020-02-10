package org.ovirt.engine.core.bll.provider;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
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
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddProviderCommand<P extends ProviderParameters> extends CommandBase<P> {

    @Inject
    private ProviderDao providerDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    private ProviderProxy<?> providerProxy;

    public AddProviderCommand(Guid commandId) {
        super(commandId);
    }

    public AddProviderCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        if (getProvider() != null) {
            getProvider().setId(Guid.newGuid());
        }
    }

    private Provider<?> getProvider() {
        return getParameters().getProvider();
    }

    public String getProviderName() {
        return getProvider().getName();
    }

    public ProviderProxy<?> getProviderProxy() {
        if (providerProxy == null) {
            providerProxy = providerProxyFactory.create(getProvider());
        }
        return providerProxy;
    }

    @Override
    protected boolean validate() {
        if (getProvider() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST);
        }
        if (getProviderProxy() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NOT_SUPPORTED,
                    String.format("$providerType %1$s", getProvider().getType()));
        }
        ProviderValidator<?> validator = getProviderProxy().getProviderValidator();
        return validate(validator.nameAvailable())
                && validate(validator.validateAuthUrl())
                && validate(validator.validatePassword())
                && validate(validator.validateAddProvider())
                && validate(validator.validateOpenStackImageConstraints());
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(() -> {
            providerDao.save(getProvider());
            getContext().getCompensationContext().snapshotNewEntity(getProvider());
            getContext().getCompensationContext().stateChanged();
            return null;
        });
        ProviderProxy<?> providerProxy = getProviderProxy();
        providerProxy.setCommandContext(getContext());
        providerProxy.onAddition();
        setActionReturnValue(getProvider().getId());
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
