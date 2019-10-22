/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Configuration;

public class V3ConfigurationOutAdapter implements V3Adapter<Configuration, V3Configuration> {
    @Override
    public V3Configuration adapt(Configuration from) {
        V3Configuration to = new V3Configuration();
        if (from.isSetData()) {
            to.setData(from.getData());
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        return to;
    }
}
