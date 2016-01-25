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

import org.ovirt.engine.api.model.Initialization;
import org.ovirt.engine.api.model.NicConfigurations;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Initialization;

public class V3InitializationInAdapter implements V3Adapter<V3Initialization, Initialization> {
    @Override
    public Initialization adapt(V3Initialization from) {
        Initialization to = new Initialization();
        if (from.isSetActiveDirectoryOu()) {
            to.setActiveDirectoryOu(from.getActiveDirectoryOu());
        }
        if (from.isSetAuthorizedSshKeys()) {
            to.setAuthorizedSshKeys(from.getAuthorizedSshKeys());
        }
        if (from.isSetCloudInit()) {
            to.setCloudInit(adaptIn(from.getCloudInit()));
        }
        if (from.isSetConfiguration()) {
            to.setConfiguration(adaptIn(from.getConfiguration()));
        }
        if (from.isSetCustomScript()) {
            to.setCustomScript(from.getCustomScript());
        }
        if (from.isSetDnsSearch()) {
            to.setDnsSearch(from.getDnsSearch());
        }
        if (from.isSetDnsServers()) {
            to.setDnsServers(from.getDnsServers());
        }
        if (from.isSetDomain()) {
            to.setDomain(from.getDomain());
        }
        if (from.isSetHostName()) {
            to.setHostName(from.getHostName());
        }
        if (from.isSetInputLocale()) {
            to.setInputLocale(from.getInputLocale());
        }
        if (from.isSetNicConfigurations()) {
            to.setNicConfigurations(new NicConfigurations());
            to.getNicConfigurations().getNicConfigurations().addAll(adaptIn(from.getNicConfigurations().getNicConfigurations()));
        }
        if (from.isSetOrgName()) {
            to.setOrgName(from.getOrgName());
        }
        if (from.isSetRegenerateIds()) {
            to.setRegenerateIds(from.isRegenerateIds());
        }
        if (from.isSetRegenerateSshKeys()) {
            to.setRegenerateSshKeys(from.isRegenerateSshKeys());
        }
        if (from.isSetRootPassword()) {
            to.setRootPassword(from.getRootPassword());
        }
        if (from.isSetSystemLocale()) {
            to.setSystemLocale(from.getSystemLocale());
        }
        if (from.isSetTimezone()) {
            to.setTimezone(from.getTimezone());
        }
        if (from.isSetUiLanguage()) {
            to.setUiLanguage(from.getUiLanguage());
        }
        if (from.isSetUserLocale()) {
            to.setUserLocale(from.getUserLocale());
        }
        if (from.isSetUserName()) {
            to.setUserName(from.getUserName());
        }
        if (from.isSetWindowsLicenseKey()) {
            to.setWindowsLicenseKey(from.getWindowsLicenseKey());
        }
        return to;
    }
}
