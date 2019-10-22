/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CustomProperties;

public class V3CustomPropertiesOutAdapter implements V3Adapter<CustomProperties, V3CustomProperties> {
    @Override
    public V3CustomProperties adapt(CustomProperties from) {
        V3CustomProperties to = new V3CustomProperties();
        to.getCustomProperty().addAll(adaptOut(from.getCustomProperties()));
        return to;
    }
}
