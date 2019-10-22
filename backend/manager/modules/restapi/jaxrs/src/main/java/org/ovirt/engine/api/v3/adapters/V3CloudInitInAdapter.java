/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.AuthorizedKeys;
import org.ovirt.engine.api.model.CloudInit;
import org.ovirt.engine.api.model.Files;
import org.ovirt.engine.api.model.Users;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CloudInit;

public class V3CloudInitInAdapter implements V3Adapter<V3CloudInit, CloudInit> {
    @Override
    public CloudInit adapt(V3CloudInit from) {
        CloudInit to = new CloudInit();
        if (from.isSetAuthorizedKeys()) {
            to.setAuthorizedKeys(new AuthorizedKeys());
            to.getAuthorizedKeys().getAuthorizedKeys().addAll(adaptIn(from.getAuthorizedKeys().getAuthorizedKeys()));
        }
        if (from.isSetFiles()) {
            to.setFiles(new Files());
            to.getFiles().getFiles().addAll(adaptIn(from.getFiles().getFiles()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
        }
        if (from.isSetNetworkConfiguration()) {
            to.setNetworkConfiguration(adaptIn(from.getNetworkConfiguration()));
        }
        if (from.isSetRegenerateSshKeys()) {
            to.setRegenerateSshKeys(from.isRegenerateSshKeys());
        }
        if (from.isSetTimezone()) {
            to.setTimezone(from.getTimezone());
        }
        if (from.isSetUsers()) {
            to.setUsers(new Users());
            to.getUsers().getUsers().addAll(adaptIn(from.getUsers().getUsers()));
        }
        return to;
    }
}
