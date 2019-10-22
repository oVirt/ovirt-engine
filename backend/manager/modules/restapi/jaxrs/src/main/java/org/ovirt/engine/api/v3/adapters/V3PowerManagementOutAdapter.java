/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Agents;
import org.ovirt.engine.api.v3.types.V3Options;
import org.ovirt.engine.api.v3.types.V3PmProxies;
import org.ovirt.engine.api.v3.types.V3PowerManagement;
import org.ovirt.engine.api.v3.types.V3Status;

public class V3PowerManagementOutAdapter implements V3Adapter<PowerManagement, V3PowerManagement> {
    @Override
    public V3PowerManagement adapt(PowerManagement from) {
        V3PowerManagement to = new V3PowerManagement();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetAgents()) {
            to.setAgents(new V3Agents());
            to.getAgents().getAgents().addAll(adaptOut(from.getAgents().getAgents()));
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
            to.setOptions(new V3Options());
            to.getOptions().getOptions().addAll(adaptOut(from.getOptions().getOptions()));
        }
        if (from.isSetPassword()) {
            to.setPassword(from.getPassword());
        }
        if (from.isSetPmProxies()) {
            to.setPmProxies(new V3PmProxies());
            to.getPmProxies().getPmProxy().addAll(adaptOut(from.getPmProxies().getPmProxies()));
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
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
