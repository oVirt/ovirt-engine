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

import org.ovirt.engine.api.model.Agents;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.PmProxies;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.PowerManagementStatus;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3PowerManagement;

public class V3PowerManagementInAdapter implements V3Adapter<V3PowerManagement, PowerManagement> {
    @Override
    public PowerManagement adapt(V3PowerManagement from) {
        PowerManagement to = new PowerManagement();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetAgents()) {
            to.setAgents(new Agents());
            to.getAgents().getAgents().addAll(adaptIn(from.getAgents().getAgents()));
        }
        if (from.isSetAutomaticPmEnabled()) {
            to.setAutomaticPmEnabled(from.isAutomaticPmEnabled());
        }
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetKdumpDetection()) {
            to.setKdumpDetection(from.isKdumpDetection());
        }
        if (from.isSetOptions()) {
            to.setOptions(new Options());
            to.getOptions().getOptions().addAll(adaptIn(from.getOptions().getOptions()));
        }
        if (from.isSetPassword()) {
            to.setPassword(from.getPassword());
        }
        if (from.isSetPmProxies()) {
            to.setPmProxies(new PmProxies());
            to.getPmProxies().getPmProxies().addAll(adaptIn(from.getPmProxies().getPmProxy()));
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(PowerManagementStatus.fromValue(from.getStatus().getState()));
        }
        if (from.isSetType()) {
            to.setType(from.getType());
        }
        if (from.isSetUsername()) {
            to.setUsername(from.getUsername());
        }
        return to;
    }
}
