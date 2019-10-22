/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackVolumeType;
import org.ovirt.engine.api.v3.types.V3Properties;

public class V3OpenStackVolumeTypeOutAdapter implements V3Adapter<OpenStackVolumeType, V3OpenStackVolumeType> {
    @Override
    public V3OpenStackVolumeType adapt(OpenStackVolumeType from) {
        V3OpenStackVolumeType to = new V3OpenStackVolumeType();
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
        if (from.isSetOpenstackVolumeProvider()) {
            to.setOpenstackVolumeProvider(adaptOut(from.getOpenstackVolumeProvider()));
        }
        if (from.isSetProperties()) {
            to.setProperties(new V3Properties());
            to.getProperties().getProperties().addAll(adaptOut(from.getProperties().getProperties()));
        }
        return to;
    }
}
