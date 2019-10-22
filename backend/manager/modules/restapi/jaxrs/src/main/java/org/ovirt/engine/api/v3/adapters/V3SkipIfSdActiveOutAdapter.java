/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.SkipIfSdActive;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SkipIfSDActive;

public class V3SkipIfSdActiveOutAdapter implements V3Adapter<SkipIfSdActive, V3SkipIfSDActive> {
    @Override
    public V3SkipIfSDActive adapt(SkipIfSdActive from) {
        V3SkipIfSDActive to = new V3SkipIfSDActive();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
