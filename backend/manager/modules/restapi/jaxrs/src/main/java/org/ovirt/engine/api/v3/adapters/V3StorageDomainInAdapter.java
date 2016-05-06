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

import org.ovirt.engine.api.model.DataCenters;
import org.ovirt.engine.api.model.ExternalStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageFormat;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3StorageDomain;

public class V3StorageDomainInAdapter implements V3Adapter<V3StorageDomain, StorageDomain> {
    @Override
    public StorageDomain adapt(V3StorageDomain from) {
        StorageDomain to = new StorageDomain();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetAvailable()) {
            to.setAvailable(from.getAvailable());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCommitted()) {
            to.setCommitted(from.getCommitted());
        }
        if (from.isSetCriticalSpaceActionBlocker()) {
            to.setCriticalSpaceActionBlocker(from.getCriticalSpaceActionBlocker());
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptIn(from.getDataCenter()));
        }
        if (from.isSetDataCenters()) {
            to.setDataCenters(new DataCenters());
            to.getDataCenters().getDataCenters().addAll(adaptIn(from.getDataCenters().getDataCenters()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetExternalStatus() && from.getExternalStatus().isSetState()) {
            to.setExternalStatus(ExternalStatus.fromValue(from.getExternalStatus().getState()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetImport()) {
            to.setImport(from.isImport());
        }
        if (from.isSetMaster()) {
            to.setMaster(from.isMaster());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(StorageDomainStatus.fromValue(from.getStatus().getState()));
        }
        if (from.isSetStorage()) {
            to.setStorage(adaptIn(from.getStorage()));
        }
        if (from.isSetStorageFormat()) {
            to.setStorageFormat(StorageFormat.fromValue(from.getStorageFormat()));
        }
        if (from.isSetType()) {
            to.setType(StorageDomainType.fromValue(from.getType()));
        }
        if (from.isSetUsed()) {
            to.setUsed(from.getUsed());
        }
        if (from.isSetWarningLowSpaceIndicator()) {
            to.setWarningLowSpaceIndicator(from.getWarningLowSpaceIndicator());
        }
        if (from.isSetWipeAfterDelete()) {
            to.setWipeAfterDelete(from.isWipeAfterDelete());
        }
        return to;
    }
}
