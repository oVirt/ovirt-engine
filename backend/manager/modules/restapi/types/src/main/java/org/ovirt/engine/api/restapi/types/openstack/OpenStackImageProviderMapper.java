/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.Property;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;

public class OpenStackImageProviderMapper {
    @Mapping(from = OpenStackImageProvider.class, to = Provider.class)
    public static Provider<OpenStackImageProviderProperties> map(OpenStackImageProvider model,
                Provider<OpenStackImageProviderProperties> template) {
        Provider<OpenStackImageProviderProperties> entity =
                template != null? template: new Provider<>();
        entity.setType(ProviderType.OPENSTACK_IMAGE);
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
        OpenStackImageProviderProperties additionalProperties = new OpenStackImageProviderProperties();
        if (model.isSetTenantName()) {
            additionalProperties.setTenantName(model.getTenantName());
        }
        entity.setAdditionalProperties(additionalProperties);
        return entity;
    }

    @Mapping(from = Provider.class, to = OpenStackImageProvider.class)
    public static OpenStackImageProvider map(Provider<OpenStackImageProviderProperties> entity,
            OpenStackImageProvider template) {
        OpenStackImageProvider model = template != null? template: new OpenStackImageProvider();
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
        OpenStackImageProviderProperties additionalProperties = entity.getAdditionalProperties();
        if (additionalProperties != null) {
            if (additionalProperties.getTenantName() != null) {
                model.setTenantName(additionalProperties.getTenantName());
            }
        }
        return model;
    }
}
