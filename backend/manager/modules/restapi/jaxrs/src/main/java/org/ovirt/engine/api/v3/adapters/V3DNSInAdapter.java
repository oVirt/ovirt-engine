/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Dns;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DNS;

public class V3DNSInAdapter implements V3Adapter<V3DNS, Dns> {
    @Override
    public Dns adapt(V3DNS from) {
        Dns to = new Dns();
        if (from.isSetSearchDomains()) {
            to.setSearchDomains(new Hosts());
            to.getSearchDomains().getHosts().addAll(adaptIn(from.getSearchDomains().getHosts()));
        }
        if (from.isSetServers()) {
            to.setServers(new Hosts());
            to.getServers().getHosts().addAll(adaptIn(from.getServers().getHosts()));
        }
        return to;
    }
}
