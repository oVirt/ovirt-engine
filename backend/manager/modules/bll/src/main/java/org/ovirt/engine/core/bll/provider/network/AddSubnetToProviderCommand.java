package org.ovirt.engine.core.bll.provider.network;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddExternalSubnetParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class AddSubnetToProviderCommand<T extends AddExternalSubnetParameters> extends CommandBase<T> {

    private Provider<?> provider;

    private ProviderNetwork externalNetwork;

    public AddSubnetToProviderCommand(T parameters) {
        super(parameters);
    }

    private Provider<?> getProvider() {
        if (provider == null && getExternalNetwork() != null) {
            provider = getDbFacade().getProviderDao().get(getExternalNetwork().getProviderId());
        }

        return provider;
    }

    private ProviderNetwork getExternalNetwork() {
        if (externalNetwork == null) {
            Network network = getNetworkDAO().get(getParameters().getNetworkId());

            if (network != null) {
                externalNetwork = network.getProvidedBy();
            }
        }

        return externalNetwork;
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
    protected boolean canDoAction() {
        ProviderValidator validator = new ProviderValidator(getProvider());

        return validate(validator.providerIsSet());
    }

    @Override
    protected void executeCommand() {
        NetworkProviderProxy proxy = ProviderProxyFactory.getInstance().create(getProvider());
        getSubnet().setExternalNetwork(getExternalNetwork());
        proxy.addSubnet(getSubnet());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SUBNET);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
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

}
