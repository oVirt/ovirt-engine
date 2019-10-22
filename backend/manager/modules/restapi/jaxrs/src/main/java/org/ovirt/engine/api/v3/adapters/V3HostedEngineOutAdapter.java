/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.HostedEngine;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostedEngine;

public class V3HostedEngineOutAdapter implements V3Adapter<HostedEngine, V3HostedEngine> {
    @Override
    public V3HostedEngine adapt(HostedEngine from) {
        V3HostedEngine to = new V3HostedEngine();
        if (from.isSetActive()) {
            to.setActive(from.isActive());
        }
        if (from.isSetConfigured()) {
            to.setConfigured(from.isConfigured());
        }
        if (from.isSetGlobalMaintenance()) {
            to.setGlobalMaintenance(from.isGlobalMaintenance());
        }
        if (from.isSetLocalMaintenance()) {
            to.setLocalMaintenance(from.isLocalMaintenance());
        }
        if (from.isSetScore()) {
            to.setScore(from.getScore());
        }
        return to;
    }
}
