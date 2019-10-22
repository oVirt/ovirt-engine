/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackVolumeType;

public class V3OpenStackVolumeTypeInAdapter implements V3Adapter<V3OpenStackVolumeType, OpenStackVolumeType> {
    @Override
    public OpenStackVolumeType adapt(V3OpenStackVolumeType from) {
        OpenStackVolumeType to = new OpenStackVolumeType();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
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
        if (from.isSetOpenstackVolumeProvider()) {
            to.setOpenstackVolumeProvider(adaptIn(from.getOpenstackVolumeProvider()));
        }
        if (from.isSetProperties()) {
            to.setProperties(new Properties());
            to.getProperties().getProperties().addAll(adaptIn(from.getProperties().getProperties()));
        }
        return to;
    }
}
