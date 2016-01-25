/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
