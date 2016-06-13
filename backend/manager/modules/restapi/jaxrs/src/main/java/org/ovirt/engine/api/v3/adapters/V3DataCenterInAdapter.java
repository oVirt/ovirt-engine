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

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DataCenterStatus;
import org.ovirt.engine.api.model.QuotaModeType;
import org.ovirt.engine.api.model.StorageFormat;
import org.ovirt.engine.api.model.Versions;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DataCenter;

public class V3DataCenterInAdapter implements V3Adapter<V3DataCenter, DataCenter> {
    @Override
    public DataCenter adapt(V3DataCenter from) {
        DataCenter to = new DataCenter();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetLocal()) {
            to.setLocal(from.isLocal());
        }
        if (from.isSetMacPool()) {
            to.setMacPool(adaptIn(from.getMacPool()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetQuotaMode()) {
            to.setQuotaMode(QuotaModeType.fromValue(from.getQuotaMode()));
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(DataCenterStatus.fromValue(from.getStatus().getState()));
        }
        if (from.isSetStorageFormat()) {
            to.setStorageFormat(StorageFormat.fromValue(from.getStorageFormat()));
        }
        if (from.isSetSupportedVersions()) {
            to.setSupportedVersions(new Versions());
            to.getSupportedVersions().getVersions().addAll(adaptIn(from.getSupportedVersions().getVersions()));
        }
        if (from.isSetVersion()) {
            to.setVersion(adaptIn(from.getVersion()));
        }

        // V3 of the API supports the "storage_type" element, but in V4 it has been replaced by the boolean
        // attribute "local":
        if (from.isSetStorageType() && !to.isSetLocal()) {
            boolean local = "localfs".equalsIgnoreCase(from.getStorageType());
            to.setLocal(local);
        }

        return to;
    }
}
