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

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Labels;
import org.ovirt.engine.api.v3.types.V3Network;
import org.ovirt.engine.api.v3.types.V3Usages;

public class V3NetworkOutAdapter implements V3Adapter<Network, V3Network> {
    @Override
    public V3Network adapt(Network from) {
        V3Network to = new V3Network();
        if (from.isSetCluster()) {
            to.setCluster(adaptOut(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptOut(from.getDataCenter()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisplay()) {
            to.setDisplay(from.isDisplay());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIp()) {
            to.setIp(adaptOut(from.getIp()));
        }
        if (from.isSetLabels()) {
            to.setLabels(new V3Labels());
            to.getLabels().getLabels().addAll(adaptOut(from.getLabels().getLabels()));
        }
        if (from.isSetMtu()) {
            to.setMtu(from.getMtu());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetProfileRequired()) {
            to.setProfileRequired(from.isProfileRequired());
        }
        if (from.isSetQos()) {
            to.setQos(adaptOut(from.getQos()));
        }
        if (from.isSetRequired()) {
            to.setRequired(from.isRequired());
        }
        if (from.isSetStatus()) {
            to.setStatus(adaptOut(from.getStatus()));
        }
        if (from.isSetStp()) {
            to.setStp(from.isStp());
        }
        if (from.isSetUsages()) {
            to.setUsages(new V3Usages());
            to.getUsages().getUsages().addAll(from.getUsages().getUsages());
        }
        if (from.isSetVlan()) {
            to.setVlan(adaptOut(from.getVlan()));
        }
        return to;
    }
}
