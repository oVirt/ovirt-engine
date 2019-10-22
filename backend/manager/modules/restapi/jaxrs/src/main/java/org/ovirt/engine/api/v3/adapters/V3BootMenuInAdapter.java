/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.BootMenu;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3BootMenu;

public class V3BootMenuInAdapter implements V3Adapter<V3BootMenu, BootMenu> {
    @Override
    public BootMenu adapt(V3BootMenu from) {
        BootMenu to = new BootMenu();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
