/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.SeLinux;
import org.ovirt.engine.api.model.SeLinuxMode;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SELinux;

public class V3SELinuxInAdapter implements V3Adapter<V3SELinux, SeLinux> {
    @Override
    public SeLinux adapt(V3SELinux from) {
        SeLinux to = new SeLinux();
        if (from.isSetMode()) {
            to.setMode(SeLinuxMode.fromValue(from.getMode()));
        }
        return to;
    }
}
