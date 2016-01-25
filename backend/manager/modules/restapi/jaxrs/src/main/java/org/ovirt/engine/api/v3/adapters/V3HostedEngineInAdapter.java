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

import org.ovirt.engine.api.model.HostedEngine;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostedEngine;

public class V3HostedEngineInAdapter implements V3Adapter<V3HostedEngine, HostedEngine> {
    @Override
    public HostedEngine adapt(V3HostedEngine from) {
        HostedEngine to = new HostedEngine();
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
