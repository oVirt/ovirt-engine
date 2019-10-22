/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.api.model.Ranges;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MacPool;

public class V3MacPoolInAdapter implements V3Adapter<V3MacPool, MacPool> {
    @Override
    public MacPool adapt(V3MacPool from) {
        MacPool to = new MacPool();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetAllowDuplicates()) {
            to.setAllowDuplicates(from.isAllowDuplicates());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDefaultPool()) {
            to.setDefaultPool(from.isDefaultPool());
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
        if (from.isSetRanges()) {
            to.setRanges(new Ranges());
            to.getRanges().getRanges().addAll(adaptIn(from.getRanges().getRanges()));
        }
        return to;
    }
}
