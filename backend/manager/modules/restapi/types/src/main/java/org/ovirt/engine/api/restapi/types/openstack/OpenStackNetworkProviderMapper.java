/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.api.model.NetworkPluginType;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackNetworkProviderType;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.Property;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkPluginType;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;

public class OpenStackNetworkProviderMapper {

    @Mapping(from = OpenStackNetworkProvider.class, to = Provider.class)
    public static Provider<OpenstackNetworkProviderProperties> map(OpenStackNetworkProvider model,
            Provider<OpenstackNetworkProviderProperties> template) {
        Provider<OpenstackNetworkProviderProperties> entity =
             template != null? template: new Provider<>();
        if (model.isSetType()) {
            entity.setType(mapProviderType(model.getType()));
        }
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetUrl()) {
            entity.setUrl(model.getUrl());
        }
        if (model.isSetRequiresAuthentication()) {
            entity.setRequiringAuthentication(model.isRequiresAuthentication());
        }
        if (model.isSetUnmanaged()) {
            entity.setIsUnmanaged(model.isUnmanaged());
        }
        if (model.isSetUsername()) {
            entity.setUsername(model.getUsername());
        }
        if (model.isSetPassword()) {
            entity.setPassword(model.getPassword());
        }
        if (model.isSetAuthenticationUrl()) {
            entity.setAuthUrl(model.getAuthenticationUrl());
        }
        if (model.isSetProperties()) {
            Map<String, String> map = new HashMap<>();
            for (Property property : model.getProperties().getProperties()) {
                map.put(property.getName(), property.getValue());
            }
            entity.setCustomProperties(map);
        }
        entity.setAdditionalProperties(map(model, entity.getAdditionalProperties()));
        return entity;
    }

    @Mapping(from = OpenStackNetworkProvider.class, to = OpenstackNetworkProviderProperties.class)
    public static OpenstackNetworkProviderProperties map(OpenStackNetworkProvider model,
            OpenstackNetworkProviderProperties template) {
        OpenstackNetworkProviderProperties entity =
                template != null? template: new OpenstackNetworkProviderProperties();
        if (model.isSetTenantName()) {
            entity.setTenantName(model.getTenantName());
        }

        if (model.isSetUserDomainName()) {
            entity.setUserDomainName(model.getUserDomainName());
        }

        if (model.isSetProjectName()) {
            entity.setProjectName(model.getProjectName());
        }

        if (model.isSetProjectDomainName()) {
            entity.setProjectDomainName(model.getProjectDomainName());
        }

        // The `plugin_type` attribute has been deprecated in version 4.2 of the engine. This code is preserved
        // for backwards compatibility, and should be removed in version 5 of the API.
        if (model.isSetPluginType() && model.getType() == OpenStackNetworkProviderType.NEUTRON) {
            entity.setPluginType(mapPluginType(model.getPluginType()));
        }
        if (model.isSetExternalPluginType()) {
            entity.setPluginType(model.getExternalPluginType());
        }
        if (model.isSetReadOnly()) {
            entity.setReadOnly(model.isReadOnly());
        }
        if (model.isSetAutoSync()) {
            entity.setAutoSync(model.isAutoSync());
        }
        return entity;
    }

    @Mapping(from = Provider.class, to = OpenStackNetworkProvider.class)
    public static OpenStackNetworkProvider map(Provider<OpenstackNetworkProviderProperties> entity,
            OpenStackNetworkProvider template) {
        OpenStackNetworkProvider model = template != null? template: new OpenStackNetworkProvider();
        model.setType(mapProviderType(entity.getType()));
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getDescription() != null) {
            model.setDescription(entity.getDescription());
        }
        if (entity.getUrl() != null) {
            model.setUrl(entity.getUrl());
        }
        if (entity.getAuthUrl() != null) {
            model.setAuthenticationUrl(entity.getAuthUrl());
        }
        model.setUnmanaged(entity.getIsUnmanaged());
        model.setRequiresAuthentication(entity.isRequiringAuthentication());
        if (entity.getUsername() != null) {
            model.setUsername(entity.getUsername());
        }
        // The password isn't mapped for security reasons.
        // if (entity.getPassword() != null) {
        //     model.setPassword(entity.getPassword());
        // }
        Map<String, String> customProperties = entity.getCustomProperties();
        if (customProperties != null) {
            Properties properties = new Properties();
            for (Map.Entry<String, String> entry : customProperties.entrySet()) {
                Property property = new Property();
                property.setName(entry.getKey());
                property.setValue(entry.getValue());
                properties.getProperties().add(property);
            }
            model.setProperties(properties);
        }
        OpenstackNetworkProviderProperties additionalProperties = entity.getAdditionalProperties();
        if (additionalProperties != null) {
            if (additionalProperties.getTenantName() != null) {
                model.setTenantName(additionalProperties.getTenantName());
            }
            if (additionalProperties.getUserDomainName() != null) {
                model.setUserDomainName(additionalProperties.getUserDomainName());
            }
            if (additionalProperties.getProjectName() != null) {
                model.setProjectName(additionalProperties.getProjectName());
            }
            if (additionalProperties.getProjectDomainName() != null) {
                model.setProjectDomainName(additionalProperties.getProjectDomainName());
            }
            String pluginType = additionalProperties.getPluginType();
            if (pluginType != null) {
                // The `plugin_type` attribute has been deprecated in version 4.2 of the engine. This code is preserved
                // for backwards compatibility, and should be removed in version 5 of the API.
                if (entity.getType() == ProviderType.OPENSTACK_NETWORK &&
                    OpenstackNetworkPluginType.OPEN_VSWITCH.name().equalsIgnoreCase(pluginType)) {
                    model.setPluginType(NetworkPluginType.OPEN_VSWITCH);
                }
                model.setExternalPluginType(additionalProperties.getPluginType());
            }
            model.setReadOnly(additionalProperties.getReadOnly());
            model.setAutoSync(additionalProperties.getAutoSync());
        }
        return model;
    }

    private static String mapPluginType(NetworkPluginType pluginType) {
        if (pluginType == NetworkPluginType.OPEN_VSWITCH) {
            return OpenstackNetworkPluginType.OPEN_VSWITCH.name();
        }
        throw new IllegalArgumentException("Unknown Neutron network plugin type \"" + pluginType + "\"");
    }

    private static ProviderType mapProviderType(OpenStackNetworkProviderType type) {
        switch (type) {
        case NEUTRON:
            return ProviderType.OPENSTACK_NETWORK;
        case EXTERNAL:
            return ProviderType.EXTERNAL_NETWORK;
        }
        throw new IllegalArgumentException("Unknown network provider type \"" + type.name() + "\"");
    }

    private static OpenStackNetworkProviderType mapProviderType(ProviderType type) {
        switch (type) {
        case OPENSTACK_NETWORK:
            return OpenStackNetworkProviderType.NEUTRON;
        case EXTERNAL_NETWORK:
            return OpenStackNetworkProviderType.EXTERNAL;
        }
        throw new IllegalArgumentException("Provider type not allowed: \"" + type.name() + "\"");
    }
}
