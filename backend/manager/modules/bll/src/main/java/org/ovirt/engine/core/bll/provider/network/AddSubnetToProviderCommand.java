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
import org.ovirt.engine.core.common.action.AddExternalSubnetParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.provider.ProviderDao;

@NonTransactiveCommandAttribute
public class AddSubnetToProviderCommand<T extends AddExternalSubnetParameters> extends CommandBase<T> {

    @Inject
    private ProviderDao providerDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;


    public AddSubnetToProviderCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private Provider<?> getProvider() {
        return providerDao.get(getParameters().getProviderId());
    }

    private ProviderNetwork getExternalNetwork() {
        ProviderNetwork providerNetwork = new ProviderNetwork();
        providerNetwork.setProviderId(getParameters().getProviderId());
        providerNetwork.setExternalId(getParameters().getNetworkId());
        return providerNetwork;
    }

    private ExternalSubnet getSubnet() {
        return getParameters().getSubnet();
    }

    @Override
    protected boolean validate() {
        ProviderValidator validator = new ProviderValidator(getProvider());

        return validate(validator.providerIsSet()) && validate(validator.validateReadOnlyActions());
    }

    @Override
    protected void executeCommand() {
        NetworkProviderProxy proxy = providerProxyFactory.create(getProvider());
        getSubnet().setExternalNetwork(getExternalNetwork());
        proxy.addSubnet(getSubnet());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__SUBNET);
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.SUBNET_ADDED : AuditLogType.SUBNET_ADDITION_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    public String getSubnetName() {
        return getParameters().getSubnet().getName();
    }

    public String getProviderName() {
        return getProvider().getName();
    }

}
