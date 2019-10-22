/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DataCenter;
import org.ovirt.engine.api.v3.types.V3Status;
import org.ovirt.engine.api.v3.types.V3SupportedVersions;

public class V3DataCenterOutAdapter implements V3Adapter<DataCenter, V3DataCenter> {
    @Override
    public V3DataCenter adapt(DataCenter from) {
        V3DataCenter to = new V3DataCenter();
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
            to.setMacPool(adaptOut(from.getMacPool()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetQuotaMode()) {
            to.setQuotaMode(from.getQuotaMode().value());
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        if (from.isSetStorageFormat()) {
            to.setStorageFormat(from.getStorageFormat().value());
        }
        if (from.isSetSupportedVersions()) {
            to.setSupportedVersions(new V3SupportedVersions());
            to.getSupportedVersions().getVersions().addAll(adaptOut(from.getSupportedVersions().getVersions()));
        }
        if (from.isSetVersion()) {
            to.setVersion(adaptOut(from.getVersion()));
        }
        return to;
    }
}
