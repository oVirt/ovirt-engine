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

import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.HostNics;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Bonding;

public class V3BondingInAdapter implements V3Adapter<V3Bonding, Bonding> {
    @Override
    public Bonding adapt(V3Bonding from) {
        Bonding to = new Bonding();
        if (from.isSetOptions()) {
            to.setOptions(new Options());
            to.getOptions().getOptions().addAll(adaptIn(from.getOptions().getOptions()));
        }
        if (from.isSetSlaves()) {
            to.setSlaves(new HostNics());
            to.getSlaves().getHostNics().addAll(adaptIn(from.getSlaves().getSlaves()));
        }
        return to;
    }
}
