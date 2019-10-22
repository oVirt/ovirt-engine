/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.CloudInit;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3AuthorizedKeys;
import org.ovirt.engine.api.v3.types.V3CloudInit;
import org.ovirt.engine.api.v3.types.V3Files;
import org.ovirt.engine.api.v3.types.V3Users;

public class V3CloudInitOutAdapter implements V3Adapter<CloudInit, V3CloudInit> {
    @Override
    public V3CloudInit adapt(CloudInit from) {
        V3CloudInit to = new V3CloudInit();
        if (from.isSetAuthorizedKeys()) {
            to.setAuthorizedKeys(new V3AuthorizedKeys());
            to.getAuthorizedKeys().getAuthorizedKeys().addAll(adaptOut(from.getAuthorizedKeys().getAuthorizedKeys()));
        }
        if (from.isSetFiles()) {
            to.setFiles(new V3Files());
            to.getFiles().getFiles().addAll(adaptOut(from.getFiles().getFiles()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptOut(from.getHost()));
        }
        if (from.isSetNetworkConfiguration()) {
            to.setNetworkConfiguration(adaptOut(from.getNetworkConfiguration()));
        }
        if (from.isSetRegenerateSshKeys()) {
            to.setRegenerateSshKeys(from.isRegenerateSshKeys());
        }
        if (from.isSetTimezone()) {
            to.setTimezone(from.getTimezone());
        }
        if (from.isSetUsers()) {
            to.setUsers(new V3Users());
            to.getUsers().getUsers().addAll(adaptOut(from.getUsers().getUsers()));
        }
        return to;
    }
}
