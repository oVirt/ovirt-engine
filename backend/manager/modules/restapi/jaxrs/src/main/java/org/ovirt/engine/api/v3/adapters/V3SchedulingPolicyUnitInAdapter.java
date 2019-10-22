/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.PolicyUnitType;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicyUnit;

public class V3SchedulingPolicyUnitInAdapter implements V3Adapter<V3SchedulingPolicyUnit, SchedulingPolicyUnit> {
    @Override
    public SchedulingPolicyUnit adapt(V3SchedulingPolicyUnit from) {
        SchedulingPolicyUnit to = new SchedulingPolicyUnit();
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
        if (from.isSetPropertiesMetaData()) {
            to.setProperties(new Properties());
            to.getProperties().getProperties().addAll(adaptIn(from.getPropertiesMetaData().getProperties()));
        }
        if (from.isSetType()) {
            to.setType(PolicyUnitType.fromValue(from.getType()));
        }
        return to;
    }
}
