/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.VnicPassThrough;
import org.ovirt.engine.api.model.VnicPassThroughMode;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VnicPassThrough;

public class V3VnicPassThroughInAdapter implements V3Adapter<V3VnicPassThrough, VnicPassThrough> {
    @Override
    public VnicPassThrough adapt(V3VnicPassThrough from) {
        VnicPassThrough to = new VnicPassThrough();
        if (from.isSetMode()) {
            to.setMode(VnicPassThroughMode.fromValue(from.getMode()));
        }
        return to;
    }
}
