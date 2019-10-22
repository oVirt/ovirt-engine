/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.SeLinux;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SELinux;

public class V3SeLinuxOutAdapter implements V3Adapter<SeLinux, V3SELinux> {
    @Override
    public V3SELinux adapt(SeLinux from) {
        V3SELinux to = new V3SELinux();
        if (from.isSetMode()) {
            to.setMode(from.getMode().value());
        }
        return to;
    }
}
