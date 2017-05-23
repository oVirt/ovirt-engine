package org.ovirt.engine.core.bll.provider.network;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExternalSubnetParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.provider.ProviderDao;

@NonTransactiveCommandAttribute
public class RemoveSubnetFromProviderCommand<T extends ExternalSubnetParameters> extends CommandBase<T> {

    @Inject
    private ProviderDao providerDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    private Provider<?> provider;

    public RemoveSubnetFromProviderCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private Provider<?> getProvider() {
        if (provider == null) {
            provider = providerDao.get(getSubnet().getExternalNetwork().getProviderId());
        }

        return provider;
    }

    public String getProviderName() {
        return getProvider().getName();
    }

    private ExternalSubnet getSubnet() {
        return getParameters().getSubnet();
    }

    public String getSubnetName() {
        return getSubnet().getName();
    }

    @Override
    protected boolean validate() {
        ProviderValidator validator = new ProviderValidator(getProvider());

        return validate(validator.providerIsSet()) && validate(validator.validateReadOnlyActions());
    }

    @Override
    protected void executeCommand() {
        NetworkProviderProxy proxy = providerProxyFactory.create(getProvider());
        proxy.removeSubnet(getSubnet().getId());
        setSucceeded(true);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(RemoveEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__SUBNET);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.SUBNET_REMOVED : AuditLogType.SUBNET_REMOVAL_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }
}
