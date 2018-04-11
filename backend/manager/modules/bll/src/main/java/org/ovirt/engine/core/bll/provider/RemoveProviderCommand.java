package org.ovirt.engine.core.bll.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class RemoveProviderCommand<P extends ProviderParameters> extends CommandBase<P> {

    private Provider<?> deletedProvider;

    private ProviderProxy providerProxy;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public RemoveProviderCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveProviderCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private Provider<?> getDeletedProvider() {
        if (deletedProvider == null) {
            deletedProvider = providerDao.get(getParameters().getProvider().getId());
        }

        return deletedProvider;
    }

    public String getProviderName() {
        Provider<?> provider = getDeletedProvider();
        return provider == null ? null : provider.getName();
    }

    protected boolean validateRemoveProvider() {
        if (getProviderProxy() != null && !getParameters().isForce()) {
            ProviderValidator providerValidator = getProviderProxy().getProviderValidator();
            return validate(providerValidator.validateRemoveProvider());
        }
        return true;
    }

    public ProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            providerProxy = providerProxyFactory.create(getParameters().getProvider());
        }
        return providerProxy;
    }

    @Override
    protected boolean validate() {
        RemoveProviderValidator validator = new RemoveProviderValidator(networkDao, clusterDao, getDeletedProvider());
        return validate(validator.providerIsSet()) && validate(validator.providerNetworksNotUsed())
                && validate(validator.providerIsNoDefaultProvider()) && validateRemoveProvider();
    }

    @Override
    protected void executeCommand() {
        final Guid providerId = getParameters().getProvider().getId();

        if (getProviderProxy() != null) {
            getProviderProxy().onRemoval();
        }

        providerDao.remove(providerId);
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
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__PROVIDER);
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

        private final NetworkDao networkDao;
        private final ClusterDao clusterDao;

        public RemoveProviderValidator(NetworkDao networkDao, ClusterDao clusterDao, Provider<?> provider) {
            super(provider);
            this.networkDao = networkDao;
            this.clusterDao = clusterDao;
        }

        public ValidationResult providerNetworksNotUsed() {
            List<Network> networksInUse = new ArrayList<>();
            List<Network> networks = networkDao.getAllForProvider(provider.getId());

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

        protected EngineMessage getProviderNetworkUsedValidationMessage(int numberOfNetworks) {
            boolean singular = numberOfNetworks == 1;
            if (singular) {
                return EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_ONCE;
            } else {
                return EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_MULTIPLE_TIMES;
            }
        }

        public ValidationResult providerIsNoDefaultProvider() {
            List<Cluster> clusters = clusterDao.getAllClustersByDefaultNetworkProviderId(provider.getId());
            return clusters.isEmpty() ? ValidationResult.VALID
                    : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_USED_IN_CLUSTER,
                    ReplacementUtils.replaceWithNameable("CLUSTER_NAMES", clusters));
        }

        protected NetworkValidator getValidator(Network network) {
            return new NetworkValidator(network);
        }
    }
}
