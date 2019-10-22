/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Dns;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DNS;
import org.ovirt.engine.api.v3.types.V3Hosts;

public class V3DnsOutAdapter implements V3Adapter<Dns, V3DNS> {
    @Override
    public V3DNS adapt(Dns from) {
        V3DNS to = new V3DNS();
        if (from.isSetSearchDomains()) {
            to.setSearchDomains(new V3Hosts());
            to.getSearchDomains().getHosts().addAll(adaptOut(from.getSearchDomains().getHosts()));
        }
        if (from.isSetServers()) {
            to.setServers(new V3Hosts());
            to.getServers().getHosts().addAll(adaptOut(from.getServers().getHosts()));
        }
        return to;
    }
}
