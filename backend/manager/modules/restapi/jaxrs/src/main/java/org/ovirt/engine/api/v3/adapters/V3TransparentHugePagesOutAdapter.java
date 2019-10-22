/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3TransparentHugePages;

public class V3TransparentHugePagesOutAdapter implements V3Adapter<TransparentHugePages, V3TransparentHugePages> {
    @Override
    public V3TransparentHugePages adapt(TransparentHugePages from) {
        V3TransparentHugePages to = new V3TransparentHugePages();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
