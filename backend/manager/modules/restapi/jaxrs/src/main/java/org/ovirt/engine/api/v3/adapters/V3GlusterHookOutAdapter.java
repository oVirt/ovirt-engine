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

import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterHook;
import org.ovirt.engine.api.v3.types.V3GlusterServerHooks;
import org.ovirt.engine.api.v3.types.V3Status;

public class V3GlusterHookOutAdapter implements V3Adapter<GlusterHook, V3GlusterHook> {
    @Override
    public V3GlusterHook adapt(GlusterHook from) {
        V3GlusterHook to = new V3GlusterHook();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetChecksum()) {
            to.setChecksum(from.getChecksum());
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptOut(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetConflictStatus()) {
            to.setConflictStatus(from.getConflictStatus());
        }
        if (from.isSetConflicts()) {
            to.setConflicts(from.getConflicts());
        }
        if (from.isSetContent()) {
            to.setContent(from.getContent());
        }
        if (from.isSetContentType()) {
            to.setContentType(from.getContentType().value());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetGlusterCommand()) {
            to.setGlusterCommand(from.getGlusterCommand());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetServerHooks()) {
            to.setServerHooks(new V3GlusterServerHooks());
            to.getServerHooks().getGlusterServerHooks().addAll(adaptOut(from.getServerHooks().getGlusterServerHooks()));
        }
        if (from.isSetStage()) {
            to.setStage(from.getStage().value());
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        return to;
    }
}
