/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types.openstack;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.Property;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.compat.Guid;

public class OpenStackVolumeProviderMapper {
    @Mapping(from = OpenStackVolumeProvider.class, to = Provider.class)
    public static Provider<OpenStackVolumeProviderProperties> map(OpenStackVolumeProvider model,
                Provider<OpenStackVolumeProviderProperties> template) {
        Provider<OpenStackVolumeProviderProperties> entity =
                template != null? template: new Provider<>();
        entity.setType(ProviderType.OPENSTACK_VOLUME);
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
        OpenStackVolumeProviderProperties additionalProperties = new OpenStackVolumeProviderProperties();
        if (model.isSetTenantName()) {
            additionalProperties.setTenantName(model.getTenantName());
        }
        if (model.isSetDataCenter()) {
            additionalProperties.setStoragePoolId(Guid.createGuidFromString(model.getDataCenter().getId()));
        }
        entity.setAdditionalProperties(additionalProperties);
        return entity;
    }

    @Mapping(from = Provider.class, to = OpenStackVolumeProvider.class)
    public static OpenStackVolumeProvider map(Provider<OpenStackVolumeProviderProperties> entity,
                                              OpenStackVolumeProvider template) {
        OpenStackVolumeProvider model = template != null? template: new OpenStackVolumeProvider();
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
        OpenStackVolumeProviderProperties additionalProperties = entity.getAdditionalProperties();
        if (additionalProperties != null) {
            if (additionalProperties.getTenantName() != null) {
                model.setTenantName(additionalProperties.getTenantName());
            }
            if (additionalProperties.getStoragePoolId() != null) {
                DataCenter dataCenter = new DataCenter();
                dataCenter.setId(additionalProperties.getStoragePoolId().toString());
                model.setDataCenter(dataCenter);
            }
        }
        return model;
    }
}
