/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Properties;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicy;

public class V3SchedulingPolicyOutAdapter implements V3Adapter<SchedulingPolicy, V3SchedulingPolicy> {
    @Override
    public V3SchedulingPolicy adapt(SchedulingPolicy from) {
        V3SchedulingPolicy to = new V3SchedulingPolicy();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDefaultPolicy()) {
            to.setDefaultPolicy(from.isDefaultPolicy());
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
        if (from.isSetLocked()) {
            to.setLocked(from.isLocked());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetProperties()) {
            to.setProperties(new V3Properties());
            to.getProperties().getProperties().addAll(adaptOut(from.getProperties().getProperties()));
        }
        return to;
    }
}
