/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ExternalHostGroup;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalHostGroup;

public class V3ExternalHostGroupOutAdapter implements V3Adapter<ExternalHostGroup, V3ExternalHostGroup> {
    @Override
    public V3ExternalHostGroup adapt(ExternalHostGroup from) {
        V3ExternalHostGroup to = new V3ExternalHostGroup();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetArchitectureName()) {
            to.setArchitectureName(from.getArchitectureName());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDomainName()) {
            to.setDomainName(from.getDomainName());
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
        if (from.isSetOperatingSystemName()) {
            to.setOperatingSystemName(from.getOperatingSystemName());
        }
        if (from.isSetSubnetName()) {
            to.setSubnetName(from.getSubnetName());
        }
        return to;
    }
}
