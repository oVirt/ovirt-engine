/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Bios;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Bios;

public class V3BiosOutAdapter implements V3Adapter<Bios, V3Bios> {
    @Override
    public V3Bios adapt(Bios from) {
        V3Bios to = new V3Bios();
        if (from.isSetBootMenu()) {
            to.setBootMenu(adaptOut(from.getBootMenu()));
        }
        return to;
    }
}
