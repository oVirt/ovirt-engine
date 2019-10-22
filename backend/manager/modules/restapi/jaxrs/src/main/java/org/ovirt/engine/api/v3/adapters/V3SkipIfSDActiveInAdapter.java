/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.SkipIfSdActive;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SkipIfSDActive;

public class V3SkipIfSDActiveInAdapter implements V3Adapter<V3SkipIfSDActive, SkipIfSdActive> {
    @Override
    public SkipIfSdActive adapt(V3SkipIfSDActive from) {
        SkipIfSdActive to = new SkipIfSdActive();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
