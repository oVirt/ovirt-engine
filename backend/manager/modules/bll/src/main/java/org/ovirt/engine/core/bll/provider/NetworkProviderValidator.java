package org.ovirt.engine.core.bll.provider;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.AgentConfiguration;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class NetworkProviderValidator extends ProviderValidator {

    public NetworkProviderValidator(Provider<?> provider) {
        super(provider);
    }

    public ValidationResult providerTypeValid() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_PROVIDER_TYPE_MISMATCH)
                .when(provider.getType() != ProviderType.OPENSTACK_NETWORK);
    }

    public ValidationResult networkMappingsProvided(String networkMappings) {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_MISSING_NETWORK_MAPPINGS)
                .when(StringUtils.isBlank(networkMappings)
                        && (getAgentConfiguration() == null
                              || StringUtils.isBlank(getAgentConfiguration().getNetworkMappings())));
    }

    public ValidationResult messagingBrokerProvided() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_MISSING_MESSAGING_BROKER_PROPERTIES)
                .when(getAgentConfiguration() == null
                        || getAgentConfiguration().getMessagingConfiguration() == null
                        || StringUtils.isEmpty(getAgentConfiguration().getMessagingConfiguration().getAddress()));
    }

    private AgentConfiguration getAgentConfiguration() {
        OpenstackNetworkProviderProperties properties =
                (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
        return properties == null ? null : properties.getAgentConfiguration();
    }
}
