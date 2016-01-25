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

import org.ovirt.engine.api.model.NetworkConfiguration;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NetworkConfiguration;

public class V3NetworkConfigurationInAdapter implements V3Adapter<V3NetworkConfiguration, NetworkConfiguration> {
    @Override
    public NetworkConfiguration adapt(V3NetworkConfiguration from) {
        NetworkConfiguration to = new NetworkConfiguration();
        if (from.isSetDns()) {
            to.setDns(adaptIn(from.getDns()));
        }
        if (from.isSetNics()) {
            to.setNics(new Nics());
            to.getNics().getNics().addAll(adaptIn(from.getNics().getNics()));
        }
        return to;
    }
}
