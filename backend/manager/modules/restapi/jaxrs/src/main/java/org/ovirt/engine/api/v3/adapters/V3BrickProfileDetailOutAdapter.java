/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.BrickProfileDetail;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3BrickProfileDetail;

public class V3BrickProfileDetailOutAdapter implements V3Adapter<BrickProfileDetail, V3BrickProfileDetail> {
    @Override
    public V3BrickProfileDetail adapt(BrickProfileDetail from) {
        V3BrickProfileDetail to = new V3BrickProfileDetail();
        if (from.isSetBrick()) {
            to.setBrick(adaptOut(from.getBrick()));
        }
        if (from.isSetProfileDetails()) {
            to.getProfileDetail().addAll(adaptOut(from.getProfileDetails().getProfileDetails()));
        }
        return to;
    }
}
