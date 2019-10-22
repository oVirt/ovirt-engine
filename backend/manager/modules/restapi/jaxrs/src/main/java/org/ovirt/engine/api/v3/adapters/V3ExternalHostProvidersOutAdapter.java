/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ExternalHostProviders;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalHostProviders;

public class V3ExternalHostProvidersOutAdapter implements V3Adapter<ExternalHostProviders, V3ExternalHostProviders> {
    @Override
    public V3ExternalHostProviders adapt(ExternalHostProviders from) {
        V3ExternalHostProviders to = new V3ExternalHostProviders();
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
        to.getExternalHostProviders().addAll(adaptOut(from.getExternalHostProviders()));
        return to;
    }
}
