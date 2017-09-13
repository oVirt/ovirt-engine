package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.ExternalNetworkProviderConfiguration;
import org.ovirt.engine.api.model.ExternalProvider;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.businessentities.Provider;

public class ExternalNetworkProviderConfigurationMapper {
    @Mapping(from = Provider.class, to = ExternalNetworkProviderConfiguration.class)
    public static ExternalNetworkProviderConfiguration map(Provider entity,  ExternalNetworkProviderConfiguration template) {
        if (entity == null) {
            return template;
        }

        ExternalNetworkProviderConfiguration model = template == null ? new ExternalNetworkProviderConfiguration() : template;
        model.setId(HexUtils.string2hex(entity.getId().toString()));
        ExternalProvider externalProvider = new ExternalProvider();
        externalProvider.setId(entity.getId().toString());
        model.setExternalNetworkProvider(externalProvider);
        return model;
    }
}
