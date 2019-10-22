/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackNetwork;

public class V3OpenStackNetworkOutAdapter implements V3Adapter<OpenStackNetwork, V3OpenStackNetwork> {
    @Override
    public V3OpenStackNetwork adapt(OpenStackNetwork from) {
        V3OpenStackNetwork to = new V3OpenStackNetwork();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
        if (from.isSetOpenstackNetworkProvider()) {
            to.setOpenstackNetworkProvider(adaptOut(from.getOpenstackNetworkProvider()));
        }
        return to;
    }
}
