/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Bonding;
import org.ovirt.engine.api.v3.types.V3Options;
import org.ovirt.engine.api.v3.types.V3Slaves;

public class V3BondingOutAdapter implements V3Adapter<Bonding, V3Bonding> {
    @Override
    public V3Bonding adapt(Bonding from) {
        V3Bonding to = new V3Bonding();
        if (from.isSetOptions()) {
            to.setOptions(new V3Options());
            to.getOptions().getOptions().addAll(adaptOut(from.getOptions().getOptions()));
        }
        if (from.isSetSlaves()) {
            to.setSlaves(new V3Slaves());
            to.getSlaves().getSlaves().addAll(adaptOut(from.getSlaves().getHostNics()));
        }
        return to;
    }
}
