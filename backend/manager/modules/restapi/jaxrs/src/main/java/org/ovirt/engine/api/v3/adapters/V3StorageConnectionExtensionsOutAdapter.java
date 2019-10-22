/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.StorageConnectionExtensions;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3StorageConnectionExtensions;

public class V3StorageConnectionExtensionsOutAdapter implements V3Adapter<StorageConnectionExtensions, V3StorageConnectionExtensions> {
    @Override
    public V3StorageConnectionExtensions adapt(StorageConnectionExtensions from) {
        V3StorageConnectionExtensions to = new V3StorageConnectionExtensions();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getStorageConnectionExtension().addAll(adaptOut(from.getStorageConnectionExtensions()));
        return to;
    }
}
