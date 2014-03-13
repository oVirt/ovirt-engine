package org.ovirt.engine.core.bll.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class RemoveProviderCommand<P extends ProviderParameters> extends CommandBase<P> {

    private Provider<?> deletedProvider;

    public RemoveProviderCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveProviderCommand(P parameters) {
        super(parameters);
    }

    private Provider<?> getDeletedProvider() {
        if (deletedProvider == null) {
            deletedProvider = getProviderDao().get(getParameters().getProvider().getId());
        }

        return deletedProvider;
    }

    public String getProviderName() {
        Provider<?> provider = getDeletedProvider();
        return provider == null ? null : provider.getName();
    }

    @Override
    protected boolean canDoAction() {
        RemoveProviderValidator validator = new RemoveProviderValidator(getDeletedProvider());
        return validate(validator.providerIsSet()) && validate(validator.providerNetworksNotUsed());
    }

    @Override
    protected void executeCommand() {
        final Guid providerId = getParameters().getProvider().getId();

        ProviderProxy providerProxy = ProviderProxyFactory.getInstance().create(getParameters().getProvider());
        if (providerProxy != null) {
            providerProxy.onRemoval();
        }

        getProviderDao().remove(providerId);
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
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__PROVIDER);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(RemoveEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.PROVIDER_REMOVED : AuditLogType.PROVIDER_REMOVAL_FAILED;
    }

    protected static class RemoveProviderValidator extends ProviderValidator {

        public RemoveProviderValidator(Provider<?> provider) {
            super(provider);
        }

        public ValidationResult providerNetworksNotUsed() {
            List<Network> networksInUse = new ArrayList<>();
            List<Network> networks = getNetworkDao().getAllForProvider(provider.getId());

            for (Network network : networks) {
                NetworkValidator networkValidator = getValidator(network);
                if (!networkValidator.networkNotUsedByVms().isValid()
                        || !networkValidator.networkNotUsedByTemplates().isValid()) {
                    networksInUse.add(network);
                }
            }

            return networksInUse.isEmpty() ? ValidationResult.VALID
                    : new ValidationResult(getProviderNetworkUsedValidationMessage(networksInUse.size()),
                            ReplacementUtils.replaceWithNameable("NETWORK_NAMES", networksInUse));
        }

        protected VdcBllMessages getProviderNetworkUsedValidationMessage(int numberOfNetworks) {
            boolean singular = numberOfNetworks == 1;
            if (singular) {
                return VdcBllMessages.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_ONCE;
            } else {
                return VdcBllMessages.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_MULTIPLE_TIMES;
            }
        }

        protected NetworkDao getNetworkDao() {
            return DbFacade.getInstance().getNetworkDao();
        }

        protected NetworkValidator getValidator(Network network) {
            return new NetworkValidator(network);
        }
    }
}
