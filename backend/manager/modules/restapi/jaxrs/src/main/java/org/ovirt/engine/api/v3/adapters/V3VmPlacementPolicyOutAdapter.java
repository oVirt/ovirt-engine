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

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Host;
import org.ovirt.engine.api.v3.types.V3Hosts;
import org.ovirt.engine.api.v3.types.V3VmPlacementPolicy;

public class V3VmPlacementPolicyOutAdapter implements V3Adapter<VmPlacementPolicy, V3VmPlacementPolicy> {
    @Override
    public V3VmPlacementPolicy adapt(VmPlacementPolicy from) {
        V3VmPlacementPolicy to = new V3VmPlacementPolicy();
        if (from.isSetAffinity()) {
            to.setAffinity(from.getAffinity().value());
        }
        if (from.isSetHosts()) {
            to.setHosts(new V3Hosts());
            to.getHosts().getHosts().addAll(adaptOut(from.getHosts().getHosts()));

            // V3 allowed specifying only one host, using the "host" element instead of "hosts":
            List<Host> hosts = from.getHosts().getHosts();
            if (hosts.size() == 1) {
                V3Host host = adaptOut(hosts.get(0));
                to.setHost(host);
            }
        }
        return to;
    }
}
