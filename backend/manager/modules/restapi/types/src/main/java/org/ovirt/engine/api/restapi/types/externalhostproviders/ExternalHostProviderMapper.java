/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.types.externalhostproviders;

import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;

public class ExternalHostProviderMapper {
    @Mapping(from = ExternalHostProvider.class, to = Provider.class)
    public static Provider map(ExternalHostProvider model, Provider template) {
        Provider entity = template != null? template: new Provider();
        entity.setType(ProviderType.FOREMAN);
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
        return entity;
    }

    @Mapping(from = Provider.class, to = ExternalHostProvider.class)
    public static ExternalHostProvider map(Provider entity, ExternalHostProvider template) {
        ExternalHostProvider model = template != null? template: new ExternalHostProvider();
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
        model.setRequiresAuthentication(entity.isRequiringAuthentication());
        if (entity.getUsername() != null) {
            model.setUsername(entity.getUsername());
        }
        // The password isn't mapped for security reasons.
        // if (entity.getPassword() != null) {
        //     model.setPassword(entity.getPassword());
        // }
        return model;
    }
}
