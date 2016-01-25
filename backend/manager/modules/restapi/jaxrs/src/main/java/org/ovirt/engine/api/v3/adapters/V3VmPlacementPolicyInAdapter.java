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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VmPlacementPolicy;

public class V3VmPlacementPolicyInAdapter implements V3Adapter<V3VmPlacementPolicy, VmPlacementPolicy> {
    @Override
    public VmPlacementPolicy adapt(V3VmPlacementPolicy from) {
        VmPlacementPolicy to = new VmPlacementPolicy();
        if (from.isSetAffinity()) {
            to.setAffinity(VmAffinity.fromValue(from.getAffinity()));
        }
        if (from.isSetHosts()) {
            to.setHosts(new Hosts());
            to.getHosts().getHosts().addAll(adaptIn(from.getHosts().getHosts()));
        }

        // V3 allowed specifying only one host, using "host" instead of "hosts":
        if (from.isSetHost() && !from.isSetHosts()) {
            Hosts hosts = new Hosts();
            Host host = adaptIn(from.getHost());
            hosts.getHosts().add(host);
            to.setHosts(hosts);
        }

        return to;
    }
}
