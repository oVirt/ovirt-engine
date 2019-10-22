/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ExternalHostGroups;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalHostGroups;

public class V3ExternalHostGroupsOutAdapter implements V3Adapter<ExternalHostGroups, V3ExternalHostGroups> {
    @Override
    public V3ExternalHostGroups adapt(ExternalHostGroups from) {
        V3ExternalHostGroups to = new V3ExternalHostGroups();
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
        to.getExternalHostGroups().addAll(adaptOut(from.getExternalHostGroups()));
        return to;
    }
}
