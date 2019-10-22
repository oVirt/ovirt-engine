/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Storage;

public class V3HostStorageOutAdapter implements V3Adapter<HostStorage, V3Storage> {
    @Override
    public V3Storage adapt(HostStorage from) {
        V3Storage to = new V3Storage();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
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
        if (from.isSetLogicalUnits()) {
            to.getLogicalUnits().addAll(adaptOut(from.getLogicalUnits().getLogicalUnits()));
        }
        if (from.isSetMountOptions()) {
            to.setMountOptions(from.getMountOptions());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNfsRetrans()) {
            to.setNfsRetrans(from.getNfsRetrans());
        }
        if (from.isSetNfsTimeo()) {
            to.setNfsTimeo(from.getNfsTimeo());
        }
        if (from.isSetNfsVersion()) {
            to.setNfsVersion(from.getNfsVersion().value());
        }
        if (from.isSetOverrideLuns()) {
            to.setOverrideLuns(from.isOverrideLuns());
        }
        if (from.isSetPassword()) {
            to.setPassword(from.getPassword());
        }
        if (from.isSetPath()) {
            to.setPath(from.getPath());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetPortal()) {
            to.setPortal(from.getPortal());
        }
        if (from.isSetTarget()) {
            to.setTarget(from.getTarget());
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        if (from.isSetUsername()) {
            to.setUsername(from.getUsername());
        }
        if (from.isSetVfsType()) {
            to.setVfsType(from.getVfsType());
        }
        if (from.isSetVolumeGroup()) {
            to.setVolumeGroup(adaptOut(from.getVolumeGroup()));
        }
        return to;
    }
}
