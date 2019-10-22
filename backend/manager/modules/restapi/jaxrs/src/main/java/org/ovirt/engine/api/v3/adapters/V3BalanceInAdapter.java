/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Balance;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Balance;

public class V3BalanceInAdapter implements V3Adapter<V3Balance, Balance> {
    @Override
    public Balance adapt(V3Balance from) {
        Balance to = new Balance();
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
        if (from.isSetSchedulingPolicy()) {
            to.setSchedulingPolicy(adaptIn(from.getSchedulingPolicy()));
        }
        if (from.isSetSchedulingPolicyUnit()) {
            to.setSchedulingPolicyUnit(adaptIn(from.getSchedulingPolicyUnit()));
        }
        return to;
    }
}
