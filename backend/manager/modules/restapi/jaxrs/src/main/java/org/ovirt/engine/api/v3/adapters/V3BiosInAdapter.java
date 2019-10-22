/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Bios;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Bios;

public class V3BiosInAdapter implements V3Adapter<V3Bios, Bios> {
    @Override
    public Bios adapt(V3Bios from) {
        Bios to = new Bios();
        if (from.isSetBootMenu()) {
            to.setBootMenu(adaptIn(from.getBootMenu()));
        }
        return to;
    }
}
