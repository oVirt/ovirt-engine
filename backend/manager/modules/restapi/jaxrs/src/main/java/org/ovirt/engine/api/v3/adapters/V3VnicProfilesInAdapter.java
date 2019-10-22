/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.VnicProfiles;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VnicProfiles;

public class V3VnicProfilesInAdapter implements V3Adapter<V3VnicProfiles, VnicProfiles> {
    @Override
    public VnicProfiles adapt(V3VnicProfiles from) {
        VnicProfiles to = new VnicProfiles();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getVnicProfiles().addAll(adaptIn(from.getVnicProfiles()));
        return to;
    }
}
