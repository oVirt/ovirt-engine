/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.BootMenu;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3BootMenu;

public class V3BootMenuOutAdapter implements V3Adapter<BootMenu, V3BootMenu> {
    @Override
    public V3BootMenu adapt(BootMenu from) {
        V3BootMenu to = new V3BootMenu();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
