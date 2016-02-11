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

import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3KatelloErratum;
import org.ovirt.engine.api.v3.types.V3Packages;

public class V3KatelloErratumOutAdapter implements V3Adapter<KatelloErratum, V3KatelloErratum> {
    @Override
    public V3KatelloErratum adapt(KatelloErratum from) {
        V3KatelloErratum to = new V3KatelloErratum();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetHost()) {
            to.setHost(adaptOut(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIssued()) {
            to.setIssued(from.getIssued());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetPackages()) {
            to.setPackages(new V3Packages());
            to.getPackages().getPackages().addAll(adaptOut(from.getPackages().getPackages()));
        }
        if (from.isSetSeverity()) {
            to.setSeverity(from.getSeverity());
        }
        if (from.isSetSolution()) {
            to.setSolution(from.getSolution());
        }
        if (from.isSetSummary()) {
            to.setSummary(from.getSummary());
        }
        if (from.isSetTitle()) {
            to.setTitle(from.getTitle());
        }
        if (from.isSetType()) {
            to.setType(from.getType());
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        return to;
    }
}
