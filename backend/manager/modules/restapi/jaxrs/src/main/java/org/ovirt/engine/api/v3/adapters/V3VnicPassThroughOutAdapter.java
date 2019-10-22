/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.VnicPassThrough;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VnicPassThrough;

public class V3VnicPassThroughOutAdapter implements V3Adapter<VnicPassThrough, V3VnicPassThrough> {
    @Override
    public V3VnicPassThrough adapt(VnicPassThrough from) {
        V3VnicPassThrough to = new V3VnicPassThrough();
        if (from.isSetMode()) {
            to.setMode(from.getMode().value());
        }
        return to;
    }
}
