/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackNetworkProvider;
import org.ovirt.engine.api.v3.types.V3Properties;

public class V3OpenStackNetworkProviderOutAdapter implements V3Adapter<OpenStackNetworkProvider, V3OpenStackNetworkProvider> {
    @Override
    public V3OpenStackNetworkProvider adapt(OpenStackNetworkProvider from) {
        V3OpenStackNetworkProvider to = new V3OpenStackNetworkProvider();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetAgentConfiguration()) {
            to.setAgentConfiguration(adaptOut(from.getAgentConfiguration()));
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
            to.setPluginType(from.getPluginType().value());
        }
        if (from.isSetProperties()) {
            to.setProperties(new V3Properties());
            to.getProperties().getProperties().addAll(adaptOut(from.getProperties().getProperties()));
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
        return to;
    }
}
