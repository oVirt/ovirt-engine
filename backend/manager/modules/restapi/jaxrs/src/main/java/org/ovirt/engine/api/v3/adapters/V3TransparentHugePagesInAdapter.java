/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3TransparentHugePages;

public class V3TransparentHugePagesInAdapter implements V3Adapter<V3TransparentHugePages, TransparentHugePages> {
    @Override
    public TransparentHugePages adapt(V3TransparentHugePages from) {
        TransparentHugePages to = new TransparentHugePages();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
