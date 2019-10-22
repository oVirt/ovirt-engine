/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DataCenters;
import org.ovirt.engine.api.v3.types.V3Status;
import org.ovirt.engine.api.v3.types.V3StorageDomain;

public class V3StorageDomainOutAdapter implements V3Adapter<StorageDomain, V3StorageDomain> {
    @Override
    public V3StorageDomain adapt(StorageDomain from) {
        V3StorageDomain to = new V3StorageDomain();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
            to.setDataCenter(adaptOut(from.getDataCenter()));
        }
        if (from.isSetDataCenters()) {
            to.setDataCenters(new V3DataCenters());
            to.getDataCenters().getDataCenters().addAll(adaptOut(from.getDataCenters().getDataCenters()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetExternalStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getExternalStatus().value());
            to.setExternalStatus(status);
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
        if (from.isSetImport()) {
            to.setImport(from.isImport());
        }
        if (from.isSetMaster()) {
            to.setMaster(from.isMaster());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        if (from.isSetStorage()) {
            to.setStorage(adaptOut(from.getStorage()));
        }
        if (from.isSetStorageFormat()) {
            to.setStorageFormat(from.getStorageFormat().value());
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
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
