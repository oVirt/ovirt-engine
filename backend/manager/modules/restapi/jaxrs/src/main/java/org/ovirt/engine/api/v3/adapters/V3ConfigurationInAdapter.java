/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Configuration;

public class V3ConfigurationInAdapter implements V3Adapter<V3Configuration, Configuration> {
    @Override
    public Configuration adapt(V3Configuration from) {
        Configuration to = new Configuration();
        if (from.isSetData()) {
            to.setData(from.getData());
        }
        if (from.isSetType()) {
            to.setType(ConfigurationType.fromValue(from.getType()));
        }
        return to;
    }
}
