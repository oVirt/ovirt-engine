/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MacPool;
import org.ovirt.engine.api.v3.types.V3Ranges;

public class V3MacPoolOutAdapter implements V3Adapter<MacPool, V3MacPool> {
    @Override
    public V3MacPool adapt(MacPool from) {
        V3MacPool to = new V3MacPool();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
            to.setRanges(new V3Ranges());
            to.getRanges().getRanges().addAll(adaptOut(from.getRanges().getRanges()));
        }
        return to;
    }
}
