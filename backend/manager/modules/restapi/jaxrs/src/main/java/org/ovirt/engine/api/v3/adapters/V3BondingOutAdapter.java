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

import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Bonding;
import org.ovirt.engine.api.v3.types.V3Options;
import org.ovirt.engine.api.v3.types.V3Slaves;

public class V3BondingOutAdapter implements V3Adapter<Bonding, V3Bonding> {
    @Override
    public V3Bonding adapt(Bonding from) {
        V3Bonding to = new V3Bonding();
        if (from.isSetOptions()) {
            to.setOptions(new V3Options());
            to.getOptions().getOptions().addAll(adaptOut(from.getOptions().getOptions()));
        }
        if (from.isSetSlaves()) {
            to.setSlaves(new V3Slaves());
            to.getSlaves().getSlaves().addAll(adaptOut(from.getSlaves().getHostNics()));
        }
        return to;
    }
}
