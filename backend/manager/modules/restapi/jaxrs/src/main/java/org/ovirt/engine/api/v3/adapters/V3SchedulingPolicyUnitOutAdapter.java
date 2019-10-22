/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Properties;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicyUnit;

public class V3SchedulingPolicyUnitOutAdapter implements V3Adapter<SchedulingPolicyUnit, V3SchedulingPolicyUnit> {
    @Override
    public V3SchedulingPolicyUnit adapt(SchedulingPolicyUnit from) {
        V3SchedulingPolicyUnit to = new V3SchedulingPolicyUnit();
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
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetInternal()) {
            to.setInternal(from.isInternal());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetProperties()) {
            to.setPropertiesMetaData(new V3Properties());
            to.getPropertiesMetaData().getProperties().addAll(adaptOut(from.getProperties().getProperties()));
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        return to;
    }
}
