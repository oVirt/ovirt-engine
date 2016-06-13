package org.ovirt.engine.core.bll.provider;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.AgentConfiguration;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class NetworkProviderValidator extends ProviderValidator {

    public NetworkProviderValidator(Provider<?> provider) {
        super(provider);
    }

    @Override
    public ValidationResult validateAddProvider() {

        ValidationResult result = validatePluginType();
        if (!result.isValid()) {
            return result;
        }
        result = validateAuthentication();
        if (!result.isValid()) {
            return result;
        }
        return super.validateAddProvider();
    }

    public ValidationResult validatePluginType() {
        OpenstackNetworkProviderProperties properties = (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
        String pluginType = properties.getPluginType();
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NO_PLUGIN_TYPE)
                .when(StringUtils.isEmpty(pluginType));
    }

    public ValidationResult validateAuthentication() {
        if (provider.isRequiringAuthentication()) {
            if (StringUtils.isEmpty(provider.getUsername())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NO_USER);
            }
            if (StringUtils.isEmpty(provider.getPassword())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NO_PASSWORD);
            }
            if (StringUtils.isEmpty(provider.getAuthUrl())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NO_AUTH_URL);
            }
            OpenstackNetworkProviderProperties properties = (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
            if (StringUtils.isEmpty(properties.getTenantName())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NO_TENANT_NAME);
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult providerTypeValid() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_TYPE_MISMATCH)
                .when(provider.getType() != ProviderType.OPENSTACK_NETWORK);
    }

    public ValidationResult networkMappingsProvided(String networkMappings) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_MISSING_NETWORK_MAPPINGS)
                .when(StringUtils.isBlank(networkMappings)
                        && (getAgentConfiguration() == null
                              || StringUtils.isBlank(getAgentConfiguration().getNetworkMappings())));
    }

    public ValidationResult messagingBrokerProvided() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_MISSING_MESSAGING_BROKER_PROPERTIES)
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
