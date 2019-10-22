/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.ReportedConfiguration;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ReportedConfiguration;

public class V3ReportedConfigurationOutAdapter implements V3Adapter<ReportedConfiguration, V3ReportedConfiguration> {
    @Override
    public V3ReportedConfiguration adapt(ReportedConfiguration from) {
        V3ReportedConfiguration to = new V3ReportedConfiguration();
        if (from.isSetActualValue()) {
            to.setActualValue(from.getActualValue());
        }
        if (from.isSetExpectedValue()) {
            to.setExpectedValue(from.getExpectedValue());
        }
        if (from.isSetInSync()) {
            to.setInSync(from.isInSync());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        return to;
    }
}
