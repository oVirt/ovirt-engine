/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.NetworkPluginType;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackNetworkProviderType;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackNetworkProvider;

public class V3OpenStackNetworkProviderInAdapter implements V3Adapter<V3OpenStackNetworkProvider, OpenStackNetworkProvider> {
    @Override
    public OpenStackNetworkProvider adapt(V3OpenStackNetworkProvider from) {
        OpenStackNetworkProvider to = new OpenStackNetworkProvider();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetAgentConfiguration()) {
            to.setAgentConfiguration(adaptIn(from.getAgentConfiguration()));
        }
        if (from.isSetAuthenticationUrl()) {
            to.setAuthenticationUrl(from.getAuthenticationUrl());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetPassword()) {
            to.setPassword(from.getPassword());
        }
        if (from.isSetPluginType()) {
            to.setPluginType(NetworkPluginType.fromValue(from.getPluginType()));
        }
        if (from.isSetProperties()) {
            to.setProperties(new Properties());
            to.getProperties().getProperties().addAll(adaptIn(from.getProperties().getProperties()));
        }
        if (from.isSetRequiresAuthentication()) {
            to.setRequiresAuthentication(from.isRequiresAuthentication());
        }
        if (from.isSetTenantName()) {
            to.setTenantName(from.getTenantName());
        }
        if (from.isSetUrl()) {
            to.setUrl(from.getUrl());
        }
        if (from.isSetUsername()) {
            to.setUsername(from.getUsername());
        }
        to.setType(OpenStackNetworkProviderType.NEUTRON);
        return to;
    }
}
