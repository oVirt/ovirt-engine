/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CustomProperties;

public class V3CustomPropertiesInAdapter implements V3Adapter<V3CustomProperties, CustomProperties> {
    @Override
    public CustomProperties adapt(V3CustomProperties from) {
        CustomProperties to = new CustomProperties();
        to.getCustomProperties().addAll(adaptIn(from.getCustomProperty()));
        return to;
    }
}
