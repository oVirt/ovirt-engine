package org.ovirt.engine.core.bll.provider;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class NetworkProviderValidator extends ProviderValidator {

    public NetworkProviderValidator(Provider<?> provider) {
        super(provider);
    }

    @Override
    public ValidationResult validateAddProvider() {

        ValidationResult pluginTypeValidation = validatePluginType();
        if (!pluginTypeValidation.isValid()) {
            return pluginTypeValidation;
        }
        ValidationResult authValidation = validateAuthentication();
        if (!authValidation.isValid()) {
            return authValidation;
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

            if (provider.getType() == ProviderType.OPENSTACK_NETWORK) {
                OpenstackNetworkProviderProperties properties = (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
                if (StringUtils.isEmpty(properties.getTenantName())
                        && StringUtils.isEmpty(properties.getUserDomainName())) {
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NO_TENANT_OR_USER_DOMAIN);
                }
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult providerTypeIsNetwork() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NOT_NETWORK)
                .unless(provider.getType() == ProviderType.OPENSTACK_NETWORK ||
                        provider.getType() == ProviderType.EXTERNAL_NETWORK);
    }
}
