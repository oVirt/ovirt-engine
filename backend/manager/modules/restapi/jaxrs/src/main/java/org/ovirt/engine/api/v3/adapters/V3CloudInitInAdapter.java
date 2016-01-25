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
