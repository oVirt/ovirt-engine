/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenstackVolumeAuthenticationKey;

public class V3OpenstackVolumeAuthenticationKeyOutAdapter implements V3Adapter<OpenstackVolumeAuthenticationKey, V3OpenstackVolumeAuthenticationKey> {
    @Override
    public V3OpenstackVolumeAuthenticationKey adapt(OpenstackVolumeAuthenticationKey from) {
        V3OpenstackVolumeAuthenticationKey to = new V3OpenstackVolumeAuthenticationKey();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCreationDate()) {
            to.setCreationDate(from.getCreationDate());
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
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOpenstackVolumeProvider()) {
            to.setOpenstackVolumeProvider(adaptOut(from.getOpenstackVolumeProvider()));
        }
        if (from.isSetUsageType()) {
            to.setUsageType(from.getUsageType().value());
        }
        if (from.isSetUuid()) {
            to.setUuid(from.getUuid());
        }
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
