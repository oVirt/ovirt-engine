/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ExternalComputeResource;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalComputeResource;

public class V3ExternalComputeResourceOutAdapter implements V3Adapter<ExternalComputeResource, V3ExternalComputeResource> {
    @Override
    public V3ExternalComputeResource adapt(ExternalComputeResource from) {
        V3ExternalComputeResource to = new V3ExternalComputeResource();
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
        if (from.isSetExternalHostProvider()) {
            to.setExternalHostProvider(adaptOut(from.getExternalHostProvider()));
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
        if (from.isSetProvider()) {
            to.setProvider(from.getProvider());
        }
        if (from.isSetUrl()) {
            to.setUrl(from.getUrl());
        }
        if (from.isSetUser()) {
            to.setUser(from.getUser());
        }
        return to;
    }
}
