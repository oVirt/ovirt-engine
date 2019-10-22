/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.HostNics;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Bonding;

public class V3BondingInAdapter implements V3Adapter<V3Bonding, Bonding> {
    @Override
    public Bonding adapt(V3Bonding from) {
        Bonding to = new Bonding();
        if (from.isSetOptions()) {
            to.setOptions(new Options());
            to.getOptions().getOptions().addAll(adaptIn(from.getOptions().getOptions()));
        }
        if (from.isSetSlaves()) {
            to.setSlaves(new HostNics());
            to.getSlaves().getHostNics().addAll(adaptIn(from.getSlaves().getSlaves()));
        }
        return to;
    }
}
