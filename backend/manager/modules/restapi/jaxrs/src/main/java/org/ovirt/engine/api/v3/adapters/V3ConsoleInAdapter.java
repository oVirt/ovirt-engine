/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Console;

public class V3ConsoleInAdapter implements V3Adapter<V3Console, Console> {
    @Override
    public Console adapt(V3Console from) {
        Console to = new Console();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
